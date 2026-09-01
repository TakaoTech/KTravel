package com.takaotech.gunzou.here.common

import io.ktor.client.HttpClient
import io.ktor.client.plugins.api.createClientPlugin
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

/**
 * Platform-specific HttpClient engine factory.
 */
expect fun createPlatformHttpClient(): HttpClient

/**
 * JSON configuration for HERE API serialization.
 */
val hereApiJson = Json {
    ignoreUnknownKeys = true
    isLenient = true
    coerceInputValues = true
    encodeDefaults = false
}

/**
 * Creates a configured HttpClient shared by every HERE API.
 *
 * @param config Shared HERE client configuration
 * @return Configured HttpClient instance
 */
internal fun createHereHttpClient(config: HereClientConfig): HttpClient =
    createPlatformHttpClient().withHereDefaults(config)

/**
 * Applies the configuration every HERE API expects: JSON negotiation, the API key, and logging.
 *
 * No base URL is installed: the APIs live on different hosts, so each one builds an absolute URL
 * from its own base. Only the API key, which is common to all of them, is applied globally.
 *
 * Logging is installed unconditionally and there is no flag to turn it off, because the logger in
 * [HereClientConfig] already is that flag: every line is written at `Severity.Debug`, and a caller
 * whose logger keeps `Severity.Info` — which is what a release build does — drops them before they
 * are formatted. Note that `LogLevel.ALL` prints the request URL, and the HERE API key travels in it
 * as a query parameter: a build that logs at debug is a build that logs the key.
 */
internal fun HttpClient.withHereDefaults(config: HereClientConfig): HttpClient = this.config {
    install(ContentNegotiation) {
        json(hereApiJson)
    }

    install(hereApiKeyPlugin(config.apiKey))

    install(Logging) {
        level = LogLevel.ALL
        logger = KermitKtorLogger(config.logger)
    }

    defaultRequest {
        contentType(ContentType.Application.Json)
    }
}

/**
 * Appends the API key to every request.
 *
 * It cannot live in `defaultRequest`: default URL parameters are dropped as soon as a request
 * builds an absolute URL, which is exactly what each API does to reach its own host.
 */
private fun hereApiKeyPlugin(apiKey: String) = createClientPlugin("HereApiKey") {
    onRequest { request, _ ->
        request.url.parameters.append(HereEndpointUrls.API_KEY_PARAM, apiKey)
    }
}
