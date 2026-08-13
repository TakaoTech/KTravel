package com.takaotech.ktravel

import com.takaotech.ktravel.endpoint.here.FakeHereCarEndpoint
import com.takaotech.navigator.api.NavigatorApi
import com.takaotech.navigator.api.common.GeoPoint
import com.takaotech.navigator.api.common.ProviderId
import com.takaotech.navigator.api.common.ProviderProfile
import com.takaotech.navigator.api.common.RouteTime
import com.takaotech.navigator.api.common.TravelMode
import com.takaotech.navigator.api.error.ErrorCode
import com.takaotech.navigator.api.here.HereCarRouteRequest
import com.takaotech.navigator.api.here.HereReturnAttribute
import com.takaotech.navigator.api.here.HereTransportMode
import io.ktor.client.request.get
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.testApplication
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.time.Instant

/**
 * `POST /v1/here/car` end to end, against the deterministic fake.
 *
 * What is under test is the path itself and everything the server wraps it in — credentials,
 * validation, limits, error shape — not the quality of the routes, which the fake makes up. That
 * separation is the reason the fake exists: these assertions stay true when the real HERE client
 * replaces it.
 */
class HereCarRouteTest {

    private val request = HereCarRouteRequest(
        origin = GeoPoint(lat = 44.4949, lng = 11.3426),
        destination = GeoPoint(lat = 43.7696, lng = 11.2558),
    )

    @Test
    fun `Given a valid request and a provider key When routing Then it answers with the common response`() =
        testApplication {
            application { module() }

            val response = client.postJson(NavigatorApi.HERE_CAR, HereCarRouteRequest.serializer(), request)

            assertEquals(HttpStatusCode.OK, response.status)
            val routes = response.decodeRoutes()
            assertEquals(ProviderId.HERE, routes.provider)
            assertEquals(ProviderProfile.CAR, routes.profile)
            val section = routes.routes.single().sections.single()
            assertEquals(TravelMode.CAR, section.mode)
            assertEquals(request.origin, section.departure?.place)
            assertEquals(request.destination, section.arrival?.place)
        }

    @Test
    fun `Given a fake answer When it is read Then it is marked as not coming from a routing engine`() =
        testApplication {
            application { module() }

            val response = client.postJson(NavigatorApi.HERE_CAR, HereCarRouteRequest.serializer(), request)

            assertEquals(
                listOf(FakeHereCarEndpoint.FAKE_NOTICE_CODE),
                response.decodeRoutes().notices.map { it.code },
            )
        }

    @Test
    fun `Given a route summary When it is read Then it is the sum of the sections`() = testApplication {
        application { module() }

        val withVia = request.copy(via = listOf(GeoPoint(lat = 44.1391, lng = 11.1583)))

        val routes = client.postJson(NavigatorApi.HERE_CAR, HereCarRouteRequest.serializer(), withVia).decodeRoutes()

        val route = routes.routes.single()
        assertEquals(2, route.sections.size, "One section per leg between the waypoints")
        assertEquals(route.sections.sumOf { it.summary.distanceMeters }, route.summary.distanceMeters)
        assertEquals(route.sections.sumOf { it.summary.durationSeconds }, route.summary.durationSeconds)
    }

    @Test
    fun `Given several alternatives are requested When routing Then that many are returned and they differ`() =
        testApplication {
            application { module() }

            val routes = client
                .postJson(NavigatorApi.HERE_CAR, HereCarRouteRequest.serializer(), request.copy(alternatives = 3))
                .decodeRoutes()

            assertEquals(3, routes.routes.size)
            assertEquals(
                routes.routes.map { it.summary.durationSeconds }.sorted(),
                routes.routes.map { it.summary.durationSeconds },
                "Alternatives are returned best first",
            )
            assertEquals(3, routes.routes.map { it.summary.durationSeconds }.distinct().size)
        }

    @Test
    fun `Given a walking request When routing Then the answer is expressed in the mode that was asked for`() =
        testApplication {
            application { module() }

            val walking = request.copy(transportMode = HereTransportMode.PEDESTRIAN)

            val routes = client.postJson(NavigatorApi.HERE_CAR, HereCarRouteRequest.serializer(), walking)
                .decodeRoutes()

            assertEquals(TravelMode.PEDESTRIAN, routes.routes.single().sections.single().mode)
        }

