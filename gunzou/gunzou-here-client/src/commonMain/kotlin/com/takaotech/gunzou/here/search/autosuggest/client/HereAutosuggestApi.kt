package com.takaotech.gunzou.here.search.autosuggest.client

import com.takaotech.gunzou.here.common.HereApiResult
import com.takaotech.gunzou.here.common.hereGet
import com.takaotech.gunzou.here.search.autosuggest.dto.request.AutosuggestRequest
import com.takaotech.gunzou.here.search.autosuggest.dto.response.AutosuggestResponse
import io.ktor.client.HttpClient
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.header
import io.ktor.client.request.parameter

/**
 * HERE Geocoding and Search API v7, Autosuggest endpoint.
 *
 * Obtained from [com.takaotech.gunzou.here.HereClient.autosuggest]; the HTTP client and its
 * lifecycle belong to the facade.
 */
class HereAutosuggestApi internal constructor(private val httpClient: HttpClient, private val baseUrl: String) {
    /**
     * Suggests places, addresses and category or chain queries for [AutosuggestRequest.query], most
     * relevant first.
     */
    suspend fun autosuggest(request: AutosuggestRequest): HereApiResult<AutosuggestResponse> =
        httpClient.hereGet("${baseUrl}autosuggest") { applyAutosuggestParameters(request) }
}

/**
 * Maps an [AutosuggestRequest] onto the query string and headers of the /autosuggest endpoint.
 */
private fun HttpRequestBuilder.applyAutosuggestParameters(request: AutosuggestRequest) {
    parameter("q", request.query)

    request.at?.let {
        parameter("at", it.toQueryString())
    }

    request.area?.let {
        parameter("in", it.toQueryString())
    }

    parameter("lang", request.lang.joinToString(","))

    request.limit?.let {
        parameter("limit", it)
    }

    request.offset?.let {
        parameter("offset", it)
    }

    request.termsLimit?.let {
        parameter("termsLimit", it)
    }

    request.show?.takeIf { it.isNotEmpty() }?.let { options ->
        parameter("show", options.joinToString(",") { it.toQueryString() })
    }

    request.requestId?.let {
        header(REQUEST_ID_HEADER, it)
    }
}

private const val REQUEST_ID_HEADER = "X-Request-ID"
