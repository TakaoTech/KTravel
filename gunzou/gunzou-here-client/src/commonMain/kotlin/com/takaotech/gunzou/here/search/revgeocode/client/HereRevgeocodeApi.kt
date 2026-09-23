package com.takaotech.gunzou.here.search.revgeocode.client

import com.takaotech.gunzou.here.common.HereApiResult
import com.takaotech.gunzou.here.common.hereGet
import com.takaotech.gunzou.here.search.revgeocode.dto.request.RevgeocodeRequest
import com.takaotech.gunzou.here.search.revgeocode.dto.response.RevgeocodeResponse
import io.ktor.client.HttpClient
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.header
import io.ktor.client.request.parameter

/**
 * HERE Geocoding and Search API v7, Reverse Geocode endpoint.
 *
 * Obtained from [com.takaotech.gunzou.here.HereClient.revgeocode]; the HTTP client and its
 * lifecycle belong to the facade.
 */
class HereRevgeocodeApi internal constructor(private val httpClient: HttpClient, private val baseUrl: String) {
    /**
     * Finds the addresses nearest to [RevgeocodeRequest.at], or within [RevgeocodeRequest.area],
     * most likely first.
     */
    suspend fun revgeocode(request: RevgeocodeRequest): HereApiResult<RevgeocodeResponse> =
        httpClient.hereGet("${baseUrl}revgeocode") { applyRevgeocodeParameters(request) }
}

/**
 * Maps a [RevgeocodeRequest] onto the query string and headers of the /revgeocode endpoint.
 */
private fun HttpRequestBuilder.applyRevgeocodeParameters(request: RevgeocodeRequest) {
    request.at?.let {
        parameter("at", it.toQueryString())
    }

    request.area?.let {
        parameter("in", it.toQueryString())
    }

    request.bearing?.let {
        parameter("bearing", it)
    }

    request.types?.takeIf { it.isNotEmpty() }?.let { types ->
        parameter("types", types.joinToString(",") { it.toQueryString() })
    }

    request.features?.takeIf { it.isNotEmpty() }?.let { features ->
        parameter("with", features.joinToString(",") { it.toQueryString() })
    }

    parameter("lang", request.lang.joinToString(","))

    request.limit?.let {
        parameter("limit", it)
    }

    request.politicalView?.let {
        parameter("politicalView", it)
    }

    request.show?.takeIf { it.isNotEmpty() }?.let { options ->
        parameter("show", options.joinToString(",") { it.toQueryString() })
    }

    request.requestId?.let {
        header(REQUEST_ID_HEADER, it)
    }
}

private const val REQUEST_ID_HEADER = "X-Request-ID"
