package com.takaotech.gunzou.here.search.browser.client

import com.takaotech.gunzou.here.common.HereApiResult
import com.takaotech.gunzou.here.common.hereGet
import com.takaotech.gunzou.here.search.browser.dto.request.BrowseRequest
import com.takaotech.gunzou.here.search.browser.dto.response.BrowseResponse
import io.ktor.client.HttpClient
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.header
import io.ktor.client.request.parameter

/**
 * HERE Geocoding and Search API v7, Browse endpoint.
 *
 * Obtained from [com.takaotech.gunzou.here.HereClient.browse]; the HTTP client and its lifecycle
 * belong to the facade.
 */
class HereBrowseApi internal constructor(private val httpClient: HttpClient, private val baseUrl: String) {
    /**
     * Searches places around [BrowseRequest.at], filtered by category, cuisine, name or area and
     * ranked by distance.
     */
    suspend fun browse(request: BrowseRequest): HereApiResult<BrowseResponse> =
        httpClient.hereGet("${baseUrl}browse") { applyBrowseParameters(request) }
}

/**
 * Maps a [BrowseRequest] onto the query string and headers of the /browse endpoint.
 */
private fun HttpRequestBuilder.applyBrowseParameters(request: BrowseRequest) {
    parameter("at", request.at.toQueryString())

    request.categories?.let { filter ->
        parameter("categories", filter.toQueryString { it.id })
    }

    request.foodTypes?.let { filter ->
        parameter("foodTypes", filter.toQueryString { it })
    }

    request.area?.let {
        parameter("in", it.toQueryString())
    }

    request.name?.let {
        parameter("name", it)
    }

    parameter("lang", request.lang.joinToString(","))

    request.limit?.let {
        parameter("limit", it)
    }

    request.offset?.let {
        parameter("offset", it)
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
