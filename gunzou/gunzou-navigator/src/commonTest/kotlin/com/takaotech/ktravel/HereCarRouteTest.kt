package com.takaotech.ktravel

import com.takaotech.navigator.api.NavigatorApi
import com.takaotech.navigator.api.common.GeoPoint
import com.takaotech.navigator.api.common.ProviderId
import com.takaotech.navigator.api.common.ProviderProfile
import com.takaotech.navigator.api.common.RouteTime
import com.takaotech.navigator.api.common.TravelMode
import com.takaotech.navigator.api.common.Units
import com.takaotech.navigator.api.error.ErrorCode
import com.takaotech.navigator.api.here.HereAvoidFeature
import com.takaotech.navigator.api.here.HereAvoidOptions
import com.takaotech.navigator.api.here.HereCarRouteRequest
import com.takaotech.navigator.api.here.HereReturnAttribute
import com.takaotech.navigator.api.here.HereRoutingMode
import com.takaotech.navigator.api.here.HereTransportMode
import com.takaotech.navigator.api.response.NoticeSeverity
import com.takaotech.navigator.api.response.PolylineEncoding
import io.ktor.client.request.get
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.testApplication
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.Instant

/**
 * `POST /v1/here/car` end to end, with a recorded HERE answer behind it.
 *
 * Two things are under test and they are worth naming apart. The first is the query string this
 * server builds from a contract request: it is the only part of the road profile that HERE ever
 * sees, and getting it wrong is invisible in every unit test of a mapper. The second is what the
 * server makes of the answer, which is what the app will draw.
 */
class HereCarRouteTest {

    private val request = HereCarRouteRequest(
        origin = GeoPoint(lat = 44.4949, lng = 11.3426),
        destination = GeoPoint(lat = 43.7696, lng = 11.2558),
    )

    // ---- what reaches HERE -------------------------------------------------------------------

    @Test
    fun `Given a request with every option set When routing Then each one reaches HERE as a query parameter`() =
        testApplication {
            val here = HereMockServer(HerePayloads.CAR_ROUTE)
            application { module(here.asKoinModule()) }

            val full = request.copy(
                via = listOf(GeoPoint(lat = 44.1391, lng = 11.1583)),
                transportMode = HereTransportMode.TRUCK,
                routingMode = HereRoutingMode.SHORT,
                alternatives = 3,
                units = Units.IMPERIAL,
                language = "it-IT",
                returnAttributes = HereReturnAttribute.NAVIGATION_WITH_TOLLS,
            )

            client.postJson(NavigatorApi.HERE_CAR, HereCarRouteRequest.serializer(), full)

            val url = here.requestUrl
            assertEquals("truck", url.query("transportMode"))
            assertEquals("short", url.query("routingMode"))
            assertEquals("44.4949,11.3426", url.query("origin"))
            assertEquals("43.7696,11.2558", url.query("destination"))
            assertEquals(listOf("44.1391,11.1583"), url.queryAll("via"))
            assertEquals("3", url.query("alternatives"))
            assertEquals("imperial", url.query("units"))
            assertEquals("it-IT", url.query("lang"))
            assertContains(url.query("return").orEmpty(), "tolls")
        }

    @Test
    fun `Given the caller sends the provider key When routing Then it is what HERE is called with`() = testApplication {
        val here = HereMockServer(HerePayloads.CAR_ROUTE)
        application { module(here.asKoinModule()) }

        client.postJson(NavigatorApi.HERE_CAR, HereCarRouteRequest.serializer(), request, providerKey = "caller-key")

        assertEquals("caller-key", here.requestUrl.query("apiKey"))
    }

    @Test
    fun `Given no language is asked for When routing Then the server locale is not sent as one`() = testApplication {
        val here = HereMockServer(HerePayloads.CAR_ROUTE)
        application { module(here.asKoinModule()) }

        client.postJson(NavigatorApi.HERE_CAR, HereCarRouteRequest.serializer(), request)

        assertNull(here.requestUrl.query("lang"), "The machine running the server is not the traveller")
    }

    @Test
    fun `Given a departure now When routing Then no time is pinned so HERE uses live traffic`() = testApplication {
        val here = HereMockServer(HerePayloads.CAR_ROUTE)
        application { module(here.asKoinModule()) }

        client.postJson(NavigatorApi.HERE_CAR, HereCarRouteRequest.serializer(), request)

        val url = here.requestUrl
        assertNull(url.query("departureTime"))
        assertNull(url.query("arrivalTime"))
    }

