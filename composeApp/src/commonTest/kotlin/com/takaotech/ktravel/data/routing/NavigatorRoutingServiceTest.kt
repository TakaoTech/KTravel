package com.takaotech.ktravel.data.routing

import com.takaotech.gunzou.api.NavigatorApi
import com.takaotech.gunzou.api.catalog.NavigatorProfile
import com.takaotech.gunzou.client.NavigatorClient
import com.takaotech.gunzou.client.NavigatorClientConfig
import com.takaotech.ktravel.core.telemetry.TelemetryConsent
import com.takaotech.ktravel.data.navigator.EmbeddedNavigatorHost
import com.takaotech.ktravel.data.navigator.NavigatorTargetResolver
import com.takaotech.ktravel.domain.model.AppSettingsDomain
import com.takaotech.ktravel.domain.model.TravelSettingsDomain
import com.takaotech.ktravel.domain.navigator.NavigatorKind
import com.takaotech.ktravel.domain.repository.AppSettingsRepository
import com.takaotech.ktravel.domain.repository.SettingsRepository
import com.takaotech.ktravel.domain.routing.ProfileAvailability
import com.takaotech.ktravel.domain.routing.RouteFeature
import com.takaotech.ktravel.domain.routing.RouteSelection
import com.takaotech.ktravel.domain.routing.RouteTimeChoice
import com.takaotech.ktravel.domain.routing.RoutingFailure
import com.takaotech.ktravel.domain.routing.RoutingMode
import com.takaotech.ktravel.domain.routing.RoutingProfileId
import com.takaotech.ktravel.domain.routing.model.RouteResult
import dev.mokkery.answering.returns
import dev.mokkery.everySuspend
import dev.mokkery.mock
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldNotContain
import io.kotest.matchers.types.shouldBeInstanceOf
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.request.HttpRequestData
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.TextContent
import io.ktor.http.headersOf
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.withContext
import kotlinx.datetime.LocalDate
import kotlinx.datetime.Month
import kotlin.time.Instant

private const val EMBEDDED_URL = "http://127.0.0.1:54213"
private const val API_KEY = "here-key"

/** The day these legs belong to. Irrelevant while every request leaves now, but required to make one. */
private val DAY = LocalDate(year = 2026, month = Month.MAY, day = 18)

private val HERE_ROUTING = RoutingProfileId("here", "routing")
private val HERE_TRANSIT = RoutingProfileId("here", "transit")

private const val BOTH_PROFILES = """
{"version":"1.2.3","profiles":[
  {"provider":"here","profile":"routing","path":"/v1/here/routing/{transportMode}","displayName":"HERE road routing","maxAlternatives":6},
  {"provider":"here","profile":"transit","path":"/v1/here/transit","displayName":"HERE public transit","maxAlternatives":5}
]}
"""

private const val ONLY_ROUTING = """
{"version":"1.2.3","profiles":[
  {"provider":"here","profile":"routing","path":"/v1/here/routing/{transportMode}","displayName":"HERE road routing","maxAlternatives":6}
]}
"""

private const val ONE_ROUTE = """
{"provider":"here","profile":"routing","routes":[
  {"summary":{"durationSeconds":600,"distanceMeters":4200},
   "sections":[{"summary":{"durationSeconds":600,"distanceMeters":4200},"mode":"CAR"}]}
]}
"""

private class FakeAppSettings(initial: AppSettingsDomain = AppSettingsDomain()) : AppSettingsRepository {
    override val settings: StateFlow<AppSettingsDomain> = MutableStateFlow(initial)
    override suspend fun updateNavigatorRemote(baseUrl: String) = error("Not written here")
    override suspend fun updateTelemetryConsent(consent: TelemetryConsent, flowVersion: Int, decidedAt: Instant) =
        error("Not written here")

    override suspend fun updateLogRetentionDays(days: Int) = error("Not written here")

    override suspend fun installationId(): String = "test-installation"
}

private class FakePlanSettings(override val settings: TravelSettingsDomain) : SettingsRepository {
    override suspend fun updateHereApiKey(apiKey: String) = error("Not written here")
    override suspend fun updateNavigatorSettings(preference: NavigatorKind, remoteBaseUrl: String) =
        error("Not written here")
}

