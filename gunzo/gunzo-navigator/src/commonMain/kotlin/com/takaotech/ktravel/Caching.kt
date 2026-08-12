package com.takaotech.ktravel

import io.ktor.http.CacheControl
import io.ktor.http.ContentType
import io.ktor.http.content.CachingOptions
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.plugins.cachingheaders.CachingHeaders

private const val ONE_DAY_IN_SECONDS = 24 * 60 * 60

fun Application.configureCaching() {
    install(CachingHeaders) {
        options { _, outgoingContent ->
            when (outgoingContent.contentType?.withoutParameters()) {
                ContentType.Text.CSS -> CachingOptions(CacheControl.MaxAge(maxAgeSeconds = ONE_DAY_IN_SECONDS))
                else -> null
            }
        }
    }
}
