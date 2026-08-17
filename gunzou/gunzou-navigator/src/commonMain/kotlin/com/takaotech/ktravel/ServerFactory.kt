package com.takaotech.ktravel

import co.touchlab.kermit.Logger
import io.ktor.server.cio.CIO
import io.ktor.server.cio.CIOApplicationEngine
import io.ktor.server.engine.EmbeddedServer
import io.ktor.server.engine.embeddedServer
import org.koin.core.module.Module

/**
 * Asks the operating system for a free port at bind time.
 *
 * The embedded server runs inside the host application on Android and iOS, where no fixed port can
 * be reserved: another process may already hold it, and two instances could never coexist.
 */
const val EPHEMERAL_PORT: Int = 0

/**
 * A started server together with the port the operating system actually assigned to it.
 *
 * @property server the running engine, for callers that need more than [stop].
 * @property port the port the server is listening on, read back from the bound socket.
 */
class RunningServer internal constructor(
    val server: EmbeddedServer<CIOApplicationEngine, CIOApplicationEngine.Configuration>,
    val port: Int,
) {
    /** Stops the server, so hosts that never touch the Ktor types can shut it down. */
    fun stop() = server.stop()
}

/**
 * Starts the server on every target.
 *
 * The JVM entry point goes through `EngineMain` and `application.conf` instead, which resolves its
 * modules by name through reflection and therefore cannot work on Android or iOS.
 *
 * With the default port the assigned one only shows up in Ktor's own startup log
 * (`Responding at http://0.0.0.0:<port>`). Callers that need the value use [startServerOnFreePort].
 *
 * Blocking, for a process whose only job is to serve requests. Nothing after the call runs until the
 * server stops, so the thread stays parked instead of exiting:
 *
 * ```
 * fun main() {
 *     startServer(port = 8080)
 * }
 * ```
 *
 * Non blocking, for a host that owns its run loop, as an iOS or Android one does. The call returns
 * straight away and the caller keeps the handle to shut the server down:
 *
 * ```
 * private var server: EmbeddedServer<CIOApplicationEngine, CIOApplicationEngine.Configuration>? = null
 *
 * fun onStart() {
 *     server = startServer(wait = false)
 * }
 *
 * fun onStop() {
 *     server?.stop()
 *     server = null
 * }
 * ```
 *
 * @param wait blocks the calling thread until the server stops. Pass `false` when the caller owns
 * the run loop, as an iOS or Android host does.
 * @param logger The host's logger, so the server's lines join the application's instead of going
 *   somewhere of their own. Null lets the server configure logging itself, which is what a process
 *   that is only this server wants.
 */
fun startServer(
    port: Int = EPHEMERAL_PORT,
    wait: Boolean = true,
    logger: Logger? = null,
): EmbeddedServer<CIOApplicationEngine, CIOApplicationEngine.Configuration> = embeddedServer(CIO, port = port) {
    module(NavigatorServerConfig.EMBEDDED, koinOverrides = null, logger = logger)
}.apply { start(wait) }

/**
 * Starts the server on a port the operating system picks, and reports which one it picked.
 *
 * Does not block: the caller keeps its run loop and stops the returned server itself. The port is
 * read once the socket is bound, so it is the real one rather than the [EPHEMERAL_PORT] placeholder.
 *
 * @param logger The host's logger. Embedded there always is a host, and it is the one that decided
 *   where log lines go; a server that ignored it would write its own somewhere else, at a severity
 *   the application never asked for.
 */
suspend fun startServerOnFreePort(logger: Logger? = null): RunningServer =
    startServerOnFreePort(koinOverrides = null, logger = logger)

/**
 * Starts the server with part of its wiring replaced.
 *
 * Internal, and without a default value, so that [Module] never appears in a signature a consumer
 * has to resolve: koin-ktor is an implementation dependency, and a public parameter of that type
 * would put it on the compile classpath of everything that starts a server.
 *
 * @param koinOverrides Definitions replacing the real ones. It is what lets an integration test
 *   reach a real socket without also reaching a paid third party API.
 * @param logger The host's logger, as in [startServerOnFreePort].
 */
internal suspend fun startServerOnFreePort(koinOverrides: Module?, logger: Logger? = null): RunningServer {
    val server = embeddedServer(CIO, port = EPHEMERAL_PORT) {
        module(NavigatorServerConfig.EMBEDDED, koinOverrides, logger)
    }
    server.startSuspend(wait = false)
    return RunningServer(server, server.engine.resolvedConnectors().first().port)
}