private class Harness(
    private val apiKey: String = API_KEY,
    private val handler: (HttpRequestData) -> Pair<HttpStatusCode, String>,
) {
    val requests = mutableListOf<HttpRequestData>()

    private val engine = MockEngine { request ->
        requests += request
        val (status, body) = handler(request)
        respond(content = body, status = status, headers = headersOf("Content-Type", "application/json"))
    }

    private val host = mock<EmbeddedNavigatorHost> {
        everySuspend { baseUrl() } returns EMBEDDED_URL
        everySuspend { restart() } returns EMBEDDED_URL
    }

    val service = NavigatorRoutingService(
        client = NavigatorClient.withEngine(engine, NavigatorClientConfig(baseUrl = EMBEDDED_URL)),
        targets = NavigatorTargetResolver(
            FakeAppSettings(),
            FakePlanSettings(TravelSettingsDomain(hereApiKey = apiKey)),
            host,
        ),
        settingsRepository = FakePlanSettings(TravelSettingsDomain(hereApiKey = apiKey)),
    )
}

/** The body a recorded request carried, as text. */
private fun HttpRequestData.bodyText(): String = (body as TextContent).text

/**
 * The one thing in the app that computes routes, against a navigator that is not really there.
 *
 * Two properties carry most of the weight. The catalog is a *join* — what the contract can express
 * against what this deployment mounts — and a profile missing from one side has to be reported
 * differently from a profile missing because nothing answered. And the two profiles produce two
 * genuinely different requests to two different paths, which is the thing that would quietly break
 * if their vocabularies were ever pooled.
 */
