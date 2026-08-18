package com.takaotech.ktravel

import com.takaotech.navigator.api.NavigatorApi
import com.takaotech.navigator.api.error.ErrorCode
import com.takaotech.navigator.api.here.HereRoutingRequest
import com.takaotech.navigator.api.here.HereTransportMode
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
// AUTH DISABLED: import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.server.testing.ApplicationTestBuilder
import io.ktor.server.testing.testApplication
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * The same server, configured as a deployment rather than as something inside an app.
 *
 * Nothing here is a different code path — that is the property being checked. The embedded server is
 * not a reduced build that skips its checks; it is this build with an empty configuration, so a bug
 * in the throttled path is a bug in the path everyone runs, and these tests reach it.
 *
 * AUTH DISABLED: the "who may call" section is commented out below. It exercises
 * `NavigatorServerConfig.accessTokens`, which no longer exists, so `@Ignore` would not compile.
 */
class DeployedServerTest {

    private val routingRequest = HereRoutingRequest(
        origin = com.takaotech.navigator.api.common.GeoPoint(lat = 44.4949, lng = 11.3426),
        destination = com.takaotech.navigator.api.common.GeoPoint(lat = 43.7696, lng = 11.2558),
    )

    /** Posts a route request, presenting whichever credentials the test is about. */
    private suspend fun ApplicationTestBuilder.requestRoute(
        // AUTH DISABLED: accessToken: String? = null,
        providerKey: String? = null,
    ): HttpResponse = client.post(NavigatorApi.hereRouting(HereTransportMode.CAR)) {
        contentType(ContentType.Application.Json)
        // AUTH DISABLED: accessToken?.let { header(HttpHeaders.Authorization, "Bearer $it") }
        providerKey?.let { header(NavigatorApi.PROVIDER_KEY_HEADER, it) }
        setBody(
            com.takaotech.navigator.api.NavigatorJson.encodeToString(HereRoutingRequest.serializer(), routingRequest),
        )
    }

    // ---- who may call ---------------------------------------------------------------------------

    // AUTH DISABLED: every test in this section configured a deployment with access tokens.
    // @Test
    // fun `Given a deployment with access tokens When one is not presented Then the call is refused`() =
    //     testApplication {
    //         val here = HereMockServer(HerePayloads.CAR_ROUTE)
    //         application {
    //             module(NavigatorServerConfig(accessTokens = setOf("a-token")), here.asKoinModule())
    //         }
    //
    //         val response = requestRoute(providerKey = "caller-key")
    //
    //         assertEquals(HttpStatusCode.Unauthorized, response.status)
    //         assertEquals(ErrorCode.UNAUTHENTICATED, response.decodeError().code)
    //         assertEquals(emptyList(), here.requests, "An unknown caller costs nothing upstream")
    //     }
    //
    // @Test
    // fun `Given a deployment with access tokens When an unknown one is presented Then the call is refused`() =
    //     testApplication {
    //         val here = HereMockServer(HerePayloads.CAR_ROUTE)
    //         application {
    //             module(NavigatorServerConfig(accessTokens = setOf("a-token")), here.asKoinModule())
    //         }
    //
    //         val response = requestRoute(accessToken = "someone-elses-token", providerKey = "caller-key")
    //
    //         assertEquals(HttpStatusCode.Unauthorized, response.status)
    //         assertEquals(ErrorCode.UNAUTHENTICATED, response.decodeError().code)
    //     }
    //
    // @Test
    // fun `Given a deployment with access tokens When a known one is presented Then the route is computed`() =
    //     testApplication {
    //         val here = HereMockServer(HerePayloads.CAR_ROUTE)
    //         application {
    //             module(NavigatorServerConfig(accessTokens = setOf("a-token", "another")), here.asKoinModule())
    //         }
    //
    //         val response = requestRoute(accessToken = "another", providerKey = "caller-key")
    //
    //         assertEquals(HttpStatusCode.OK, response.status)
    //     }
    //
    // // A probe that has to hold a token is a probe that reports an outage the server does not have,
    // // and the endpoint carries nothing worth protecting.
    // @Test
    // fun `Given a deployment with access tokens When health is probed without one Then it still answers`() =
    //     testApplication {
    //         application {
    //             module(
    //                 NavigatorServerConfig(accessTokens = setOf("a-token")),
    //                 HereMockServer(HerePayloads.CAR_ROUTE).asKoinModule(),
    //             )
    //         }
    //
    //         assertEquals(HttpStatusCode.OK, client.get(NavigatorApi.HEALTH).status)
    //         assertEquals(HttpStatusCode.OK, client.get(NavigatorApi.PROFILES).status)
    //     }

    // What is left of that section: nobody has to say who they are, on any deployment.
    @Test
    fun `Given a server with authentication disabled When no token is presented Then nothing is required`() =
        testApplication {
            val here = HereMockServer(HerePayloads.CAR_ROUTE)
            application { module(NavigatorServerConfig.EMBEDDED, here.asKoinModule()) }

            assertEquals(HttpStatusCode.OK, requestRoute(providerKey = "caller-key").status)
        }

    @Test
    fun `Given the discovery paths When they are probed Then they answer without any credential`() = testApplication {
        application {
            module(NavigatorServerConfig.EMBEDDED, HereMockServer(HerePayloads.CAR_ROUTE).asKoinModule())
        }

        assertEquals(HttpStatusCode.OK, client.get(NavigatorApi.HEALTH).status)
        assertEquals(HttpStatusCode.OK, client.get(NavigatorApi.PROFILES).status)
    }

