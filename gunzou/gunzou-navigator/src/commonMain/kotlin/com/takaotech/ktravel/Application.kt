package com.takaotech.ktravel

import io.ktor.server.application.Application

/**
 * Everything the server installs on every target.
 *
 * The JVM adds compression, metrics, OpenAPI and Swagger on top of this through
 * `Application.jvmModule`: those Ktor modules publish no native variant.
 *
 * Order matters in one place: [configureStatusPages] must be installed before the routes, so the
 * plugin is wrapping them when one throws.
 */
fun Application.module() {
    installLogging()
    configureKoin()
    configureSerialization()
    configureStatusPages()
    configureRequestValidation()
    configureCaching()
    configureRouting()
}
