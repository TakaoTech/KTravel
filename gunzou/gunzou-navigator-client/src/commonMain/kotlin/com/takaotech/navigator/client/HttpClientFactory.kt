package com.takaotech.navigator.client

import com.takaotech.navigator.api.NavigatorJson
import io.ktor.client.HttpClient
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.serialization.kotlinx.json.json

/**
 * The engine of the current platform: OkHttp on Android and the JVM, Darwin on iOS.
 *
 * Same shape as `:gunzou-here-client` uses, for the same reason: an engine is the one thing that
 * cannot be written once for every target.
 */
expect fun createPlatformHttpClient(): HttpClient

/** Builds the client a [NavigatorClient] talks through. */
internal fun createNavigatorHttpClient(config: NavigatorClientConfig): HttpClient =
    createPlatformHttpClient().withNavigatorDefaults(config)

/**
 * Applies what every call to the navigator needs.
 *
 * The [NavigatorJson] instance is the contract's own, the same one the server installs. Configuring
 * a second one here with slightly different settings is exactly how a client stops being able to
 * read a server that grew a field.
 *
 * No `defaultRequest` with a base URL: it is resolved per request, because an embedded server does
 * not keep the same one.
 */
internal fun HttpClient.withNavigatorDefaults(config: NavigatorClientConfig): HttpClient = this.config {
    install(ContentNegotiation) {
        json(NavigatorJson)
    }

    install(HttpTimeout) {
        requestTimeoutMillis = config.requestTimeoutMillis
    }

    // Never throws on a non 2xx: a failing status carries an ErrorResponse the caller is meant to
    // read, and turning it into an exception would throw that away before anyone saw it.
    expectSuccess = false

    if (config.enableLogging) {
        install(Logging) {
            level = LogLevel.INFO
        }
    }
}
