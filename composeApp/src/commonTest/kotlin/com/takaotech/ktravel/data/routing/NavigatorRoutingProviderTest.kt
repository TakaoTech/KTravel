package com.takaotech.ktravel.data.routing

import com.takaotech.ktravel.data.navigator.EmbeddedNavigatorHost
import com.takaotech.ktravel.data.navigator.NavigatorBaseUrlResolver
import com.takaotech.ktravel.data.navigator.NavigatorEndpoint
import com.takaotech.ktravel.domain.model.TravelSettingsDomain
import com.takaotech.ktravel.domain.repository.SettingsRepository
import com.takaotech.ktravel.domain.routing.RoutingProviderSettings
import com.takaotech.navigator.api.NavigatorApi
import com.takaotech.navigator.api.NavigatorJson
import com.takaotech.navigator.api.common.GeoPoint
import com.takaotech.navigator.api.common.ProviderId
import com.takaotech.navigator.api.common.ProviderProfile
import com.takaotech.navigator.api.common.TravelMode
import com.takaotech.navigator.api.error.ErrorCode
import com.takaotech.navigator.api.error.ErrorResponse
import com.takaotech.navigator.api.here.HereCarRouteRequest
import com.takaotech.navigator.api.response.PolylineEncoding
import com.takaotech.navigator.api.response.RouteActionDto
import com.takaotech.navigator.api.response.RouteDto
import com.takaotech.navigator.api.response.RouteGeometry
import com.takaotech.navigator.api.response.RouteResponse
import com.takaotech.navigator.api.response.RouteSectionDto
import com.takaotech.navigator.api.response.RouteSummaryDto
import com.takaotech.navigator.api.response.RouteWaypointDto
import com.takaotech.navigator.client.NavigatorClient
import com.takaotech.navigator.client.NavigatorClientConfig
import com.takaotech.navigator.client.NavigatorException
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.request.HttpRequestData
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.utils.io.errors.IOException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.time.Duration.Companion.seconds

/**
 * The app's side of the seam.
 *
 * The provider is now the only thing in the app that knows a contract exists, so it is the only
 * thing worth testing here: that the plan's settings become the right request, that a contract
 * answer becomes the model the screens draw, and that an unreachable navigator is retried exactly
 * once while a refused one is not retried at all.
 *
 * No routing engine appears anywhere below, which is the point of the whole exercise.
 */