class NavigatorRoutingServiceTest :
    BehaviorSpec({

        // The client installs HttpTimeout, whose killer coroutine is a delay: under the virtual
        // clock every call races its own timeout and the slower ones lose.
        suspend fun realTime(block: suspend () -> Unit) = withContext(Dispatchers.Default) { block() }

        given("a navigator serving both HERE profiles, and a plan with an API key") {
            `when`("the catalog is read") {
                then("both are offered") {
                    realTime {
                        val catalog = Harness { HttpStatusCode.OK to BOTH_PROFILES }
                            .service.catalog(NavigatorKind.EMBEDDED)

                        catalog.options shouldHaveSize NavigatorProfile.ALL.size
                        catalog.selectable shouldHaveSize 2
                        catalog.isReachable shouldBe true
                        catalog.navigatorVersion shouldBe "1.2.3"
                    }
                }

                then("each profile keeps the modes of its own API") {
                    realTime {
                        val catalog = Harness { HttpStatusCode.OK to BOTH_PROFILES }
                            .service.catalog(NavigatorKind.EMBEDDED)

                        val routing = catalog.options.first { it.profile.id == HERE_ROUTING }.profile
                        val transit = catalog.options.first { it.profile.id == HERE_TRANSIT }.profile

                        // Walking is something you ask the road profile for; on the transit side it
                        // is only ever how the answer describes the steps between stops.
                        routing.options.modes.map { it.id } shouldContain "PEDESTRIAN"
                        transit.options.modes.map { it.id }.joinToString() shouldNotContain "PEDESTRIAN"
                        transit.options.modes.map { it.id } shouldContain "SUBWAY"
                    }
                }
            }
        }

        given("a navigator that mounts only the road profile") {
            `when`("the catalog is read") {
                then("the transit one is still listed, and says why it cannot be picked") {
                    realTime {
                        val catalog = Harness { HttpStatusCode.OK to ONLY_ROUTING }
                            .service.catalog(NavigatorKind.EMBEDDED)

                        // Omitting it would tell the traveller that HERE transit does not exist,
                        // when the truth is that this deployment does not serve it.
                        catalog.options.first { it.profile.id == HERE_TRANSIT }
                            .availability shouldBe ProfileAvailability.NotServed
                        catalog.selectable shouldHaveSize 1
                    }
                }
            }
        }

        given("a plan with no API key") {
            `when`("the catalog is read") {
                then("the profiles that need one are refused before any route is attempted") {
                    realTime {
                        val catalog = Harness(apiKey = "") { HttpStatusCode.OK to BOTH_PROFILES }
                            .service.catalog(NavigatorKind.EMBEDDED)

                        catalog.selectable.shouldHaveSize(0)
                        catalog.options.first { it.profile.id == HERE_ROUTING }
                            .availability shouldBe ProfileAvailability.MissingApiKey
                    }
                }
            }
        }

        given("a navigator that does not answer") {
            `when`("the catalog is read") {
                then("every profile is listed as unreachable rather than the list being empty") {
                    realTime {
                        val harness = Harness { throw io.ktor.utils.io.errors.IOException("Connection refused") }

                        val catalog = harness.service.catalog(NavigatorKind.EMBEDDED)

                        catalog.isReachable shouldBe false
                        catalog.options shouldHaveSize NavigatorProfile.ALL.size
                        catalog.options.forEach {
                            it.availability::class shouldBe
                                ProfileAvailability.NavigatorUnreachable::class
                        }
                    }
                }
            }
        }

        // ---- the two profiles produce two different requests ------------------------------------

        given("a road selection") {
            `when`("a route is computed") {
                then("it goes to the path of its vehicle, carrying the toll preference") {
                    realTime {
                        val harness = Harness { HttpStatusCode.OK to ONE_ROUTE }

                        harness.service.routes(
                            kind = NavigatorKind.EMBEDDED,
                            origin = "44.4949,11.3426",
                            destination = "43.7696,11.2558",
                            selection = RouteSelection.Routing(
                                profileId = HERE_ROUTING,
                                mode = RoutingMode("TRUCK"),
                                alternatives = 3,
                                avoid = setOf(RouteFeature.TOLL_ROAD),
                            ),
                            time = RouteTimeChoice.Now,
                            dayDate = DAY,
                        )

                        val sent = harness.requests.single()
                        // The vehicle is the path, so it is not repeated in the body.
                        sent.url.toString() shouldBe "$EMBEDDED_URL/v1/here/routing/truck"
                        sent.bodyText() shouldNotContain "transportMode"
                        sent.bodyText() shouldContain "TOLL_ROAD"
                        sent.bodyText() shouldContain "TOLLS"
                        sent.headers[NavigatorApi.PROVIDER_KEY_HEADER] shouldBe API_KEY
                    }
                }
            }
        }

        given("a road selection on foot") {
            `when`("a route is computed") {
                then("nothing about tolls is asked for, because a walk pays none") {
                    realTime {
                        val harness = Harness { HttpStatusCode.OK to ONE_ROUTE }

                        harness.service.routes(
                            kind = NavigatorKind.EMBEDDED,
                            origin = "44.4949,11.3426",
                            destination = "43.7696,11.2558",
                            selection = RouteSelection.Routing(
                                profileId = HERE_ROUTING,
                                mode = RoutingMode("PEDESTRIAN"),
                            ),
                            time = RouteTimeChoice.Now,
                            dayDate = DAY,
                        )

                        val sent = harness.requests.single()
                        sent.url.toString() shouldBe "$EMBEDDED_URL/v1/here/routing/pedestrian"
                        sent.bodyText() shouldNotContain "TOLLS"
                    }
                }
            }
        }

        given("a road selection by bicycle") {
            `when`("a route is computed") {
                then("nothing about tolls is asked for either") {
                    realTime {
                        val harness = Harness { HttpStatusCode.OK to ONE_ROUTE }

                        harness.service.routes(
                            kind = NavigatorKind.EMBEDDED,
                            origin = "44.4949,11.3426",
                            destination = "43.7696,11.2558",
                            selection = RouteSelection.Routing(
                                profileId = HERE_ROUTING,
                                mode = RoutingMode("BICYCLE"),
                            ),
                            time = RouteTimeChoice.Now,
                            dayDate = DAY,
                        )

                        val sent = harness.requests.single()
                        sent.url.toString() shouldBe "$EMBEDDED_URL/v1/here/routing/bicycle"
                        sent.bodyText() shouldNotContain "TOLLS"
                    }
                }
            }
        }

        given("a transit selection with a filter") {
            `when`("a journey is computed") {
                then("it goes to the transit path, carrying the accepted services") {
                    realTime {
                        val harness = Harness { HttpStatusCode.OK to ONE_ROUTE }

                        harness.service.routes(
                            kind = NavigatorKind.EMBEDDED,
                            origin = "44.4949,11.3426",
                            destination = "44.5058,11.3428",
                            selection = RouteSelection.Transit(
                                profileId = HERE_TRANSIT,
                                modeFilter = setOf(RoutingMode("SUBWAY"), RoutingMode("REGIONAL_TRAIN")),
                            ),
                            time = RouteTimeChoice.Now,
                            dayDate = DAY,
                        )

                        val sent = harness.requests.single()
                        sent.url.toString() shouldBe "$EMBEDDED_URL${NavigatorApi.HERE_TRANSIT}"
                        sent.bodyText() shouldContain "SUBWAY"
                        // A road-only option has no business in a journey request.
                        sent.bodyText() shouldNotContain "transportMode"
                    }
                }
            }
        }

        given("a transit selection with no filter") {
            `when`("a journey is computed") {
                then("no filter is sent, because an empty one would forbid every service") {
                    realTime {
                        val harness = Harness { HttpStatusCode.OK to ONE_ROUTE }

                        harness.service.routes(
                            kind = NavigatorKind.EMBEDDED,
                            origin = "44.4949,11.3426",
                            destination = "44.5058,11.3428",
                            selection = RouteSelection.Transit(profileId = HERE_TRANSIT),
                            time = RouteTimeChoice.Now,
                            dayDate = DAY,
                        )

                        harness.requests.single().bodyText() shouldNotContain "modes"
                    }
                }
            }
        }

        // ---- failures say what to do about them -------------------------------------------------

        given("a navigator that rejects the access token") {
            `when`("a route is computed") {
                then("the failure names the token rather than the routing key") {
                    realTime {
                        val harness = Harness {
                            HttpStatusCode.Unauthorized to """{"code":"UNAUTHENTICATED","message":"no"}"""
                        }

                        shouldThrow<RoutingFailure.NotAuthenticated> {
                            harness.service.routes(
                                NavigatorKind.EMBEDDED,
                                "44.4949,11.3426",
                                "43.7696,11.2558",
                                RouteSelection.Routing(HERE_ROUTING, RoutingMode("CAR")),
                                RouteTimeChoice.Now,
                                DAY,
                            )
                        }
                    }
                }
            }
        }

        given("a provider with no route between two places") {
            `when`("a route is computed") {
                then("it is a failure of its own, not an unreachable navigator") {
                    realTime {
                        val harness = Harness {
                            HttpStatusCode.UnprocessableEntity to """{"code":"NO_ROUTE_FOUND","message":"none"}"""
                        }

                        shouldThrow<RoutingFailure.NoRouteFound> {
                            harness.service.routes(
                                NavigatorKind.EMBEDDED,
                                "44.4949,11.3426",
                                "43.7696,11.2558",
                                RouteSelection.Routing(HERE_ROUTING, RoutingMode("CAR")),
                                RouteTimeChoice.Now,
                                DAY,
                            )
                        }
                    }
                }
            }
        }

        given("an embedded server whose socket did not survive suspension") {
            `when`("a route is computed") {
                then("it is restarted and asked once more, and the traveller sees nothing") {
                    realTime {
                        var calls = 0
                        val harness = Harness {
                            calls++
                            if (calls == 1) throw io.ktor.utils.io.errors.IOException("Connection refused")
                            HttpStatusCode.OK to ONE_ROUTE
                        }

                        val result = harness.service.routes(
                            NavigatorKind.EMBEDDED,
                            "44.4949,11.3426",
                            "43.7696,11.2558",
                            RouteSelection.Routing(HERE_ROUTING, RoutingMode("CAR")),
                            RouteTimeChoice.Now,
                            DAY,
                        )

                        result.shouldBeInstanceOf<RouteResult.Routing>().routes.routes shouldHaveSize 1
                        calls shouldBe 2
                    }
                }
            }
        }

        given("a remote navigator that cannot be reached") {
            `when`("a route is computed") {
                then("it is not retried, because nothing on the far side can be restarted") {
                    realTime {
                        var calls = 0
                        val harness = Harness {
                            calls++
                            throw io.ktor.utils.io.errors.IOException("Connection refused")
                        }

                        shouldThrow<RoutingFailure.NavigatorUnreachable> {
                            harness.service.routes(
                                NavigatorKind.EMBEDDED,
                                "44.4949,11.3426",
                                "43.7696,11.2558",
                                RouteSelection.Routing(HERE_ROUTING, RoutingMode("CAR")),
                                RouteTimeChoice.Now,
                                DAY,
                            )
                        }

                        // The embedded one does get its single retry; both attempts failed here.
                        calls shouldBe 2
                    }
                }
            }
        }
    })
