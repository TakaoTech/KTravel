package com.takaotech.ktravel

import io.ktor.server.application.Application

/**
 * Everything the server installs on every target.
 *
 * The JVM adds compression, metrics, OpenAPI and Swagger on top of this through
 * `Application.jvmModule`: those Ktor modules publish no native variant.
 */
fun Application.module() {
    installLogging()
    configureKoin()
    configureSerialization()
    configureResources()
    configureRequestValidation()
    configureCaching()
    configureRouting()
}
