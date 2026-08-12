package com.takaotech.ktravel

import co.touchlab.kermit.koin.KermitKoinLogger
import io.ktor.server.application.Application
import io.ktor.server.application.install
import org.koin.dsl.module
import org.koin.ktor.plugin.Koin

fun Application.configureKoin() {
    install(Koin) {
        // koin-logger-slf4j is JVM only; Kermit covers every target this module builds for.
        logger(KermitKoinLogger(appLog.withTag("koin")))
        modules(
            module {
                single<HelloService> {
                    HelloService { appLog.i { "Hello, World!" } }
                }
            },
        )
    }
}
