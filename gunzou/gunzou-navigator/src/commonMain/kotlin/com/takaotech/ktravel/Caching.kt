package com.takaotech.ktravel

import io.ktor.http.CacheControl
import io.ktor.http.ContentType
import io.ktor.http.content.CachingOptions
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.plugins.cachingheaders.CachingHeaders

private const val ONE_DAY_IN_SECONDS = 24 * 60 * 60

/**
 * Cache headers for the static content the JVM build serves alongside the API.
 *
 * The routing answers are deliberately left uncached: a route depends on the traffic at the moment
 * it was asked for, and serving yesterday's answer from a proxy would be worse than not answering.
 */
fun Application.configureCaching() {
    install(CachingHeaders) {
        // TODO
        options { _, outgoingContent ->
            when (outgoingContent.contentType?.withoutParameters()) {
                ContentType.Text.CSS -> CachingOptions(CacheControl.MaxAge(maxAgeSeconds = ONE_DAY_IN_SECONDS))
                else -> null
            }
        }
    }
}