    @Test
    fun `Given a departure at an instant When routing Then it reaches HERE with its offset spelled out`() =
        testApplication {
            val here = HereMockServer(HerePayloads.CAR_ROUTE)
            application { module(here.asKoinModule()) }

            val departAt = request.copy(time = RouteTime.DepartAt(Instant.parse("2026-08-13T07:30:00Z")))

            client.postJson(NavigatorApi.HERE_CAR, HereCarRouteRequest.serializer(), departAt)

            val sent = here.requestUrl.query("departureTime").orEmpty()
            assertContains(sent, "2026-08-13T07:30:00")
            assertTrue(sent.endsWith("+00:00") || sent.endsWith("Z"), "Ambiguous without an offset: $sent")
        }

    @Test
    fun `Given an arrival by an instant When routing Then HERE is asked to plan backwards`() = testApplication {
        val here = HereMockServer(HerePayloads.CAR_ROUTE)
        application { module(here.asKoinModule()) }

        val arriveBy = request.copy(time = RouteTime.ArriveBy(Instant.parse("2026-08-13T09:00:00Z")))

        client.postJson(NavigatorApi.HERE_CAR, HereCarRouteRequest.serializer(), arriveBy)

        assertEquals("2026-08-13T09:00:00Z", here.requestUrl.query("arrivalTime"))
        assertNull(here.requestUrl.query("departureTime"))
    }

    @Test
    fun `Given tolls are to be avoided When routing Then their cost is requested so it can be shown`() =
        testApplication {
            val here = HereMockServer(HerePayloads.CAR_ROUTE)
            application { module(here.asKoinModule()) }

            val avoidTolls = request.copy(
                avoid = HereAvoidOptions(features = listOf(HereAvoidFeature.TOLL_ROAD)),
                returnAttributes = HereReturnAttribute.NAVIGATION,
            )

            client.postJson(NavigatorApi.HERE_CAR, HereCarRouteRequest.serializer(), avoidTolls)

            assertContains(here.requestUrl.query("return").orEmpty(), "tolls")
        }

    @Test
    fun `Given a private bus When routing Then the mode is spelled the way HERE spells it`() = testApplication {
        val here = HereMockServer(HerePayloads.CAR_ROUTE)
        application { module(here.asKoinModule()) }

        val privateBus = request.copy(transportMode = HereTransportMode.PRIVATE_BUS)

        client.postJson(NavigatorApi.HERE_CAR, HereCarRouteRequest.serializer(), privateBus)

        assertEquals("privateBus", here.requestUrl.query("transportMode"))
    }

    // ---- what the caller gets back -------------------------------------------------------------

    @Test
    fun `Given a HERE answer When it is translated Then it becomes the common response`() = testApplication {
        val here = HereMockServer(HerePayloads.CAR_ROUTE)
        application { module(here.asKoinModule()) }

        val response = client.postJson(NavigatorApi.HERE_CAR, HereCarRouteRequest.serializer(), request)

        assertEquals(HttpStatusCode.OK, response.status)
        val routes = response.decodeRoutes()
        assertEquals(ProviderId.HERE, routes.provider)
        assertEquals(ProviderProfile.CAR, routes.profile)
        assertEquals(2, routes.routes.single().sections.size)
        assertEquals(TravelMode.CAR, routes.routes.single().sections.first().mode)
    }

    @Test
    fun `Given a route of several sections When it is translated Then the summary is their sum`() = testApplication {
        val here = HereMockServer(HerePayloads.CAR_ROUTE)
        application { module(here.asKoinModule()) }

        val route = client.postJson(NavigatorApi.HERE_CAR, HereCarRouteRequest.serializer(), request)
            .decodeRoutes().routes.single()

        assertEquals(2100 + 1800, route.summary.durationSeconds)
        assertEquals(48_000 + 42_000, route.summary.distanceMeters)
        assertEquals(1980 + 1700, route.summary.baseDurationSeconds, "Traffic delay is worth showing")
    }

    @Test
    fun `Given a section with a polyline When it is translated Then it keeps HERE own encoding`() = testApplication {
        val here = HereMockServer(HerePayloads.CAR_ROUTE)
        application { module(here.asKoinModule()) }

        val geometry = client.postJson(NavigatorApi.HERE_CAR, HereCarRouteRequest.serializer(), request)
            .decodeRoutes().routes.single().sections.first().geometry

        assertEquals(PolylineEncoding.HERE_FLEXIBLE, geometry?.encoding)
        assertEquals("BFoz5xJ67i1B1B7PzIhaxL7Y", geometry?.value)
    }

    @Test
    fun `Given a departure time with an offset When it is translated Then the offset survives`() = testApplication {
        val here = HereMockServer(HerePayloads.CAR_ROUTE)
        application { module(here.asKoinModule()) }

        val departure = client.postJson(NavigatorApi.HERE_CAR, HereCarRouteRequest.serializer(), request)
            .decodeRoutes().routes.single().sections.first().departure

        assertEquals(Instant.parse("2026-08-13T07:00:00Z"), departure?.time?.instant)
        assertEquals(2 * 60 * 60, departure?.time?.offsetSeconds, "The wall clock at the stop, not on the device")
        assertEquals("Via Rizzoli", departure?.name)
    }