    // ---- whose provider key ---------------------------------------------------------------------

    @Test
    fun `Given the deployment holds a provider key When the caller sends none Then the server key is used`() =
        testApplication {
            val here = HereMockServer(HerePayloads.CAR_ROUTE)
            application {
                module(NavigatorServerConfig(providerApiKey = "server-held-key"), here.asKoinModule())
            }

            val response = requestRoute()

            assertEquals(HttpStatusCode.OK, response.status)
            // The point of holding one: an app can talk to a remote navigator without a provider key
            // living on every device and crossing the network on every request.
            assertEquals("server-held-key", here.requestUrl.query("apiKey"))
        }

    @Test
    fun `Given both hold a provider key When a route is asked for Then the caller's own wins`() = testApplication {
        val here = HereMockServer(HerePayloads.CAR_ROUTE)
        application {
            module(NavigatorServerConfig(providerApiKey = "server-held-key"), here.asKoinModule())
        }

        requestRoute(providerKey = "caller-key")

        // The embedded case, unchanged: a caller that brought a key is billed against their own.
        assertEquals("caller-key", here.requestUrl.query("apiKey"))
    }

    @Test
    fun `Given neither holds a provider key When a route is asked for Then it is refused before any call`() =
        testApplication {
            val here = HereMockServer(HerePayloads.CAR_ROUTE)
            application { module(NavigatorServerConfig.EMBEDDED, here.asKoinModule()) }

            val response = requestRoute()

            assertEquals(HttpStatusCode.Unauthorized, response.status)
            assertEquals(ErrorCode.MISSING_CREDENTIALS, response.decodeError().code)
            assertEquals(emptyList(), here.requests)
        }

    // ---- how much any one caller gets -----------------------------------------------------------

    @Test
    fun `Given a rate limit When a caller exceeds it Then further requests are refused`() = testApplication {
        val here = HereMockServer(HerePayloads.CAR_ROUTE)
        application {
            module(
                NavigatorServerConfig(
                    providerApiKey = "server-held-key",
                    rateLimit = NavigatorRateLimit(requests = 2, refillPeriodSeconds = 60),
                ),
                here.asKoinModule(),
            )
        }

        val responses = List(4) { requestRoute() }

        assertEquals(listOf(HttpStatusCode.OK, HttpStatusCode.OK), responses.take(2).map { it.status })
        assertTrue(
            responses.drop(2).all { it.status == HttpStatusCode.TooManyRequests },
            "Got ${responses.map { it.status }}",
        )
        assertEquals(2, here.requests.size, "A throttled request never reaches the provider")

        // The plugin refuses with a bare 429. Without a body the client reads it as an unparseable
        // failure and loses the one thing that would tell it to back off instead of retrying.
        assertEquals(ErrorCode.PROVIDER_RATE_LIMITED, responses.last().decodeError().code)
    }

    @Test
    fun `Given a rate limit When health is probed repeatedly Then it is never throttled`() = testApplication {
        application {
            module(
                NavigatorServerConfig(rateLimit = NavigatorRateLimit(requests = 1, refillPeriodSeconds = 60)),
                HereMockServer(HerePayloads.CAR_ROUTE).asKoinModule(),
            )
        }

        val statuses = List(5) { client.get(NavigatorApi.HEALTH).status }

        assertTrue(statuses.all { it == HttpStatusCode.OK }, "A load balancer polls this: $statuses")
    }

    // AUTH DISABLED: two callers are no longer distinguishable. The limit is keyed by remote host,
    // and every client of a `testApplication` shares one, so this property cannot be expressed here
    // any more — it is kept as it was rather than rewritten into something weaker.
    // @Test
    // fun `Given a rate limit and two callers When one exhausts it Then the other is unaffected`() =
    //     testApplication {
    //         val here = HereMockServer(HerePayloads.CAR_ROUTE)
    //         application {
    //             module(
    //                 NavigatorServerConfig(
    //                     accessTokens = setOf("noisy", "quiet"),
    //                     providerApiKey = "server-held-key",
    //                     rateLimit = NavigatorRateLimit(requests = 1, refillPeriodSeconds = 60),
    //                 ),
    //                 here.asKoinModule(),
    //             )
    //         }
    //
    //         assertEquals(HttpStatusCode.OK, requestRoute(accessToken = "noisy").status)
    //         assertEquals(HttpStatusCode.TooManyRequests, requestRoute(accessToken = "noisy").status)
    //
    //         // The failure a shared bucket would produce: one client in a retry loop locking out
    //         // everybody else, which is the thing the limit exists to prevent.
    //         assertEquals(HttpStatusCode.OK, requestRoute(accessToken = "quiet").status)
    //     }

    @Test
    fun `Given an embedded server When it is called repeatedly Then nothing is throttled`() = testApplication {
        val here = HereMockServer(HerePayloads.CAR_ROUTE)
        application { module(NavigatorServerConfig.EMBEDDED, here.asKoinModule()) }

        val statuses = List(20) { requestRoute(providerKey = "caller-key").status }

        assertTrue(statuses.all { it == HttpStatusCode.OK }, "One process is not a crowd: $statuses")
    }
}
