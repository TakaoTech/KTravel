package com.takaotech.gunzou.here.publictransit.client

import com.takaotech.gunzou.here.common.HereApiResult
import com.takaotech.gunzou.here.common.hereGet
import com.takaotech.gunzou.here.publictransit.dto.request.TransitRoutesRequest
import com.takaotech.gunzou.here.publictransit.dto.response.TransitRouteResponse
import com.takaotech.gunzou.here.publictransit.model.ReturnAttribute
import io.ktor.client.HttpClient
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.parameter

/**
 * HERE Public Transit API v8.
 *
 * Obtained from [com.takaotech.gunzou.here.HereClient.publicTransit]; the HTTP client and its
 * lifecycle belong to the facade.
 */
class HereTransitApi internal constructor(private val httpClient: HttpClient, private val baseUrl: String) {
    /**
     * Calculates transit routes between origin and destination.
     *
     * @param request Transit route request parameters
     * @return HereApiResult containing TransitRouteResponse or error
     */
    suspend fun getRoutes(request: TransitRoutesRequest): HereApiResult<TransitRouteResponse> =
        httpClient.hereGet("${baseUrl}routes") { applyTransitRoutesParameters(request) }
}

/**
 * Maps a [TransitRoutesRequest] onto the query string of the /routes endpoint.
 */
private fun HttpRequestBuilder.applyTransitRoutesParameters(request: TransitRoutesRequest) {
    parameter("origin", request.origin.toQueryString())
    parameter("destination", request.destination.toQueryString())

    parameter("lang", request.lang.joinToString(","))

    parameter("units", request.units.toQueryString())

    request.departureTime?.let {
        parameter("departureTime", it)
    }

    request.arrivalTime?.let {
        parameter("arrivalTime", it)
    }

    parameter("alternatives", request.alternatives)

    request.changes?.let {
        parameter("changes", it)
    }

    request.modes?.let {
        parameter("modes", it.toQueryString())
    }

    request.pedestrianSpeed?.let {
        parameter("pedestrian[speed]", it)
    }

    request.pedestrianMaxDistance?.let {
        parameter("pedestrian[maxDistance]", it)
    }

    request.accessibility?.let { accessibilityList ->
        parameter("accessibility", accessibilityList.joinToString(","))
    }

    request.returnAttributes?.let { attrs ->
        parameter("return", ReturnAttribute.toQueryString(attrs))
    }
}
