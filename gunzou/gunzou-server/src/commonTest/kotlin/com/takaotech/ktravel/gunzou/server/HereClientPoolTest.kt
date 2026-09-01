package com.takaotech.ktravel.gunzou.server

import com.takaotech.ktravel.gunzou.server.endpoint.here.HereClientPool
import com.takaotech.gunzou.here.HereClient
import com.takaotech.gunzou.here.common.HereApiResult
import com.takaotech.gunzou.here.common.HereClientConfig
import com.takaotech.gunzou.here.routing.dto.request.RoutesRequest
import com.takaotech.gunzou.here.routing.dto.request.Waypoint
import com.takaotech.gunzou.here.routing.model.TransportMode
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.async
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotSame
import kotlin.test.assertSame
import kotlin.test.assertTrue

/**
 * The pool is the one piece of the HERE integration with state, and the only one where a bug shows
 * up as an intermittent failure on somebody else's request rather than on the one that caused it.
 */
class HereClientPoolTest {

    private val engine = MockEngine {
        respond(
            content = HerePayloads.CAR_ROUTE,
            status = HttpStatusCode.OK,
            headers = headersOf(HttpHeaders.ContentType, "application/json"),
        )
    }

    private var created = 0

    private fun pool(maxClients: Int = 2) = HereClientPool(
        maxClients = maxClients,
        createClient = { config: HereClientConfig ->
            created++
            HereClient.withEngine(engine, config)
        },
    )

    private suspend fun HereClient.callRouting(): HereApiResult<*> = routing.getRoutes(
        RoutesRequest(
            transportMode = TransportMode.CAR,
            origin = Waypoint(lat = 44.4949, lng = 11.3426),
            destination = Waypoint(lat = 43.7696, lng = 11.2558),
        ),
    )

    @Test
    fun `Given the same key twice When a client is asked for Then the second call reuses the first`() = runTest {
        val pool = pool()

        val first = pool.withClient("key") { it }
        val second = pool.withClient("key") { it }

        assertSame(first, second)
        assertEquals(1, created, "A client per request would build a connection pool per request")
    }

    @Test
    fun `Given two different keys When clients are asked for Then each key gets its own`() = runTest {
        val pool = pool()

        val first = pool.withClient("key-a") { it }
        val second = pool.withClient("key-b") { it }

        assertNotSame(first, second)
        assertEquals(2, created)
    }

    @Test
    fun `Given more keys than the pool holds When one is asked for again Then it was evicted`() = runTest {
        val pool = pool(maxClients = 2)

        pool.withClient("key-a") { }
        pool.withClient("key-b") { }
        pool.withClient("key-c") { }
        // key-a was the least recently used when key-c arrived, so it is gone and has to be rebuilt.
        pool.withClient("key-a") { }

        assertEquals(4, created)
    }

    @Test
    fun `Given a key kept in use When the pool overflows Then the in flight call still completes`() = runTest {
        val pool = pool(maxClients = 1)
        val holding = CompletableDeferred<Unit>()
        val evicted = CompletableDeferred<Unit>()

        val inFlight = async {
            pool.withClient("key-a") { client ->
                holding.complete(Unit)
                evicted.await()
                // Whoever else's key pushed this one out of the pool must not have closed the
                // client this call is holding.
                client.callRouting()
            }
        }

        holding.await()
        pool.withClient("key-b") { }
        evicted.complete(Unit)

        assertTrue(inFlight.await() is HereApiResult.Success)
    }

    @Test
    fun `Given a key was evicted while in use When it is asked for again Then a fresh client is built`() = runTest {
        val pool = pool(maxClients = 1)
        val holding = CompletableDeferred<Unit>()
        val evicted = CompletableDeferred<Unit>()

        val inFlight = async {
            pool.withClient("key-a") { client ->
                holding.complete(Unit)
                evicted.await()
                client
            }
        }

        holding.await()
        pool.withClient("key-b") { }
        evicted.complete(Unit)
        val held = inFlight.await()

        assertNotSame(held, pool.withClient("key-a") { it }, "The evicted client is not handed out again")
    }

    @Test
    fun `Given the pool is closed When a client is asked for Then it refuses instead of leaking one`() = runTest {
        val pool = pool()
        pool.withClient("key") { }

        pool.close()

        val failure = runCatching { pool.withClient("key") { } }
        assertTrue(failure.isFailure, "A pool that kept serving after shutdown would keep the process alive")
    }
}
