package com.takaotech.gunzou.here.search.autocomplete.client

import com.takaotech.gunzou.here.common.HereApiResult
import com.takaotech.gunzou.here.common.hereGet
import com.takaotech.gunzou.here.search.autocomplete.dto.request.AutocompleteRequest
import com.takaotech.gunzou.here.search.autocomplete.dto.response.AutocompleteResponse
import io.ktor.client.HttpClient
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.header
import io.ktor.client.request.parameter

/**
 * HERE Geocoding and Search API v7, Autocomplete endpoint.
 *
 * Obtained from [com.takaotech.gunzou.here.HereClient.autocomplete]; the HTTP client and its
 * lifecycle belong to the facade.
 */
class HereAutocompleteApi internal constructor(private val httpClient: HttpClient, private val baseUrl: String) {
    /**
     * Completes [AutocompleteRequest.query] into street addresses and administrative areas, most
     * likely first.
     */
    suspend fun autocomplete(request: AutocompleteRequest): HereApiResult<AutocompleteResponse> =
        httpClient.hereGet("${baseUrl}autocomplete") { applyAutocompleteParameters(request) }
}

/**
 * Maps an [AutocompleteRequest] onto the query string and headers of the /autocomplete endpoint.
 */
private fun HttpRequestBuilder.applyAutocompleteParameters(request: AutocompleteRequest) {
    parameter("q", request.query)

    request.at?.let {
        parameter("at", it.toQueryString())
    }

    request.area?.let {
        parameter("in", it.toQueryString())
    }

    request.postalCodeMode?.let {
        parameter("postalCodeMode", it.toQueryString())
    }

    request.types?.takeIf { it.isNotEmpty() }?.let { types ->
        parameter("types", types.joinToString(",") { it.toQueryString() })
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
