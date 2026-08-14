package com.takaotech.navigator.client

import com.takaotech.navigator.api.NavigatorApi
import com.takaotech.navigator.api.NavigatorJson
import com.takaotech.navigator.api.common.GeoPoint
import com.takaotech.navigator.api.common.ProviderId
import com.takaotech.navigator.api.common.ProviderProfile
import com.takaotech.navigator.api.error.ErrorCode
import com.takaotech.navigator.api.error.ErrorResponse
import com.takaotech.navigator.api.here.HereCarRouteRequest
import com.takaotech.navigator.api.here.HereTransitRouteRequest
import com.takaotech.navigator.api.response.RouteResponse
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.engine.mock.respondError
import io.ktor.client.request.HttpRequestData
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.utils.io.errors.IOException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withContext
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull

/**
 * The client, against a navigator that is not really there.
 *
 * What matters here is the seam, not the routing: that the right method reaches the right path, that
 * the caller's key is presented as a header and never in a body or a query, and above all that the
 * three outcomes stay three. Flattening "the navigator said no" into "I could not reach it" would
 * make an embedded host restart a perfectly healthy server every time a key was wrong.
 */
class NavigatorClientTest {

    private val carRequest = HereCarRouteRequest(
        origin = GeoPoint(lat = 44.4949, lng = 11.3426),
        destination = GeoPoint(lat = 43.7696, lng = 11.2558),
    )

    private val routeResponse = RouteResponse(provider = ProviderId.HERE, profile = ProviderProfile.CAR)

    private val recorded = mutableListOf<HttpRequestData>()

    private fun client(
        status: HttpStatusCode = HttpStatusCode.OK,
        body: String = NavigatorJson.encodeToString(RouteResponse.serializer(), routeResponse),
        baseUrl: NavigatorBaseUrl = NavigatorBaseUrl { "http://navigator.test" },
    ): NavigatorClient {
        val engine = MockEngine { request ->
            recorded += request
            respond(content = body, status = status, headers = headersOf(HttpHeaders.ContentType, "application/json"))
        }

        return NavigatorClient.withEngine(engine, NavigatorClientConfig(baseUrl = baseUrl))
    }

    /**
     * Runs on a real dispatcher rather than on the virtual clock `runTest` installs.
     *
     * The client installs `HttpTimeout`, whose killer coroutine is a `delay`. Under virtual time
     * that delay is resolved as soon as the scheduler runs out of other work, so every call races
     * its own timeout and the slower ones lose — a failure that says nothing about the client and
     * everything about the clock.
     */
    private fun clientTest(block: suspend () -> Unit) = runTest {
        withContext(Dispatchers.Default) { block() }
    }

    @Test
    fun `Given a road request When it is sent Then it goes to the road path as a POST`() = clientTest {
        client().use { it.hereCar(carRequest) }

        val sent = recorded.single()
        assertEquals(HttpMethod.Post, sent.method)
        assertEquals("http://navigator.test${NavigatorApi.HERE_CAR}", sent.url.toString())
    }

    @Test
    fun `Given a journey request When it is sent Then it goes to the transit path`() = clientTest {
        val transit = HereTransitRouteRequest(
            origin = GeoPoint(lat = 44.4949, lng = 11.3426),
            destination = GeoPoint(lat = 44.5058, lng = 11.3428),
        )

        client().use { it.hereTransit(transit) }

        assertEquals("http://navigator.test${NavigatorApi.HERE_TRANSIT}", recorded.single().url.toString())
    }

    @Test
    fun `Given the catalog is asked for When it is sent Then it is a GET on the profiles path`() = clientTest {
        client(body = """{ "profiles": [] }""").use { it.profiles() }

        val sent = recorded.single()
        assertEquals(HttpMethod.Get, sent.method)
        assertEquals("http://navigator.test${NavigatorApi.PROFILES}", sent.url.toString())
    }

    @Test
    fun `Given a provider key When a route is asked for Then it travels in the header and nowhere else`() = clientTest {
        client().use { it.hereCar(carRequest, apiKey = "caller-key") }

        val sent = recorded.single()
        assertEquals("caller-key", sent.headers[NavigatorApi.PROVIDER_KEY_HEADER])
        assertNull(sent.url.parameters["apiKey"], "A key in a query string ends up in every access log")
    }

