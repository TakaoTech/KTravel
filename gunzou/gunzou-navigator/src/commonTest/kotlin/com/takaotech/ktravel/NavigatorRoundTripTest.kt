package com.takaotech.ktravel

import com.takaotech.navigator.api.common.GeoPoint
import com.takaotech.navigator.api.common.ProviderId
import com.takaotech.navigator.api.common.ProviderProfile
import com.takaotech.navigator.api.common.TravelMode
import com.takaotech.navigator.api.error.ErrorCode
import com.takaotech.navigator.api.here.HereCarRouteRequest
import com.takaotech.navigator.api.here.HereTransitRouteRequest
import com.takaotech.navigator.api.response.PolylineEncoding
import com.takaotech.navigator.client.NavigatorClient
import com.takaotech.navigator.client.NavigatorClientConfig
import com.takaotech.navigator.client.NavigatorResult
import com.takaotech.navigator.client.getOrThrow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withContext
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

/**
 * The whole thing, over a real socket.
 *
 * Every other suite stops at a seam: `testApplication` never binds a port, and the client tests
 * answer themselves with a mock engine. What neither covers is everything in between — that the
 * server a client reaches on a loopback port is the one that was started, that the JSON one writes
 * is the JSON the other reads, that a failure survives the trip as a failure. That is exactly what
 * breaks when the two halves of the contract drift apart, and it is invisible to a test of either
 * half on its own.
 *
 * HERE is still replaced, because a test that called it would be testing HERE. Everything between
 * the client and the endpoint is real.
 */
class NavigatorRoundTripTest {

    private val carRequest = HereCarRouteRequest(
        origin = GeoPoint(lat = 44.4949, lng = 11.3426),
        destination = GeoPoint(lat = 43.7696, lng = 11.2558),
    )

    /**
     * Starts a server, points a client at the port it actually bound, and tears both down.
     *
     * On a real dispatcher rather than the virtual clock `runTest` installs: a socket, a request
     * timeout and an engine thread pool are not things virtual time can stand in for.
     */
    private fun roundTrip(
        here: HereMockServer = HereMockServer(HerePayloads.CAR_ROUTE),
        block: suspend (NavigatorClient) -> Unit,
    ) = runTest {
        withContext(Dispatchers.Default) {
            val server = startServerOnFreePort(here.asKoinModule())

            try {
                NavigatorClient(NavigatorClientConfig("http://127.0.0.1:${server.port}")).use { block(it) }
            } finally {
                server.stop()
            }
        }
    }

    @Test
    fun `Given a running navigator When it is asked whether it is alive Then it answers over the socket`() =
        roundTrip { client ->
            assertEquals("ok", client.health().getOrThrow().status)
        }

    @Test
    fun `Given a running navigator When the catalog is fetched Then both profiles arrive as typed values`() =
        roundTrip { client ->
            val profiles = client.profiles().getOrThrow().profiles

            assertEquals(listOf(ProviderProfile.CAR, ProviderProfile.TRANSIT), profiles.map { it.profile })
            assertTrue(profiles.all { it.provider == ProviderId.HERE })
        }

    @Test
    fun `Given a route is asked for over HTTP When it comes back Then it is the contract and not a string`() =
        roundTrip { client ->
            val routes = client.hereCar(carRequest, apiKey = "round-trip-key").getOrThrow()

            assertEquals(ProviderId.HERE, routes.provider)
            val route = routes.routes.single()
            assertEquals(TravelMode.CAR, route.sections.first().mode)
            assertEquals(PolylineEncoding.HERE_FLEXIBLE, route.sections.first().geometry?.encoding)
            assertEquals(2100 + 1800, route.summary.durationSeconds)
        }

    @Test
    fun `Given a journey is asked for over HTTP When it comes back Then the transit details survive the trip`() =
        roundTrip(HereMockServer(HerePayloads.TRANSIT_ROUTE)) { client ->
            val transit = HereTransitRouteRequest(
                origin = GeoPoint(lat = 44.4949, lng = 11.3426),
                destination = GeoPoint(lat = 44.1601, lng = 10.9739),
            )

            val sections = client.hereTransit(transit, apiKey = "round-trip-key")
                .getOrThrow().routes.single().sections

            assertEquals(listOf(TravelMode.PEDESTRIAN, TravelMode.TRANSIT), sections.map { it.mode })
            assertEquals("R 2841", sections.last().transit?.name)
            assertEquals(2 * 60 * 60, sections.last().departure?.time?.offsetSeconds)
        }

    @Test
    fun `Given the key is missing When a route is asked for Then the refusal survives as a refusal`() =
        roundTrip { client ->
            val result = client.hereCar(carRequest, apiKey = null)

            // Not a TransportError: the navigator was reached and it said no. An embedded host that
            // confused the two would restart a healthy server every time a key was unconfigured.
            val failure = assertIs<NavigatorResult.ServerError>(result)
            assertEquals(401, failure.status)
            assertEquals(ErrorCode.MISSING_CREDENTIALS, failure.error.code)
        }

    @Test
    fun `Given a request the profile rejects When it is sent Then the reason arrives readable`() = roundTrip { client ->
        val result = client.hereCar(carRequest.copy(alternatives = 99), apiKey = "round-trip-key")

        val failure = assertIs<NavigatorResult.ServerError>(result)
        assertEquals(ErrorCode.INVALID_REQUEST, failure.error.code)
        assertContains(failure.error.message, "alternatives")
    }

    @Test
    fun `Given the navigator has stopped When it is called Then it is a transport failure and not a refusal`() =
        runTest {
            withContext(Dispatchers.Default) {
                val server = startServerOnFreePort(HereMockServer(HerePayloads.CAR_ROUTE).asKoinModule())
                val client = NavigatorClient(NavigatorClientConfig("http://127.0.0.1:${server.port}"))
                server.stop()

                val result = client.use { it.health() }

                // The case an embedded host recovers from by restarting the server, which is what
                // happens on iOS every time the app comes back to the foreground.
                assertIs<NavigatorResult.TransportError>(result)
            }
        }
}