    @Test
    fun `Given actions were requested When they are translated Then the instructions come through`() = testApplication {
        val here = HereMockServer(HerePayloads.CAR_ROUTE)
        application { module(here.asKoinModule()) }

        val actions = client.postJson(NavigatorApi.HERE_CAR, HereCarRouteRequest.serializer(), request)
            .decodeRoutes().routes.single().sections.first().actions

        assertEquals(listOf("depart", "turn"), actions.map { it.action })
        assertEquals("Turn right onto the A1", actions.last().instruction)
        assertEquals("right", actions.last().direction)
        assertEquals(1_400, actions.last().distanceMeters)
    }

    @Test
    fun `Given a toll priced as one figure When it is translated Then it is a range with equal ends`() =
        testApplication {
            val here = HereMockServer(HerePayloads.CAR_ROUTE)
            application { module(here.asKoinModule()) }

            val section = client.postJson(NavigatorApi.HERE_CAR, HereCarRouteRequest.serializer(), request)
                .decodeRoutes().routes.single().sections.first()

            assertEquals(listOf("autostrade"), section.tollSystems.map { it.id })
            val toll = section.tolls.single()
            assertEquals(listOf(0), toll.tollSystemRefs)
            assertEquals("ITA", toll.countryCode)
            val price = toll.fares.single().price
            assertEquals("EUR", price.currency)
            assertEquals(8.7, price.minimum)
            assertEquals(8.7, price.maximum)
        }

    @Test
    fun `Given a toll priced as a range When it is translated Then both ends survive`() = testApplication {
        val here = HereMockServer(HerePayloads.CAR_ROUTE_WITH_PRICE_RANGE)
        application { module(here.asKoinModule()) }

        val price = client.postJson(NavigatorApi.HERE_CAR, HereCarRouteRequest.serializer(), request)
            .decodeRoutes().routes.single().sections.single().tolls.single().fares.single().price

        assertEquals("CHF", price.currency)
        assertEquals(12.0, price.minimum)
        assertEquals(40.0, price.maximum)
        assertTrue(price.estimated)
    }

    @Test
    fun `Given HERE warns about the answer When it is translated Then the notice is carried through`() =
        testApplication {
            val here = HereMockServer(HerePayloads.CAR_ROUTE)
            application { module(here.asKoinModule()) }

            val notice = client.postJson(NavigatorApi.HERE_CAR, HereCarRouteRequest.serializer(), request)
                .decodeRoutes().notices.single()

            assertEquals(NoticeSeverity.CRITICAL, notice.severity)
            assertEquals("Route uses a toll road", notice.title)
        }

    @Test
    fun `Given a road section When it is translated Then it carries no transit details`() = testApplication {
        val here = HereMockServer(HerePayloads.CAR_ROUTE)
        application { module(here.asKoinModule()) }

        val sections = client.postJson(NavigatorApi.HERE_CAR, HereCarRouteRequest.serializer(), request)
            .decodeRoutes().routes.single().sections

        assertTrue(sections.all { it.transit == null })
    }

    // ---- what happens when it goes wrong -------------------------------------------------------

    @Test
    fun `Given HERE rejects the key When routing Then the caller is told it was the key`() = testApplication {
        val here = HereMockServer(HerePayloads.UNAUTHORIZED, HttpStatusCode.Unauthorized)
        application { module(here.asKoinModule()) }

        val response = client.postJson(NavigatorApi.HERE_CAR, HereCarRouteRequest.serializer(), request)

        assertEquals(HttpStatusCode.Unauthorized, response.status)
        val error = response.decodeError()
        assertEquals(ErrorCode.PROVIDER_UNAUTHORIZED, error.code)
        assertEquals(401, error.providerStatus, "HERE's own status is kept for whoever is debugging")
        assertContains(error.providerMessage.orEmpty(), "credentials")
    }

    @Test
    fun `Given HERE is out of quota When routing Then the caller is told to try later`() = testApplication {
        val here = HereMockServer("""{ "title": "Too Many Requests", "status": 429 }""", HttpStatusCode.TooManyRequests)
        application { module(here.asKoinModule()) }

        val response = client.postJson(NavigatorApi.HERE_CAR, HereCarRouteRequest.serializer(), request)

        assertEquals(HttpStatusCode.TooManyRequests, response.status)
        assertEquals(ErrorCode.PROVIDER_RATE_LIMITED, response.decodeError().code)
    }

    @Test
    fun `Given HERE is down When routing Then it is reported as the provider being unavailable`() = testApplication {
        val here = HereMockServer("""{ "title": "Internal Error", "status": 500 }""", HttpStatusCode.ServiceUnavailable)
        application { module(here.asKoinModule()) }

        val response = client.postJson(NavigatorApi.HERE_CAR, HereCarRouteRequest.serializer(), request)

        assertEquals(HttpStatusCode.BadGateway, response.status)
        assertEquals(ErrorCode.PROVIDER_UNAVAILABLE, response.decodeError().code)
    }

