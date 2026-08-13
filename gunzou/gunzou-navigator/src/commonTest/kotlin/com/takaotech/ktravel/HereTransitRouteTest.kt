package com.takaotech.ktravel

import com.takaotech.navigator.api.NavigatorApi
import com.takaotech.navigator.api.common.GeoPoint
import com.takaotech.navigator.api.common.ProviderProfile
import com.takaotech.navigator.api.common.RouteTime
import com.takaotech.navigator.api.common.TransitMode
import com.takaotech.navigator.api.common.TravelMode
import com.takaotech.navigator.api.error.ErrorCode
import com.takaotech.navigator.api.here.HereTransitModeFilter
import com.takaotech.navigator.api.here.HereTransitRouteRequest
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.testApplication
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.Instant

/**
 * `POST /v1/here/transit` end to end, with a recorded HERE answer behind it.
 *
 * This is the profile that makes the contract worth having. The road API and the transit API share
 * nothing upstream — different host, different parameters, an answer with a shape of its own — and
 * if the single [com.takaotech.navigator.api.response.RouteResponse] could not carry both, the
 * server would be a proxy rather than a facade. The assertions below are what "it carries both"
 * means concretely: a walking leg and a train leg are the same section type, told apart by their
 * mode and by whether the transit details are there.
 */
class HereTransitRouteTest {

    private val request = HereTransitRouteRequest(
        origin = GeoPoint(lat = 44.4949, lng = 11.3426),
        destination = GeoPoint(lat = 44.1601, lng = 10.9739),
    )

    // ---- what reaches HERE -------------------------------------------------------------------

    @Test
    fun `Given a journey request When routing Then the transit host is the one that is called`() = testApplication {
        val here = HereMockServer(HerePayloads.TRANSIT_ROUTE)
        application { module(here.asKoinModule()) }

        client.postJson(NavigatorApi.HERE_TRANSIT, HereTransitRouteRequest.serializer(), request)

        val url = here.requestUrl
        assertEquals("transit.hereapi.com", url.host, "Public transit is a different API, not a parameter")
        assertEquals("44.4949,11.3426", url.query("origin"))
        assertEquals("44.1601,10.9739", url.query("destination"))
    }

    @Test
    fun `Given a departure at an instant When routing Then it reaches HERE unambiguously`() = testApplication {
        val here = HereMockServer(HerePayloads.TRANSIT_ROUTE)
        application { module(here.asKoinModule()) }

        val departAt = request.copy(time = RouteTime.DepartAt(Instant.parse("2026-08-13T07:14:00Z")))

        client.postJson(NavigatorApi.HERE_TRANSIT, HereTransitRouteRequest.serializer(), departAt)

        // A bare local date and time would be read as local at the origin, and a timetable read an
        // hour or two out is a missed train rather than a slightly worse route.
        val sent = here.requestUrl.query("departureTime").orEmpty()
        assertContains(sent, "2026-08-13T07:14:00")
        assertTrue(sent.endsWith("Z") || sent.endsWith("+00:00"), "Ambiguous without an offset: $sent")
    }

    @Test
    fun `Given a mode filter When routing Then the included and excluded kinds both reach HERE`() = testApplication {
        val here = HereMockServer(HerePayloads.TRANSIT_ROUTE)
        application { module(here.asKoinModule()) }

        val filtered = request.copy(
            modes = HereTransitModeFilter(
                include = listOf(TransitMode.REGIONAL_TRAIN, TransitMode.SUBWAY),
                exclude = listOf(TransitMode.FLIGHT),
            ),
        )

        client.postJson(NavigatorApi.HERE_TRANSIT, HereTransitRouteRequest.serializer(), filtered)

        assertEquals("regionalTrain,subway,-flight", here.requestUrl.query("modes"))
    }

    @Test
    fun `Given a filter naming a kind the contract does not know When routing Then it is dropped`() = testApplication {
        val here = HereMockServer(HerePayloads.TRANSIT_ROUTE)
        application { module(here.asKoinModule()) }

        val filtered = request.copy(
            modes = HereTransitModeFilter(include = listOf(TransitMode.BUS, TransitMode.OTHER)),
        )

        client.postJson(NavigatorApi.HERE_TRANSIT, HereTransitRouteRequest.serializer(), filtered)

        assertEquals("bus", here.requestUrl.query("modes"), "OTHER is for reading a response, not for asking")
    }

    @Test
    fun `Given walking preferences When routing Then they reach HERE as its bracketed parameters`() = testApplication {
        val here = HereMockServer(HerePayloads.TRANSIT_ROUTE)
        application { module(here.asKoinModule()) }

        val walking = request.copy(
            pedestrianSpeedMetersPerSecond = 1.2,
            pedestrianMaxDistanceMeters = 1_500,
            changes = 2,
        )

        client.postJson(NavigatorApi.HERE_TRANSIT, HereTransitRouteRequest.serializer(), walking)

        val url = here.requestUrl
        assertEquals("1.2", url.query("pedestrian[speed]"))
        assertEquals("1500", url.query("pedestrian[maxDistance]"))
        assertEquals("2", url.query("changes"))
    }

    // ---- what the caller gets back -------------------------------------------------------------

    @Test
    fun `Given a journey When it is translated Then it is the same response type as a road route`() = testApplication {
        val here = HereMockServer(HerePayloads.TRANSIT_ROUTE)
        application { module(here.asKoinModule()) }

        val response = client.postJson(NavigatorApi.HERE_TRANSIT, HereTransitRouteRequest.serializer(), request)

        assertEquals(HttpStatusCode.OK, response.status)
        val routes = response.decodeRoutes()
        assertEquals(ProviderProfile.TRANSIT, routes.profile)
        assertEquals(2, routes.routes.single().sections.size)
    }

