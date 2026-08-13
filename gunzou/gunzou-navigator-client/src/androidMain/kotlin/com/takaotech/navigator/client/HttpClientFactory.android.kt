package com.takaotech.navigator.client

import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp

/**
 * OkHttp on Android.
 *
 * Reaching an embedded server over `127.0.0.1` still counts as network access, so the host
 * application has to declare the `INTERNET` permission even when nothing leaves the device.
 */
actual fun createPlatformHttpClient(): HttpClient = HttpClient(OkHttp)
