package com.takaotech.ktravel.endpoint.here

import com.takaotech.ktravel.endpoint.NavigatorException
import com.takaotech.ktravel.endpoint.ProviderCredentials
import com.takaotech.ktravel.endpoint.requireWithinLimits
import com.takaotech.navigator.api.error.ErrorCode
import com.takaotech.navigator.api.here.HereCarRouteRequest
import com.takaotech.navigator.api.response.RouteResponse

/**
 * `/v1/here/car`, served by the HERE Routing API.
 *
 * The endpoint itself is deliberately short: it checks what the profile published about itself,
 * borrows a client for the caller's key, and hands the two translations to [HereCarMapping]. Every
 * decision that could be wrong lives in one of those two places, where it can be tested against a
 * recorded payload without an HTTP server in the way.
 */
class LiveHereCarEndpoint(private val clients: HereClientPool) : HereCarEndpoint {

    override suspend fun route(request: HereCarRouteRequest, credentials: ProviderCredentials?): RouteResponse {
        descriptor.requireWithinLimits(
            alternatives = request.alternatives,
            viaCount = request.via.size,
            time = request.time,
        )

        val apiKey = credentials?.apiKey ?: throw NavigatorException(
            code = ErrorCode.MISSING_CREDENTIALS,
            message = "${descriptor.profile} cannot be served without a HERE key",
        )

        val response = clients.withClient(apiKey) { client ->
            client.routing.getRoutes(request.toHereRoutesRequest())
        }.orThrowNavigatorException()

        requireRouteFound(response.routes.size)

        return response.toRouteResponse()
    }
}
