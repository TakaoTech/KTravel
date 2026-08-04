package com.takaotech.navigation.common

import io.ktor.client.HttpClient
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
): HttpClient =
    createPlatformHttpClient().config {
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