    @Test
    fun `Given a walking leg and a train leg When translated Then one section type carries both`() = testApplication {
        val here = HereMockServer(HerePayloads.TRANSIT_ROUTE)
        application { module(here.asKoinModule()) }

        val sections = client.postJson(NavigatorApi.HERE_TRANSIT, HereTransitRouteRequest.serializer(), request)
            .decodeRoutes().routes.single().sections

        assertEquals(listOf(TravelMode.PEDESTRIAN, TravelMode.TRANSIT), sections.map { it.mode })
        assertNull(sections.first().transit, "Nobody operates a walk")
        assertEquals(TransitMode.REGIONAL_TRAIN, sections.last().transit?.mode)
    }

    @Test
    fun `Given a train leg When translated Then the line the traveller reads comes through`() = testApplication {
        val here = HereMockServer(HerePayloads.TRANSIT_ROUTE)
        application { module(here.asKoinModule()) }

        val transit = client.postJson(NavigatorApi.HERE_TRANSIT, HereTransitRouteRequest.serializer(), request)
            .decodeRoutes().routes.single().sections.last().transit

        assertEquals("R 2841", transit?.name)
        assertEquals("Regionale", transit?.category)
        assertEquals("Porretta Terme", transit?.headsign, "How the two directions of a line are told apart")
        assertEquals("#008C45", transit?.color)
    }

    @Test
    fun `Given intermediate stops were requested When translated Then they come through with their times`() =
        testApplication {
            val here = HereMockServer(HerePayloads.TRANSIT_ROUTE)
            application { module(here.asKoinModule()) }

            val stop = client.postJson(NavigatorApi.HERE_TRANSIT, HereTransitRouteRequest.serializer(), request)
                .decodeRoutes().routes.single().sections.last().transit?.intermediateStops?.single()

            assertEquals("Casalecchio Garibaldi", stop?.name)
            assertEquals(Instant.parse("2026-08-13T07:22:00Z"), stop?.departure?.instant)
        }

    @Test
    fun `Given a departure from a station When translated Then the local offset is kept`() = testApplication {
        val here = HereMockServer(HerePayloads.TRANSIT_ROUTE)
        application { module(here.asKoinModule()) }

        val departure = client.postJson(NavigatorApi.HERE_TRANSIT, HereTransitRouteRequest.serializer(), request)
            .decodeRoutes().routes.single().sections.last().departure

        assertEquals("Bologna Centrale", departure?.name)
        assertEquals(Instant.parse("2026-08-13T07:14:00Z"), departure?.time?.instant)
        assertEquals(2 * 60 * 60, departure?.time?.offsetSeconds, "09:14 on the departure board")
    }

    @Test
    fun `Given a journey When translated Then the summary is the sum of its legs`() = testApplication {
        val here = HereMockServer(HerePayloads.TRANSIT_ROUTE)
        application { module(here.asKoinModule()) }

        val route = client.postJson(NavigatorApi.HERE_TRANSIT, HereTransitRouteRequest.serializer(), request)
            .decodeRoutes().routes.single()

        assertEquals(360 + 1620, route.summary.durationSeconds)
        assertEquals(420 + 58_200, route.summary.distanceMeters)
    }

    // ---- failures ------------------------------------------------------------------------------

    @Test
    fun `Given no journey exists When routing Then it is an error and not an empty list`() = testApplication {
        val here = HereMockServer(HerePayloads.NO_ROUTES)
        application { module(here.asKoinModule()) }

        val response = client.postJson(NavigatorApi.HERE_TRANSIT, HereTransitRouteRequest.serializer(), request)

        assertEquals(HttpStatusCode.UnprocessableEntity, response.status)
        assertEquals(ErrorCode.NO_ROUTE_FOUND, response.decodeError().code)
    }

    @Test
    fun `Given no provider key When routing Then HERE is never called`() = testApplication {
        val here = HereMockServer(HerePayloads.TRANSIT_ROUTE)
        application { module(here.asKoinModule()) }

        val response = client.postJson(
            NavigatorApi.HERE_TRANSIT,
            HereTransitRouteRequest.serializer(),
            request,
            providerKey = null,
        )

        assertEquals(HttpStatusCode.Unauthorized, response.status)
        assertEquals(ErrorCode.MISSING_CREDENTIALS, response.decodeError().code)
        assertEquals(emptyList(), here.requests)
    }

    @Test
    fun `Given a walking speed nobody walks at When routing Then HERE never sees it`() = testApplication {
        val here = HereMockServer(HerePayloads.TRANSIT_ROUTE)
        application { module(here.asKoinModule()) }

        val tooFast = request.copy(pedestrianSpeedMetersPerSecond = 9.0)

        val response = client.postJson(NavigatorApi.HERE_TRANSIT, HereTransitRouteRequest.serializer(), tooFast)

        assertEquals(HttpStatusCode.BadRequest, response.status)
        assertContains(response.decodeError().message, "pedestrianSpeedMetersPerSecond")
        assertEquals(emptyList(), here.requests)
    }

    @Test
    fun `Given more alternatives than transit advertises When routing Then its own limit is enforced`() =
        testApplication {
            val here = HereMockServer(HerePayloads.TRANSIT_ROUTE)
            application { module(here.asKoinModule()) }

            // Six is within what the road profile allows and beyond what this one does: the limits
            // belong to the profile, not to the provider.
            val response = client.postJson(
                NavigatorApi.HERE_TRANSIT,
                HereTransitRouteRequest.serializer(),
                request.copy(alternatives = 6),
            )

            assertEquals(HttpStatusCode.BadRequest, response.status)
            assertEquals(emptyList(), here.requests)
        }
}
