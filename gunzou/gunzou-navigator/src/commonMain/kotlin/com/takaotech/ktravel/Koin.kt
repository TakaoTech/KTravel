package com.takaotech.ktravel

import co.touchlab.kermit.koin.KermitKoinLogger
import com.takaotech.ktravel.endpoint.ProviderCatalog
import com.takaotech.ktravel.endpoint.here.FakeHereCarEndpoint
import com.takaotech.ktravel.endpoint.here.HereCarEndpoint
import io.ktor.server.application.Application
import io.ktor.server.application.install
import org.koin.dsl.module
import org.koin.ktor.plugin.Koin

/**
 * What the routes are built out of.
 *
 * Each endpoint is bound to its own interface rather than to `NavigationEndpoint<T>`: generics are
 * erased at the container's key, so every profile would otherwise collide on the same entry.
 */
fun Application.configureKoin() {
    install(Koin) {
        // koin-logger-slf4j is JVM only; Kermit covers every target this module builds for.
        logger(KermitKoinLogger(appLog.withTag("koin")))
        modules(
            module {
                // Straight line answers, no HERE behind them. Replaced by the real client once the
                // routes and the error mapping are pinned down by tests that cost nothing to run.
                single<HereCarEndpoint> { FakeHereCarEndpoint() }

                // Assembled from the endpoints that exist, so `GET /v1/profiles` cannot advertise a
                // profile no route serves.
                single { ProviderCatalog(listOf(get<HereCarEndpoint>())) }
            },
        )
    }
}
