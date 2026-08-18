package com.takaotech.ktravel

import co.touchlab.kermit.Logger
import co.touchlab.kermit.koin.KermitKoinLogger
import com.takaotech.ktravel.endpoint.ProviderCatalog
import com.takaotech.ktravel.endpoint.here.HereClientPool
import com.takaotech.ktravel.endpoint.here.HereRoutingEndpoint
import com.takaotech.ktravel.endpoint.here.HereTransitEndpoint
import com.takaotech.ktravel.endpoint.here.LiveHereRoutingEndpoint
import com.takaotech.ktravel.endpoint.here.LiveHereTransitEndpoint
import io.ktor.server.application.Application
import io.ktor.server.application.ApplicationStopped
import io.ktor.server.application.install
import org.koin.core.module.Module
import org.koin.dsl.module
import org.koin.ktor.ext.getKoin
import org.koin.ktor.plugin.Koin

/**
 * What the routes are built out of.
 *
 * Each endpoint is bound to its own interface rather than to `NavigationEndpoint<T>`: generics are
 * erased at the container's key, so both profiles would otherwise collide on the same entry.
 *
 * @param overrides Definitions replacing the real ones. Only tests pass this, to put a mock HTTP
 *   engine behind the HERE client without a second copy of the wiring going stale beside this one.
 */
internal fun Application.configureKoin(overrides: Module?) {
    install(Koin) {
        // koin-logger-slf4j is JVM only; Kermit covers every target this module builds for.
        logger(KermitKoinLogger(appLog.withTag("koin")))

        // Resolved here rather than read from `appLog` inside the module: what the server logs
        // through is decided by `installLogging`, which has already run when this is built, and
        // capturing it now is what keeps a definition from reading it again later.
        modules(listOfNotNull(navigatorModule(appLog), overrides))
    }

    // The pool owns HTTP clients, which own connection pools and threads. Closing it here rather
    // than leaving it to the garbage collector matters most on Android and iOS, where the server
    // starts and stops with the host rather than living as long as the process.
    //
    // Resolved now and captured, instead of looked up when the event fires: by then Koin may have
    // been closed itself, and the order between two plugins' shutdown hooks is not something to
    // depend on. `ApplicationStopped` fires after the engine has finished its in flight requests,
    // which is the precondition HereClientPool.close documents.
    val clients = getKoin().get<HereClientPool>()
    monitor.subscribe(ApplicationStopped) { clients.close() }
}

/**
 * The real wiring: HERE behind both profiles, sharing one pool of clients.
 *
 * @param logger What the server writes through, passed on to the HTTP clients so their request and
 *   response dumps end up beside everything else rather than in a channel of their own.
 */
private fun navigatorModule(logger: Logger): Module = module {
    // One pool for both profiles. HereClient is a facade over a single HTTP client that serves the
    // routing and the transit host alike, so a second pool would double the connections for nothing.
    single { HereClientPool(logger = logger) }

    single<HereRoutingEndpoint> { LiveHereRoutingEndpoint(get()) }
    single<HereTransitEndpoint> { LiveHereTransitEndpoint(get()) }

    // Assembled from the endpoints that exist, so `GET /v1/profiles` cannot advertise a profile no
    // route serves.
    single { ProviderCatalog(listOf(get<HereRoutingEndpoint>(), get<HereTransitEndpoint>())) }
}
