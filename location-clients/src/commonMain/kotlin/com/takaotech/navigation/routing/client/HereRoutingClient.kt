package com.takaotech.navigation.routing.client

import com.takaotech.navigation.common.HereEndpointUrls
import com.takaotech.navigation.common.createHereHttpClient
import com.takaotech.navigation.routing.dto.request.RoutesRequest
import com.takaotech.navigation.routing.dto.response.RouterRouteResponse
import com.takaotech.navigation.routing.exception.HereApiResult
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.serialization.SerializationException

/**
 * Client for HERE Routing API v8.
 *
 * @property httpClient Configured HttpClient instance
 */
class HereRoutingClient(
    private val httpClient: HttpClient
) {
    /**
     * Creates a HereRoutingClient with the given API key.
     *
     * @param apiKey HERE API key
     * @param enableLogging Enable HTTP logging for debugging
     */
    constructor(
        apiKey: String,
        enableLogging: Boolean = false
    ) : this(createHereHttpClient(HereEndpointUrls.ROUTING, apiKey, enableLogging))

    /**
     * Calculates routes between origin and destination.
     *
     * @param request Route request parameters
     * @return HereApiResult containing RouterRouteResponse or error
     */
    suspend fun getRoutes(request: RoutesRequest): HereApiResult<RouterRouteResponse> {
        return try {
            val response = httpClient.get("routes") {
                parameter("transportMode", request.transportMode.name.lowercase())

                //TODO Validate and sanitize query string components
                //  - WaypointOptions not available in "origin" param
                //  - "destination" param for WaypointOptions param available is "stopDuration"
                parameter("origin", request.origin.toQueryString())
                parameter("destination", request.destination.toQueryString())
                request.via?.forEach { waypoint ->
                    parameter("via", waypoint.toQueryString())
                }

                //TODO Validate:
                // WithOffset not available for request
                request.departureTime?.let {
                    parameter("departureTime", it.toQueryString())
                }
                request.arrivalTime?.let {
                    parameter("arrivalTime", it)
                }

                //TODO Validate
                // bicycle, bus, pedestrian, privateBus, scooter, taxi allow only "fast" mode
                request.routingMode?.let {
                    parameter("routingMode", it.toString())
                }

                request.alternatives?.let {
                    parameter("alternatives", it)
                }

                //TODO Add "avoid" param?
                //TODO Add "allow" param?
                //TODO Add "exclude" param?

                request.units?.let {
                    parameter("units", it.name.lowercase())
                }
                request.lang?.let {
                    parameter("lang", it)
                }

                //TODO validate:
                // If actions is requested, then polyline must also be requested as well.
                // If instructions is requested, then actions must also be requested as well.
                // If turnByTurnActions is requested, then polyline must also be requested as well.
                // If at least one attribute is requested within the spans parameter, then polyline must be request as well

                request.returnAttributes?.let { attrs ->
                    parameter(
                        "return",
                        com.takaotech.navigation.routing.dto.request.ReturnAttribute.toQueryString(
                            attrs
                        )
                    )
                }

                //TODO Add "spans" param?

                //TODO "vehicle"
                //TODO "consumptionModel"
                //TODO "ev"
                //TODO "fuel"
                //TODO "driver"

                //TODO pedestrian[speed]

                //TODO "scooter"

                //TODO currency

                //TODO taxi

                //TODO tolls

                //TODO maxSpeedOnSegment

                //TODO traffic
            }

            if (response.status.isSuccess()) {
                val routeResponse = response.body<RouterRouteResponse>()
                HereApiResult.Success(routeResponse)
            } else {
                val errorResponse = try {
                    response.body<com.takaotech.navigation.routing.dto.response.ErrorResponse>()
                } catch (e: SerializationException) {
                    _root_ide_package_.com.takaotech.navigation.routing.dto.response.ErrorResponse(
                        title = "Unknown error",
                        status = response.status.value,
                        cause = response.bodyAsText()
                    )
                }
                HereApiResult.Error(
                    httpStatusCode = response.status.value,
                    errorResponse = errorResponse
                )
            }
        } catch (e: SerializationException) {
            HereApiResult.Error(
                httpStatusCode = 0,
                exception = e,
                errorResponse = _root_ide_package_.com.takaotech.navigation.routing.dto.response.ErrorResponse(
                    title = "Serialization error",
                    status = 0,
                    cause = e.message
                )
            )
        } catch (e: Exception) {
            HereApiResult.Error(
                httpStatusCode = 0,
                exception = e,
                errorResponse = _root_ide_package_.com.takaotech.navigation.routing.dto.response.ErrorResponse(
                    title = "Network error",
                    status = 0,
                    cause = e.message
                )
            )
        }
    }

    /**
     * Closes the underlying HttpClient.
     * Call this when the client is no longer needed.
     */
    fun close() {
        httpClient.close()
    }
}
