package com.takaotech.gunzou.here.common

import io.ktor.client.HttpClient
import io.ktor.client.engine.darwin.Darwin

/**
 * iOS-specific HttpClient using Darwin engine.
 */
actual fun createPlatformHttpClient(): HttpClient = HttpClient(Darwin) {
    engine {
        configureRequest {
            setTimeoutInterval(30.0)
        }
    }
}
