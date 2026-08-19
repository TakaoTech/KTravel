package com.takaotech.ktravel

import com.takaotech.navigator.api.NavigatorApi
import com.takaotech.navigator.api.common.GeoPoint
import com.takaotech.navigator.api.common.ProviderProfile
import com.takaotech.navigator.api.common.RouteTime
import com.takaotech.navigator.api.common.TransitMode
import com.takaotech.navigator.api.error.ErrorCode
import com.takaotech.navigator.api.here.HereTransitModeFilter
import com.takaotech.navigator.api.here.HereTransitRouteRequest
import com.takaotech.navigator.api.response.TransitJourneyLeg
import com.takaotech.navigator.api.response.TransitJourneyResponse
import com.takaotech.navigator.api.response.WheelchairAccess
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.testApplication
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue
import kotlin.time.Instant

/**
 * `POST /v1/here/transit` end to end, with a recorded HERE answer behind it.
 *
 * The road API and the transit API share nothing upstream — different host, different parameters, an
 * answer with a shape of its own — and the contract now says so on both halves. What these tests
 * hold onto is the translation: that a section HERE marks `pedestrian` becomes a
 * [TransitJourneyLeg.Walk] and everything else a [TransitJourneyLeg.Ride], and that everything the
 * traveller reads off a ride — the line, the operator, the stops it calls at — survives it.
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
    fun `Given a journey When it is translated Then it answers as a journey and not as a route`() = testApplication {
        val here = HereMockServer(HerePayloads.TRANSIT_ROUTE)
        application { module(here.asKoinModule()) }

        val response = client.postJson(NavigatorApi.HERE_TRANSIT, HereTransitRouteRequest.serializer(), request)

        assertEquals(HttpStatusCode.OK, response.status)
        val journeys = response.decodeJourneys()
        assertEquals(ProviderProfile.TRANSIT, journeys.profile)
        assertEquals(2, journeys.journeys.single().legs.size)
    }

    @Test
    fun `Given a walking leg and a train leg When translated Then each becomes the kind of leg it is`() =
        testApplication {
            val here = HereMockServer(HerePayloads.TRANSIT_ROUTE)
            application { module(here.asKoinModule()) }

            val legs = client.postJson(NavigatorApi.HERE_TRANSIT, HereTransitRouteRequest.serializer(), request)
                .decodeJourneys().journeys.single().legs

            assertIs<TransitJourneyLeg.Walk>(legs.first(), "Nobody operates a walk")
            assertEquals(TransitMode.REGIONAL_TRAIN, assertIs<TransitJourneyLeg.Ride>(legs.last()).line.mode)
        }

    @Test
    fun `Given a train leg When translated Then the line the traveller reads comes through`() = testApplication {
        val here = HereMockServer(HerePayloads.TRANSIT_ROUTE)
        application { module(here.asKoinModule()) }

        val line = client.postJson(NavigatorApi.HERE_TRANSIT, HereTransitRouteRequest.serializer(), request)
            .decodeJourneys().rideLeg().line

        assertEquals("R 2841", line.name)
        assertEquals("Regionale", line.category)
        assertEquals("Porretta Terme", line.headsign, "How the two directions of a line are told apart")
        assertEquals("#008C45", line.color)
        assertEquals(WheelchairAccess.LIMITED, line.wheelchairAccessible)
    }

    @Test
    fun `Given a service with a named operator When translated Then the agency reaches the answer`() = testApplication {
        val here = HereMockServer(HerePayloads.TRANSIT_ROUTE)
        application { module(here.asKoinModule()) }

        val agency = client.postJson(NavigatorApi.HERE_TRANSIT, HereTransitRouteRequest.serializer(), request)
            .decodeJourneys().rideLeg().agency

        assertEquals("Trenitalia", agency?.name, "Who to ask about a disruption")
        assertEquals("agency:trenitalia", agency?.id)
        assertEquals("https://example.test/trenitalia", agency?.website)
    }

    @Test
    fun `Given intermediate stops were requested When translated Then they keep their place on the line`() =
        testApplication {
            val here = HereMockServer(HerePayloads.TRANSIT_ROUTE)
            application { module(here.asKoinModule()) }

            val stop = client.postJson(NavigatorApi.HERE_TRANSIT, HereTransitRouteRequest.serializer(), request)
                .decodeJourneys().rideLeg().intermediateStops.single()

            assertEquals("Casalecchio Garibaldi", stop.name)
            assertEquals(Instant.parse("2026-08-13T07:22:00Z"), stop.departure?.instant)
            assertEquals(60, stop.dwellSeconds, "How long the train stands there")
            assertEquals(23, stop.offset, "Where a marker goes without geocoding the stop again")
        }

    @Test
    fun `Given a boarding stop When translated Then the local time and the station details are kept`() =
        testApplication {
            val here = HereMockServer(HerePayloads.TRANSIT_ROUTE)
            application { module(here.asKoinModule()) }

            val boarding = client.postJson(NavigatorApi.HERE_TRANSIT, HereTransitRouteRequest.serializer(), request)
                .decodeJourneys().rideLeg().boarding

            assertEquals("Bologna Centrale", boarding?.name)
            assertEquals(Instant.parse("2026-08-13T07:14:00Z"), boarding?.departure?.instant)
            assertEquals(2 * 60 * 60, boarding?.departure?.offsetSeconds, "09:14 on the departure board")
            assertEquals(WheelchairAccess.YES, boarding?.wheelchairAccessible)
            assertEquals("https://example.test/stations/bologna-centrale", boarding?.url)
        }

    @Test
    fun `Given a journey When translated Then the summary is the sum of its legs`() = testApplication {
        val here = HereMockServer(HerePayloads.TRANSIT_ROUTE)
        application { module(here.asKoinModule()) }

        val journey = client.postJson(NavigatorApi.HERE_TRANSIT, HereTransitRouteRequest.serializer(), request)
            .decodeJourneys().journeys.single()

        assertEquals(360 + 1620, journey.summary.durationSeconds)
        assertEquals(420 + 58_200, journey.summary.distanceMeters)
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

/**
 * The one ride of the recorded journey.
 *
 * A helper rather than a chain repeated in every assertion, because the cast is the interesting part
 * and it should fail in one place with one message when the translation stops producing a ride.
 */
private fun TransitJourneyResponse.rideLeg(): TransitJourneyLeg.Ride = assertIs(journeys.single().legs.last())