    @Test
    fun `Given no provider key When routing Then it is refused before any provider is called`() = testApplication {
        application { module() }

        val response = client.postJson(
            NavigatorApi.HERE_CAR,
            HereCarRouteRequest.serializer(),
            request,
            providerKey = null,
        )

        assertEquals(HttpStatusCode.Unauthorized, response.status)
        assertEquals(ErrorCode.MISSING_CREDENTIALS, response.decodeError().code)
    }

    @Test
    fun `Given a blank provider key When routing Then it counts as no key at all`() = testApplication {
        application { module() }

        val response = client.postJson(
            NavigatorApi.HERE_CAR,
            HereCarRouteRequest.serializer(),
            request,
            providerKey = "   ",
        )

        assertEquals(HttpStatusCode.Unauthorized, response.status)
        assertEquals(ErrorCode.MISSING_CREDENTIALS, response.decodeError().code)
    }

    @Test
    fun `Given a body that is not valid JSON When routing Then it fails as an invalid request`() = testApplication {
        application { module() }

        val response = client.postRaw(NavigatorApi.HERE_CAR, "{ this is not json")

        assertEquals(HttpStatusCode.BadRequest, response.status)
        assertEquals(ErrorCode.INVALID_REQUEST, response.decodeError().code)
    }

    @Test
    fun `Given a body missing a mandatory field When routing Then it fails as an invalid request`() = testApplication {
        application { module() }

        val response = client.postRaw(NavigatorApi.HERE_CAR, """{ "origin": { "lat": 44.0, "lng": 11.0 } }""")

        assertEquals(HttpStatusCode.BadRequest, response.status)
        assertEquals(ErrorCode.INVALID_REQUEST, response.decodeError().code)
    }

    @Test
    fun `Given a coordinate off the globe When routing Then the failure names the offending field`() = testApplication {
        application { module() }

        val offGlobe = request.copy(origin = GeoPoint(lat = 200.0, lng = 11.3426))

        val response = client.postJson(NavigatorApi.HERE_CAR, HereCarRouteRequest.serializer(), offGlobe)

        assertEquals(HttpStatusCode.BadRequest, response.status)
        val error = response.decodeError()
        assertEquals(ErrorCode.INVALID_REQUEST, error.code)
        assertContains(error.message, "origin.lat")
    }

    @Test
    fun `Given zero alternatives When routing Then it is refused as an invalid request`() = testApplication {
        application { module() }

        val response = client
            .postJson(NavigatorApi.HERE_CAR, HereCarRouteRequest.serializer(), request.copy(alternatives = 0))

        assertEquals(HttpStatusCode.BadRequest, response.status)
        assertEquals(ErrorCode.INVALID_REQUEST, response.decodeError().code)
    }

    @Test
    fun `Given more alternatives than the profile advertises When routing Then the published limit is enforced`() =
        testApplication {
            application { module() }

            val response = client
                .postJson(NavigatorApi.HERE_CAR, HereCarRouteRequest.serializer(), request.copy(alternatives = 7))

            assertEquals(HttpStatusCode.BadRequest, response.status)
            assertEquals(ErrorCode.INVALID_REQUEST, response.decodeError().code)
        }

    @Test
    fun `Given return attributes with an unmet dependency When routing Then the caller is told which one`() =
        testApplication {
            application { module() }

            val request = request.copy(returnAttributes = listOf(HereReturnAttribute.ACTIONS))

            val response = client.postJson(NavigatorApi.HERE_CAR, HereCarRouteRequest.serializer(), request)

            assertEquals(HttpStatusCode.BadRequest, response.status)
            assertContains(response.decodeError().message, "POLYLINE")
        }

    @Test
    fun `Given an arrival time on a profile that supports it When routing Then the request is accepted`() =
        testApplication {
            application { module() }

            val arriveBy = request.copy(time = RouteTime.ArriveBy(Instant.parse("2026-08-13T09:00:00Z")))

            val response = client.postJson(NavigatorApi.HERE_CAR, HereCarRouteRequest.serializer(), arriveBy)

            assertEquals(HttpStatusCode.OK, response.status)
        }

    @Test
    fun `Given the routing path When it is called with GET Then it is not served`() = testApplication {
        application { module() }

        val response = client.get(NavigatorApi.HERE_CAR)

        assertTrue(
            response.status == HttpStatusCode.MethodNotAllowed || response.status == HttpStatusCode.NotFound,
            "Routing is a POST only path, got ${response.status}",
        )
    }
}
