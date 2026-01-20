package com.takaotech.navigation.common

import io.ktor.client.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
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
 * Creates a configured HttpClient for HERE API calls.
 *
 * @param apiKey HERE API key for authentication
 * @param enableLogging Enable HTTP request/response logging
 * @return Configured HttpClient instance
 */
fun createHereHttpClient(
    baseUrl: String,
    apiKey: String,
    enableLogging: Boolean = false
): HttpClient {
    return createPlatformHttpClient().config {
        install(ContentNegotiation) {
            json(hereApiJson)
        }

        if (enableLogging) {
            install(Logging) {
                level = LogLevel.ALL
            }
        }

        defaultRequest {
            url(baseUrl)
            contentType(ContentType.Application.Json)
            url.parameters.append("apiKey", apiKey)
        }
    }
}
