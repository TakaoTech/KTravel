package com.takaotech.ktravel.endpoint.here

import com.takaotech.ktravel.endpoint.NavigatorException
import com.takaotech.ktravel.endpoint.ProviderCredentials
import com.takaotech.ktravel.endpoint.requireWithinLimits
import com.takaotech.navigator.api.error.ErrorCode
import com.takaotech.navigator.api.here.HereTransitRouteRequest
import com.takaotech.navigator.api.response.RouteResponse

/**
 * `/v1/here/transit`, served by the HERE Public Transit API.
 *
 * Structurally identical to [LiveHereRoutingEndpoint] and sharing its client pool, while talking to a
 * different API with a different request and a different answer. That is what the second profile is
 * for: everything the two have in common turned out to be the server's own machinery, and the only
 * thing that differs is the translation.
 */
class LiveHereTransitEndpoint(private val clients: HereClientPool) : HereTransitEndpoint {

    override suspend fun route(request: HereTransitRouteRequest, credentials: ProviderCredentials?): RouteResponse {
        descriptor.requireWithinLimits(
            alternatives = request.alternatives,
            viaCount = 0,
            time = request.time,
        )

        val apiKey = credentials?.apiKey ?: throw NavigatorException(
            code = ErrorCode.MISSING_CREDENTIALS,
            message = "${descriptor.profile} cannot be served without a HERE key",
        )

        val response = clients.withClient(apiKey) { client ->
            client.publicTransit.getRoutes(request.toHereTransitRequest())
        }.orThrowNavigatorException()

        requireRouteFound(response.routes.size)

        return response.toRouteResponse()
    }
}