    @Test
    fun `Given an access token When a call is made Then it is presented as a bearer token`() = clientTest {
        val engine = MockEngine { request ->
            recorded += request
            respond("{}", HttpStatusCode.OK, headersOf(HttpHeaders.ContentType, "application/json"))
        }
        val config = NavigatorClientConfig(baseUrl = "http://navigator.test", accessToken = "an-access-token")

        NavigatorClient.withEngine(engine, config).use { it.profiles() }

        assertEquals("Bearer an-access-token", recorded.single().headers[HttpHeaders.Authorization])
    }

    @Test
    fun `Given an access token and a provider key When routing Then the two travel separately`() = clientTest {
        val engine = MockEngine { request ->
            recorded += request
            respond(
                NavigatorJson.encodeToString(RouteResponse.serializer(), routeResponse),
                HttpStatusCode.OK,
                headersOf(HttpHeaders.ContentType, "application/json"),
            )
        }
        val config = NavigatorClientConfig(baseUrl = "http://navigator.test", accessToken = "an-access-token")

        NavigatorClient.withEngine(engine, config).use { it.hereCar(carRequest, apiKey = "provider-key") }

        // Different questions: who is allowed to ask, and which routing account to bill.
        val sent = recorded.single()
        assertEquals("Bearer an-access-token", sent.headers[HttpHeaders.Authorization])
        assertEquals("provider-key", sent.headers[NavigatorApi.PROVIDER_KEY_HEADER])
    }

    @Test
    fun `Given no access token When a call is made Then no authorization header is sent`() = clientTest {
        client().use { it.hereCar(carRequest) }

        assertNull(recorded.single().headers[HttpHeaders.Authorization])
    }

    @Test
    fun `Given no provider key When a route is asked for Then no key header is sent at all`() = clientTest {
        client().use { it.hereCar(carRequest) }

        assertNull(recorded.single().headers[NavigatorApi.PROVIDER_KEY_HEADER])
    }

    @Test
    fun `Given the navigator answers a route When it is read Then the contract type comes back`() = clientTest {
        val result = client().use { it.hereCar(carRequest) }

        assertIs<NavigatorResult.Success<RouteResponse>>(result)
        assertEquals(ProviderId.HERE, result.value.provider)
    }

    @Test
    fun `Given the navigator refuses When it is read Then the failure is its own and not the transport`() = clientTest {
        val error = ErrorResponse(code = ErrorCode.PROVIDER_UNAUTHORIZED, message = "The key was rejected")

        val result = client(
            status = HttpStatusCode.Unauthorized,
            body = NavigatorJson.encodeToString(ErrorResponse.serializer(), error),
        ).use { it.hereCar(carRequest) }

        val failure = assertIs<NavigatorResult.ServerError>(result)
        assertEquals(401, failure.status)
        assertEquals(ErrorCode.PROVIDER_UNAUTHORIZED, failure.error.code)
    }

    @Test
    fun `Given a failing status with a body that is not the contract When read Then it is still a server error`() =
        clientTest {
            val result = client(status = HttpStatusCode.BadGateway, body = "<html>gateway timeout</html>")
                .use { it.hereCar(carRequest) }

            val failure = assertIs<NavigatorResult.ServerError>(result)
            assertEquals(ErrorCode.INTERNAL, failure.error.code)
            assertContains(failure.error.message, "not a navigator error")
        }

    @Test
    fun `Given the navigator cannot be reached When routing Then it is a transport failure`() = clientTest {
        val engine = MockEngine { throw IOException("Connection refused") }
        val client = NavigatorClient.withEngine(engine, NavigatorClientConfig("http://127.0.0.1:1"))

        val result = client.use { it.hereCar(carRequest) }

        // The distinction an embedded host acts on: this one means restart the server, a
        // ServerError never does.
        assertIs<NavigatorResult.TransportError>(result)
    }

    @Test
    fun `Given the base URL changes between calls When two are made Then each goes where it was told`() = clientTest {
        var port = 1000
        val moving = NavigatorBaseUrl { "http://127.0.0.1:${port++}" }

        client(baseUrl = moving).use {
            it.health()
            it.health()
        }

        assertEquals(
            listOf("http://127.0.0.1:1000/v1/health", "http://127.0.0.1:1001/v1/health"),
            recorded.map { it.url.toString() },
            "A server restarted on a new port has to be followed without rebuilding the client",
        )
    }

