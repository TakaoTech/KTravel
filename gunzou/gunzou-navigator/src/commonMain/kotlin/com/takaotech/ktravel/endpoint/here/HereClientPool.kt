package com.takaotech.ktravel.endpoint.here

import co.touchlab.kermit.Logger
import com.takaotech.navigation.HereClient
import com.takaotech.navigation.common.HereClientConfig
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

private const val DEFAULT_MAX_CLIENTS = 8

/**
 * Keeps one [HereClient] per API key alive, so a key that arrives on every request does not mean a
 * new HTTP client on every request.
 *
 * The pool exists because of how [HereClientConfig] works: the key is baked into the client at
 * construction, appended to each outgoing URL by a plugin installed once. There is no way to send a
 * different key through an existing client, and building one per call would create and tear down a
 * connection pool, a TLS session and an engine thread pool for a single request.
 *
 * Embedded there is exactly one key and the map never grows past one entry. Remote there are as many
 * as there are callers, which is why the pool is bounded: without a ceiling it would be a map keyed
 * by a value a client controls, growing until the process died.
 *
 * Eviction is reference counted rather than immediate. Closing a client that still has a request in
 * flight would fail that request, and it would fail it for a reason — pressure from *other* callers'
 * keys — that has nothing to do with the caller who would see the error. An evicted client is
 * therefore dropped from the map at once, so nothing new reaches it, and closed when its last
 * in flight call returns.
 *
 * @param maxClients How many keys stay resident. The least recently used one is evicted beyond it.
 * @param logger What every client built here logs its HTTP traffic through. Null falls back to the
 *   Kermit singleton, which is right for a process that configured it and wrong for one that runs
 *   this server inside an application with a logger of its own.
 * @param createClient How a client is built. Overridden in tests to supply a mock engine.
 */
class HereClientPool(
    private val maxClients: Int = DEFAULT_MAX_CLIENTS,
    private val logger: Logger? = null,
    private val createClient: (HereClientConfig) -> HereClient = ::HereClient,
) : AutoCloseable {

    private val mutex = Mutex()

    /**
     * Insertion ordered, and re-inserted on each acquisition, so the first entry is the least
     * recently used one.
     */
    private val clients = LinkedHashMap<String, PooledClient>()

    private var closed = false

    /**
     * Runs [block] against the client for [apiKey], keeping it alive for the duration of the call.
     *
     * The client must not be captured beyond [block]: outside it, nothing stops the pool from
     * closing it.
     */
    suspend fun <T> withClient(apiKey: String, block: suspend (HereClient) -> T): T {
        val pooled = acquire(apiKey)

        return try {
            block(pooled.client)
        } finally {
            release(pooled)
        }
    }

    /** Takes a reference on the client for [apiKey], creating and making room for it if needed. */
    private suspend fun acquire(apiKey: String): PooledClient = mutex.withLock {
        check(!closed) { "The HERE client pool is closed" }

        // Removing and re-adding is what moves the entry to the end of the insertion order, which is
        // what makes the first entry the least recently used one.
        val existing = clients.remove(apiKey)
        val pooled = existing ?: PooledClient(
            client = createClient(HereClientConfig(apiKey = apiKey, logger = logger)),
        )
        clients[apiKey] = pooled
        pooled.inUse++

        evictWhileOverCapacity()

        pooled
    }

    /** Drops a reference, closing the client if it was evicted while this call was running. */
    private suspend fun release(pooled: PooledClient) {
        val toClose = mutex.withLock {
            pooled.inUse--
            pooled.takeIf { it.evicted && it.inUse == 0 }
        }

        toClose?.client?.close()
    }

    /**
     * Makes room for the entry that was just acquired.
     *
     * Called with the lock held, and only from [acquire], so the most recently used entry — the one
     * being acquired — is always the last and is never the one evicted.
     */
    private fun evictWhileOverCapacity() {
        while (clients.size > maxClients) {
            // The key is read out before the removal, and the entry is never touched afterward: on
            // Kotlin/Native and Kotlin/Wasm a map entry obtained from the iterator is invalidated by
            // a structural change and throws ConcurrentModificationException when read again.
            val eldestKey = clients.keys.first()
            val eldest = clients.remove(eldestKey) ?: break

            // Nothing is using it, so it can go now. Otherwise it is marked and closed by whichever
            // call releases it last.
            if (eldest.inUse == 0) {
                eldest.client.close()
            } else {
                eldest.evicted = true
            }
        }
    }

    /**
     * Closes every resident client and refuses further acquisitions.
     *
     * Clients still in flight are left to the call that holds them: they were removed from the map,
     * so nothing new can reach them, and the last release closes them.
     *
     * Call it once the engine has stopped accepting requests, which is when Ktor fires
     * `ApplicationStopped`. It cannot take the pool's mutex — [AutoCloseable.close] is not
     * suspending — so it is only safe when no acquisition can still be in progress.
     */
    override fun close() {
        closed = true

        val resident = clients.values.toList()
        clients.clear()
        resident.forEach { pooled ->
            if (pooled.inUse == 0) pooled.client.close() else pooled.evicted = true
        }
    }

    /**
     * A resident client and what the pool knows about its use.
     *
     * [inUse] and [evicted] are only ever read or written under the pool's mutex.
     */
    private class PooledClient(val client: HereClient) {
        var inUse: Int = 0
        var evicted: Boolean = false
    }
}
