package com.takaotech.navigator.client

import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp

/** OkHttp on the desktop, matching what `:gunzou-here-client` uses on the same target. */
actual fun createPlatformHttpClient(): HttpClient = HttpClient(OkHttp)
