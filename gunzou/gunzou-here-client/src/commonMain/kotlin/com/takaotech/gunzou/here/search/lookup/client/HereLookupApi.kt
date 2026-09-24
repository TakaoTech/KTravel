package com.takaotech.gunzou.here.search.lookup.client

import com.takaotech.gunzou.here.common.HereApiResult
import com.takaotech.gunzou.here.common.hereGet
import com.takaotech.gunzou.here.search.lookup.dto.request.LookupRequest
import com.takaotech.gunzou.here.search.lookup.dto.response.LookupResponse
import io.ktor.client.HttpClient
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.header
import io.ktor.client.request.parameter

/**
 * HERE Geocoding and Search API v7, Lookup endpoint.
 *
 * Obtained from [com.takaotech.gunzou.here.HereClient.lookup]; the HTTP client and its lifecycle
 * belong to the facade.
 */
class HereLookupApi internal constructor(private val httpClient: HttpClient, private val baseUrl: String) {
    /**
     * Retrieves the result identified by [LookupRequest.id], as returned by another Search
     * endpoint. An unknown identifier is an error with HTTP status 404.
     */
    suspend fun lookup(request: LookupRequest): HereApiResult<LookupResponse> =
        httpClient.hereGet("${baseUrl}lookup") { applyLookupParameters(request) }
}

/**
 * Maps a [LookupRequest] onto the query string and headers of the /lookup endpoint.
 */
private fun HttpRequestBuilder.applyLookupParameters(request: LookupRequest) {
    parameter("id", request.id)

    parameter("lang", request.lang.joinToString(","))

    request.show?.takeIf { it.isNotEmpty() }?.let { options ->
        parameter("show", options.joinToString(",") { it.toQueryString() })
    }

    request.requestId?.let {
        header(REQUEST_ID_HEADER, it)
    }
}

private const val REQUEST_ID_HEADER = "X-Request-ID"
