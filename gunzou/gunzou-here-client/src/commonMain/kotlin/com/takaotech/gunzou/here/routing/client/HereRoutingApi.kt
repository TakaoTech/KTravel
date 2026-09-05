package com.takaotech.gunzou.here.routing.client

import com.takaotech.gunzou.here.common.HereApiResult
import com.takaotech.gunzou.here.common.hereGet
import com.takaotech.gunzou.here.routing.dto.request.RoutesRequest
import com.takaotech.gunzou.here.routing.dto.response.RouterRouteResponse
import com.takaotech.gunzou.here.routing.model.ReturnAttribute
import io.ktor.client.HttpClient
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.parameter

/**
 * HERE Routing API v8.
 *
 * Obtained from [com.takaotech.gunzou.here.HereClient.routing]; the HTTP client and its lifecycle
 * belong to the facade.
 */
class HereRoutingApi internal constructor(private val httpClient: HttpClient, private val baseUrl: String) {
    /**
     * Calculates routes between origin and destination.
     *
     * @param request Route request parameters
     * @return HereApiResult containing RouterRouteResponse or error
     */
    suspend fun getRoutes(request: RoutesRequest): HereApiResult<RouterRouteResponse> =
        httpClient.hereGet("${baseUrl}routes") { applyRoutesParameters(request) }
}

/**
 * Maps a [RoutesRequest] onto the query string of the /routes endpoint.
 */
private fun HttpRequestBuilder.applyRoutesParameters(request: RoutesRequest) {
    parameter("transportMode", request.transportMode.toQueryString())

    // TODO Validate and sanitize query string components
    //  - WaypointOptions not available in "origin" param
    //  - "destination" param for WaypointOptions param available is "stopDuration"
    parameter("origin", request.origin.toQueryString())
    parameter("destination", request.destination.toQueryString())
    request.via?.forEach { waypoint ->
        parameter("via", waypoint.toQueryString())
    }

    // TODO Validate:
    // WithOffset not available for request
    request.departureTime?.let {
        parameter("departureTime", it.toQueryString())
    }
    request.arrivalTime?.let {
        parameter("arrivalTime", it)
    }

    // TODO Validate
    // bicycle, bus, pedestrian, privateBus, scooter, taxi allow only "fast" mode
    request.routingMode?.let {
        parameter("routingMode", it.toString())
    }

    request.alternatives?.let {
        parameter("alternatives", it)
    }

    // Sent only when there is something to avoid: HERE rejects an empty avoid[features].
    request.avoid?.featuresQueryString()?.let {
        parameter("avoid[features]", it)
    }

    // TODO Add "allow" param?
    // TODO Add "exclude" param?

    parameter("units", request.units.toQueryString())
    request.lang?.let {
        parameter("lang", it)
    }

    // TODO validate:
    // If actions is requested, then polyline must also be requested as well.
    // If instructions is requested, then actions must also be requested as well.
    // If turnByTurnActions is requested, then polyline must also be requested as well.
    // If at least one attribute is requested within the spans parameter, then polyline must be request as well

    request.returnAttributes?.let { attrs ->
        parameter("return", ReturnAttribute.toQueryString(attrs))
    }

    // TODO Add "spans" param?

    // TODO "vehicle"
    // TODO "consumptionModel"
    // TODO "ev"
    // TODO "fuel"
    // TODO "driver"

    // TODO pedestrian[speed]

    // TODO "scooter"

    // TODO currency

    // TODO taxi

    // TODO tolls

    // TODO maxSpeedOnSegment

    // TODO traffic
}
