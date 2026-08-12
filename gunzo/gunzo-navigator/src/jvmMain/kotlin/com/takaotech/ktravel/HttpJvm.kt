package com.takaotech.ktravel

import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.plugins.compression.Compression
import io.ktor.server.plugins.openapi.openAPI
import io.ktor.server.plugins.swagger.swaggerUI
import io.ktor.server.routing.routing

/**
 * The HTTP features that only exist on the JVM: `ktor-server-compression`, `ktor-server-openapi` and
 * `ktor-server-swagger` publish no native variant.
 *
 * The documentation is read from `openapi/documentation.yaml` on the classpath, which
 * :gunzo-navigator-app supplies.
 */
fun Application.configureHttpJvm() {
    install(Compression)
    routing {
        openAPI(path = "openapi")
        swaggerUI(path = "swagger")
    }
}
