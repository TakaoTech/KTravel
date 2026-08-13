package com.takaotech.navigator.client

import io.ktor.client.HttpClient
import io.ktor.client.engine.darwin.Darwin

/** Darwin on iOS, so the call goes through the platform's own URL loading system. */
actual fun createPlatformHttpClient(): HttpClient = HttpClient(Darwin)
