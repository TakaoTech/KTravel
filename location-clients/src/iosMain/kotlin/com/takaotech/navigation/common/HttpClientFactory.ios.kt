package com.takaotech.navigation.common

import io.ktor.client.*
import io.ktor.client.engine.darwin.*

/**
 * iOS-specific HttpClient using Darwin engine.
 */
actual fun createPlatformHttpClient(): HttpClient {
    return HttpClient(Darwin) {
        engine {
            configureRequest {
                setTimeoutInterval(30.0)
            }
        }
    }
}