class NavigatorRoutingProviderTest :
    BehaviorSpec({

        val origin = "44.4949,11.3426"
        val destination = "43.7696,11.2558"

        val response = RouteResponse(
            provider = ProviderId.HERE,
            profile = ProviderProfile.CAR,
            routes = listOf(
                RouteDto(
                    summary = RouteSummaryDto(durationSeconds = 3900, distanceMeters = 90_000),
                    sections = listOf(
                        RouteSectionDto(
                            summary = RouteSummaryDto(durationSeconds = 3900, distanceMeters = 90_000),
                            mode = TravelMode.CAR,
                            actions = listOf(
                                RouteActionDto(
                                    action = "turn",
                                    durationSeconds = 120,
                                    distanceMeters = 1_400,
                                    instruction = "Turn right onto the A1",
                                    offset = 12,
                                    direction = "right",
                                ),
                            ),
                            departure = RouteWaypointDto(place = GeoPoint(lat = 44.4949, lng = 11.3426)),
                            arrival = RouteWaypointDto(place = GeoPoint(lat = 43.7696, lng = 11.2558)),
                            geometry = RouteGeometry(PolylineEncoding.HERE_FLEXIBLE, "BFoz5xJ67i1B1B7PzIhaxL7Y"),
                        ),
                    ),
                ),
            ),
        )

        /**
         * A navigator that answers whatever the test says, and remembers what it was asked.
         *
         * @param failuresBeforeSuccess How many calls are refused at the transport level first,
         *   which is how a suspended embedded server behaves on the first request after a wake up.
         */
        fun provider(
            body: String = NavigatorJson.encodeToString(RouteResponse.serializer(), response),
            status: HttpStatusCode = HttpStatusCode.OK,
            failuresBeforeSuccess: Int = 0,
            recorded: MutableList<HttpRequestData> = mutableListOf(),
            apiKey: String = "plan-key",
        ): Pair<NavigatorRoutingProvider, MutableList<HttpRequestData>> {
            var remainingFailures = failuresBeforeSuccess
            val engine = MockEngine { request ->
                if (remainingFailures-- > 0) throw IOException("Connection refused")
                recorded += request
                respond(body, status, headersOf(HttpHeaders.ContentType, "application/json"))
            }
            val client = NavigatorClient.withEngine(engine, NavigatorClientConfig("http://navigator.test"))

            // A remote endpoint, so `recover` does not start a real server: what is under test is
            // that the retry happens at all, not what the embedded host does when it does.
            val resolver = NavigatorBaseUrlResolver(
                embeddedHost = EmbeddedNavigatorHost(),
                endpoint = { NavigatorEndpoint.Remote("http://navigator.test") },
            )

            return NavigatorRoutingProvider(
                client = client,
                navigatorBaseUrl = resolver,
                settingsRepository = FakeSettingsRepository(apiKey),
            ) to recorded
        }

        given("a plan configured for HERE road routing") {
            val settings = RoutingProviderSettings.Here(alternatives = 2)

            `when`("routes are requested") {
                then("the plan settings reach the navigator as a contract request") {
                    withContext(Dispatchers.Default) {
                        val (provider, recorded) = provider()

                        provider.getRoutes(origin, destination, settings)

                        val sent = recorded.single()
                        sent.url.encodedPath shouldBe NavigatorApi.HERE_CAR
                        sent.headers[NavigatorApi.PROVIDER_KEY_HEADER] shouldBe "plan-key"
                        val body = NavigatorJson.decodeFromString(
                            HereCarRouteRequest.serializer(),
                            sent.bodyText(),
                        )
                        body.origin shouldBe GeoPoint(lat = 44.4949, lng = 11.3426)
                        body.destination shouldBe GeoPoint(lat = 43.7696, lng = 11.2558)
                        body.alternatives shouldBe 2
                    }
                }

                then("the answer becomes the model the screens draw") {
                    withContext(Dispatchers.Default) {
                        val (provider, _) = provider()

                        val routes = provider.getRoutes(origin, destination, settings)

                        routes.routes shouldHaveSize 1
                        val section = routes.routes.single().sections.single()
                        section.summary.durationSeconds shouldBe 3900.seconds
                        section.summary.distanceMeters shouldBe 90_000
                        section.polyline shouldBe "BFoz5xJ67i1B1B7PzIhaxL7Y"
                        section.actions.single().instruction shouldBe "Turn right onto the A1"
                        section.actions.single().offset shouldBe 12
                    }
                }
            }

            // The screens decode the polyline themselves and only know one encoding. Handing them a
            // Valhalla one would draw a line across the wrong continent rather than fail.
            `when`("the navigator answers in an encoding the map cannot draw") {
                then("the geometry is dropped rather than misread") {
                    withContext(Dispatchers.Default) {
                        val foreign = response.copy(
                            routes = response.routes.map { route ->
                                route.copy(
                                    sections = route.sections.map {
                                        it.copy(geometry = RouteGeometry(PolylineEncoding.POLYLINE6, "abc"))
                                    },
                                )
                            },
                        )
                        val (provider, _) = provider(
                            body = NavigatorJson.encodeToString(RouteResponse.serializer(), foreign),
                        )

                        provider.getRoutes(origin, destination, settings)
                            .routes.single().sections.single().polyline.shouldBeNull()
                    }
                }
            }

            // What happens on iOS every time the app comes back to the foreground.
            `when`("the navigator cannot be reached on the first attempt") {
                then("it is restarted and the request is sent again") {
                    withContext(Dispatchers.Default) {
                        val (provider, recorded) = provider(failuresBeforeSuccess = 1)

                        val routes = provider.getRoutes(origin, destination, settings)

                        routes.routes shouldHaveSize 1
                        recorded shouldHaveSize 1
                    }
                }
            }

            `when`("the navigator cannot be reached at all") {
                then("it gives up after the second attempt rather than looping") {
                    withContext(Dispatchers.Default) {
                        val (provider, _) = provider(failuresBeforeSuccess = Int.MAX_VALUE)

                        shouldThrow<NavigatorException> {
                            provider.getRoutes(origin, destination, settings)
                        }.message shouldBe "The navigator could not be reached, even after being restarted"
                    }
                }
            }

            // A refusal is an answer. Retrying would double every call made with a wrong key, and
            // restart a server that was never unhealthy.
            `when`("the navigator refuses the request") {
                then("it is reported and never retried") {
                    withContext(Dispatchers.Default) {
                        val error = ErrorResponse(
                            code = ErrorCode.PROVIDER_UNAUTHORIZED,
                            message = "The key was rejected",
                        )
                        val (provider, recorded) = provider(
                            body = NavigatorJson.encodeToString(ErrorResponse.serializer(), error),
                            status = HttpStatusCode.Unauthorized,
                        )

                        shouldThrow<NavigatorException> {
                            provider.getRoutes(origin, destination, settings)
                        }

                        recorded shouldHaveSize 1
                    }
                }
            }

            `when`("a place is not a coordinate") {
                then("it fails before anything is sent") {
                    withContext(Dispatchers.Default) {
                        val (provider, recorded) = provider()

                        shouldThrow<IllegalArgumentException> {
                            provider.getRoutes("not a coordinate", destination, settings)
                        }

                        recorded shouldHaveSize 0
                    }
                }
            }

            `when`("the settings are for a different provider") {
                then("it is refused rather than sent as a road request") {
                    withContext(Dispatchers.Default) {
                        val (provider, _) = provider()

                        shouldThrow<IllegalArgumentException> {
                            provider.getRoutes(origin, destination, RoutingProviderSettings.Local())
                        }
                    }
                }
            }
        }
    })

/** The plan's key, and nothing else this provider reads. */
private class FakeSettingsRepository(apiKey: String) : SettingsRepository {
    override val settings = TravelSettingsDomain(hereApiKey = apiKey)

    override suspend fun updateHereApiKey(apiKey: String) = error("Not written by the routing provider")
}

/** The body a recorded request carried, as text. */
private fun HttpRequestData.bodyText(): String = (body as io.ktor.http.content.TextContent).text