    @Test
    fun `Given HERE finds nothing When routing Then it is an error and not an empty list`() = testApplication {
        val here = HereMockServer(HerePayloads.NO_ROUTES)
        application { module(here.asKoinModule()) }

        val response = client.postJson(NavigatorApi.HERE_CAR, HereCarRouteRequest.serializer(), request)

        assertEquals(HttpStatusCode.UnprocessableEntity, response.status)
        assertEquals(ErrorCode.NO_ROUTE_FOUND, response.decodeError().code)
    }

    // ---- what never reaches HERE at all --------------------------------------------------------

    @Test
    fun `Given no provider key When routing Then HERE is never called`() = testApplication {
        val here = HereMockServer(HerePayloads.CAR_ROUTE)
        application { module(here.asKoinModule()) }

        val response = client.postJson(
            NavigatorApi.HERE_CAR,
            HereCarRouteRequest.serializer(),
            request,
            providerKey = null,
        )

        assertEquals(HttpStatusCode.Unauthorized, response.status)
        assertEquals(ErrorCode.MISSING_CREDENTIALS, response.decodeError().code)
        assertEquals(emptyList(), here.requests, "A request with no key is not worth a call")
    }

    @Test
    fun `Given a blank provider key When routing Then it counts as no key at all`() = testApplication {
        val here = HereMockServer(HerePayloads.CAR_ROUTE)
        application { module(here.asKoinModule()) }

        val response = client.postJson(
            NavigatorApi.HERE_CAR,
            HereCarRouteRequest.serializer(),
            request,
            providerKey = "   ",
        )

        assertEquals(HttpStatusCode.Unauthorized, response.status)
        assertEquals(emptyList(), here.requests)
    }

    @Test
    fun `Given a body that is not valid JSON When routing Then it fails as an invalid request`() = testApplication {
        val here = HereMockServer(HerePayloads.CAR_ROUTE)
        application { module(here.asKoinModule()) }

        val response = client.postRaw(NavigatorApi.HERE_CAR, "{ this is not json")

        assertEquals(HttpStatusCode.BadRequest, response.status)
        assertEquals(ErrorCode.INVALID_REQUEST, response.decodeError().code)
        assertEquals(emptyList(), here.requests)
    }

    @Test
    fun `Given a coordinate off the globe When routing Then the failure names the offending field`() = testApplication {
        val here = HereMockServer(HerePayloads.CAR_ROUTE)
        application { module(here.asKoinModule()) }

        val offGlobe = request.copy(origin = GeoPoint(lat = 200.0, lng = 11.3426))

        val response = client.postJson(NavigatorApi.HERE_CAR, HereCarRouteRequest.serializer(), offGlobe)

        assertEquals(HttpStatusCode.BadRequest, response.status)
        assertContains(response.decodeError().message, "origin.lat")
        assertEquals(emptyList(), here.requests)
    }

    @Test
    fun `Given more alternatives than the profile advertises When routing Then the limit is enforced first`() =
        testApplication {
            val here = HereMockServer(HerePayloads.CAR_ROUTE)
            application { module(here.asKoinModule()) }

            val response = client
                .postJson(NavigatorApi.HERE_CAR, HereCarRouteRequest.serializer(), request.copy(alternatives = 7))

            assertEquals(HttpStatusCode.BadRequest, response.status)
            assertEquals(ErrorCode.INVALID_REQUEST, response.decodeError().code)
            assertEquals(emptyList(), here.requests, "A request the profile has published it will reject is not sent")
        }

    @Test
    fun `Given return attributes with an unmet dependency When routing Then the caller is told which one`() =
        testApplication {
            val here = HereMockServer(HerePayloads.CAR_ROUTE)
            application { module(here.asKoinModule()) }

            val unmet = request.copy(returnAttributes = listOf(HereReturnAttribute.ACTIONS))

            val response = client.postJson(NavigatorApi.HERE_CAR, HereCarRouteRequest.serializer(), unmet)

            assertEquals(HttpStatusCode.BadRequest, response.status)
            assertContains(response.decodeError().message, "POLYLINE")
        }

    @Test
    fun `Given the routing path When it is called with GET Then it is not served`() = testApplication {
        val here = HereMockServer(HerePayloads.CAR_ROUTE)
        application { module(here.asKoinModule()) }

        val response = client.get(NavigatorApi.HERE_CAR)

        assertTrue(
            response.status == HttpStatusCode.MethodNotAllowed || response.status == HttpStatusCode.NotFound,
            "Routing is a POST only path, got ${response.status}",
        )
    }
}
