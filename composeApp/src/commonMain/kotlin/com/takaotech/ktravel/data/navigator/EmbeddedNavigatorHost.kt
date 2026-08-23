package com.takaotech.ktravel.data.navigator

import co.touchlab.kermit.Logger
import com.takaotech.ktravel.LOOPBACK_HOST
import com.takaotech.ktravel.RunningServer
import com.takaotech.ktravel.core.annotation.OpenForMokkery
import com.takaotech.ktravel.di.AppScope
import com.takaotech.ktravel.startServerOnFreePort
import dev.zacsweers.metro.SingleIn
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Runs gunzo-navigator inside this process, and says where it is.
 *
 * One per application: the server holds a socket, and a second one would be a second port for no
 * reason. Started lazily, on the first call to [baseUrl], because an app that never plans a route
 * should not be paying for an HTTP server.
 *
 * Two things it has to get right, and both are about being called from more than one place at once:
 *
 * - **Starting once.** Two screens asking for a route at the same moment must not race into two
 *   servers. The attempt is published as a [CompletableDeferred] under a mutex, so the second caller
 *   awaits the first one's instead of beginning another.
 * - **Coming back from the dead.** On iOS the socket does not survive suspension: the app returns to
 *   the foreground and the port that worked before is refused. [restart] is how a caller recovers,
 *   and it is why the base URL is resolved per request rather than captured once — the new server
 *   binds a different port, and callers have to follow it without being rebuilt.
 *
 * The logger is passed in rather than injected into this constructor, and it is nullable, for one
 * reason: tests replace the host with a mock, and a mock has to be able to build one without a
 * logger to give it. The application always gives it one — see `AppGraph.provideEmbeddedNavigatorHost`
 * — and null only ever happens in a test that starts a real server to look at a real port.
 *
 * @param logger The application's logger, handed to the server so it writes where the rest of the
 *   application writes. Null lets the server configure logging for itself, at a severity nobody
 *   chose, which is why the application never passes null.
 */
@SingleIn(AppScope::class)
@OpenForMokkery
class EmbeddedNavigatorHost(private val logger: Logger? = null) {

    private val mutex = Mutex()

    /**
     * The running server, or the attempt to start one.
     *
     * A deferred and not the server itself, so a caller arriving while a start is still in flight
     * waits for that one. Cleared when a start fails, so the next call tries again rather than
     * handing out the same failure for the life of the process.
     */
    private var running: CompletableDeferred<RunningServer>? = null

    /**
     * Where the embedded server is, starting it if it is not up yet.
     *
     * Suspends only on the first call, and on the first after a [restart] or a [stop].
     */
    suspend fun baseUrl(): String = "http://$LOOPBACK_HOST:${server().port}"

    /**
     * Stops whatever is running and starts again on a new port.
     *
     * The answer to a connection that was refused: on iOS the server does not outlive a suspension,
     * and there is nothing to be learned by asking the old port a second time.
     */
    suspend fun restart(): String {
        stop()

        return baseUrl()
    }

    /** Stops the server. It starts again on the next call to [baseUrl]. */
    suspend fun stop() {
        val pending = mutex.withLock { running.also { running = null } } ?: return

        runCatching { pending.await().stop() }
    }

    private suspend fun server(): RunningServer {
        val pending: CompletableDeferred<RunningServer>
        val startHere: Boolean

        mutex.withLock {
            val existing = running
            startHere = existing == null
            pending = existing ?: CompletableDeferred<RunningServer>().also { running = it }
        }

        if (startHere) {
            pending.startServer()
        }

        return pending.await()
    }

    /**
     * Starts a server into this deferred, forgetting the attempt if it fails.
     *
     * The failure is not thrown from here: it is completed into the deferred, so the caller that
     * started it and every caller waiting on it see the same thing from [CompletableDeferred.await].
     */
    private suspend fun CompletableDeferred<RunningServer>.startServer() {
        try {
            complete(startServerOnFreePort(logger))
        } catch (cancellation: CancellationException) {
            forget()
            cancel(cancellation)
            throw cancellation
        } catch (@Suppress("TooGenericExceptionCaught") failure: Throwable) {
            forget()
            completeExceptionally(failure)
        }
    }

    /** Drops this attempt, unless another one has already replaced it. */
    private suspend fun CompletableDeferred<RunningServer>.forget() {
        mutex.withLock { if (running === this@forget) running = null }
    }
}