    // ---- one client, two navigators ---------------------------------------------------------------

    @Test
    fun `Given a target When a call names it Then the request goes there and not to the configured one`() = clientTest {
        val target = NavigatorTarget(baseUrl = "https://remote.test")

        client().use { it.profiles(target) }

        assertEquals("https://remote.test${NavigatorApi.PROFILES}", recorded.single().url.toString())
    }

    @Test
    fun `Given a target with a trailing slash When a call names it Then the path is not doubled up`() = clientTest {
        client().use { it.health(NavigatorTarget(baseUrl = "https://remote.test/")) }

        assertEquals("https://remote.test${NavigatorApi.HEALTH}", recorded.single().url.toString())
    }

    @Test
    fun `Given a target with a token When a route is asked for Then that token is presented`() = clientTest {
        val config = NavigatorClientConfig(baseUrl = "http://navigator.test", accessToken = "configured-token")
        val engine = MockEngine { request ->
            recorded += request
            respond(
                NavigatorJson.encodeToString(RouteResponse.serializer(), routeResponse),
                HttpStatusCode.OK,
                headersOf(HttpHeaders.ContentType, "application/json"),
            )
        }

        NavigatorClient.withEngine(engine, config).use {
            it.hereCar(carRequest, apiKey = "provider-key", target = NavigatorTarget("https://remote.test", "its-own"))
        }

        val sent = recorded.single()
        assertEquals("Bearer its-own", sent.headers[HttpHeaders.Authorization])
        // The provider key is the caller's, not the deployment's, so it follows the caller everywhere.
        assertEquals("provider-key", sent.headers[NavigatorApi.PROVIDER_KEY_HEADER])
    }

    @Test
    fun `Given a target without a token When a call names it Then the configured token is not sent there`() =
        clientTest {
            val config = NavigatorClientConfig(baseUrl = "http://navigator.test", accessToken = "configured-token")
            val engine = MockEngine { request ->
                recorded += request
                respond("{}", HttpStatusCode.OK, headersOf(HttpHeaders.ContentType, "application/json"))
            }

            NavigatorClient.withEngine(engine, config).use { it.profiles(NavigatorTarget("https://elsewhere.test")) }

            // A credential issued by one deployment must not reach another host just because the
            // caller asked that host a question.
            assertNull(recorded.single().headers[HttpHeaders.Authorization])
        }

    @Test
    fun `Given no target When a call is made Then the configured navigator still answers it`() = clientTest {
        val config = NavigatorClientConfig(baseUrl = "http://navigator.test", accessToken = "configured-token")
        val engine = MockEngine { request ->
            recorded += request
            respond("{}", HttpStatusCode.OK, headersOf(HttpHeaders.ContentType, "application/json"))
        }

        NavigatorClient.withEngine(engine, config).use { it.profiles() }

        val sent = recorded.single()
        assertEquals("http://navigator.test${NavigatorApi.PROFILES}", sent.url.toString())
        assertEquals("Bearer configured-token", sent.headers[HttpHeaders.Authorization])
    }

    @Test
    fun `Given two navigators When both are asked in turn Then one client serves both`() = clientTest {
        val embedded = NavigatorTarget("http://127.0.0.1:54213")
        val remote = NavigatorTarget("https://remote.test", "a-token")

        client(body = """{ "profiles": [] }""").use {
            it.profiles(embedded)
            it.profiles(remote)
        }

        // The case the parameter exists for: a screen offering a choice between the two has to ask
        // both what they serve, and building a second client for it would cost a second pool.
        assertEquals(
            listOf("http://127.0.0.1:54213/v1/profiles", "https://remote.test/v1/profiles"),
            recorded.map { it.url.toString() },
        )
        assertNull(recorded.first().headers[HttpHeaders.Authorization])
        assertEquals("Bearer a-token", recorded.last().headers[HttpHeaders.Authorization])
    }

    @Test
    fun `Given a base URL with a trailing slash When a call is made Then the path is not doubled up`() = clientTest {
        val engine = MockEngine { request ->
            recorded += request
            respondError(HttpStatusCode.NotFound)
        }
        val client = NavigatorClient.withEngine(engine, NavigatorClientConfig("http://navigator.test/"))

        client.use { it.health() }

        assertEquals("http://navigator.test${NavigatorApi.HEALTH}", recorded.single().url.toString())
    }
}
