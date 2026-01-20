package com.takaotech.navigation.publictransit.client

import com.takaotech.navigation.common.HereEndpointUrls
import com.takaotech.navigation.common.createHereHttpClient
import com.takaotech.navigation.publictransit.dto.request.TransitRoutesRequest
import com.takaotech.navigation.publictransit.dto.response.ErrorResponse
import com.takaotech.navigation.publictransit.dto.response.TransitRouteResponse
import com.takaotech.navigation.publictransit.exception.PublicTransitApiResult
import com.takaotech.navigation.publictransit.model.ReturnAttribute
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.serialization.SerializationException

/**
 * Client for HERE Public Transit API v8.
 *
 * @property httpClient Configured HttpClient instance
 */
class HerePublicTransitClient(
    private val httpClient: HttpClient
) {
    /**
     * Creates a HerePublicTransitClient with the given API key.
     *
     * @param apiKey HERE API key
     * @param enableLogging Enable HTTP logging for debugging
     */
    constructor(
        apiKey: String,
        enableLogging: Boolean = false
    ) : this(
        createHereHttpClient(
            HereEndpointUrls.PUBLIC_TRANSIT,
            apiKey,
            enableLogging
        )
    )

    /**
     * Calculates transit routes between origin and destination.
     *
     * @param request Transit route request parameters
     * @return PublicTransitApiResult containing TransitRouteResponse or error
     */
    suspend fun getRoutes(request: TransitRoutesRequest): PublicTransitApiResult<TransitRouteResponse> {
        return try {
            val response = httpClient.get("routes") {
                parameter("origin", request.origin)
                parameter("destination", request.destination)

                require(request.lang.isNotEmpty()) { "Language list cannot be empty" }

                request.lang.let { langs ->
                    parameter("lang", langs.joinToString(","))
                }

                request.units.let {
                    parameter("units", it.toQueryString())
                }

                request.departureTime?.let {
                    parameter("departureTime", it)
                }

                request.arrivalTime?.let {
                    parameter("arrivalTime", it)
                }

                request.alternatives.let {
                    parameter("alternatives", it)
                }

                request.changes?.let {
                    parameter("changes", it)
                }

                request.modes?.let { modes ->
                    parameter("modes", request.modes.toQueryString())
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

            if (response.status.isSuccess()) {
                val routeResponse = response.body<TransitRouteResponse>()
                PublicTransitApiResult.Success(routeResponse)
            } else {
                val errorResponse = try {
                    response.body<ErrorResponse>()
                } catch (e: SerializationException) {
                    ErrorResponse(
                        title = "Unknown error",
                        status = response.status.value,
                        cause = response.bodyAsText()
                    )
                }
                PublicTransitApiResult.Error(
                    httpStatusCode = response.status.value,
                    errorResponse = errorResponse
                )
            }
        } catch (e: SerializationException) {
            PublicTransitApiResult.Error(
                httpStatusCode = 0,
                exception = e,
                errorResponse = ErrorResponse(
                    title = "Serialization error",
                    status = 0,
                    cause = e.message
                )
            )
        } catch (e: Exception) {
            PublicTransitApiResult.Error(
                httpStatusCode = 0,
                exception = e,
                errorResponse = ErrorResponse(
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
