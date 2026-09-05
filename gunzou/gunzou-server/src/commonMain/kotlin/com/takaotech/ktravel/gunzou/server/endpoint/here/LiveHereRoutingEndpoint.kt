package com.takaotech.ktravel.gunzou.server.endpoint.here

import com.takaotech.gunzou.api.error.ErrorCode
import com.takaotech.gunzou.api.here.HereAvoidFeature
import com.takaotech.gunzou.api.here.HereReturnAttribute
import com.takaotech.gunzou.api.here.HereTransportMode
import com.takaotech.gunzou.api.response.RoutingRouteResponse
import com.takaotech.ktravel.gunzou.server.endpoint.NavigatorException
import com.takaotech.ktravel.gunzou.server.endpoint.ProviderCredentials
import com.takaotech.ktravel.gunzou.server.endpoint.requireWithinLimits

/**
 * `/v1/here/routing/{transportMode}`, served by the HERE Routing API.
 *
 * The endpoint itself is deliberately short: it checks what the profile published about itself,
 * borrows a client for the caller's key, and hands the two translations to
 * `HereRoutingRequestMapping` and `HereRoutingResponseMapping`. Every decision that could be wrong
 * lives in one of those two places, where it can be tested against a recorded payload without an
 * HTTP server in the way.
 */
class LiveHereRoutingEndpoint(private val clients: HereClientPool) : HereRoutingEndpoint {

    override suspend fun route(request: HereRoutingCall, credentials: ProviderCredentials?): RoutingRouteResponse {
        val (mode, body) = request

        descriptor.requireWithinLimits(
            alternatives = body.alternatives,
            viaCount = body.via.size,
            time = body.time,
        )
        mode.requireTollsAreAskable(body.returnAttributes, body.avoid?.features.orEmpty())

        val apiKey = credentials?.apiKey
            ?: throw NavigatorException(
                code = ErrorCode.MISSING_CREDENTIALS,
                message = "${descriptor.profile} cannot be served without a HERE key",
            )

        val response = clients.withClient(apiKey) { client ->
            client.routing.getRoutes(body.toHereRoutesRequest(mode))
        }.orThrowNavigatorException()

        requireRouteFound(response.routes.size)

        return response.toRoutingRouteResponse()
    }
}

/**
 * Refuses to ask HERE what a walk costs in tolls.
 *
 * A pedestrian and a cyclist pay none, so both halves of the question are empty by construction: the
 * cost there is nothing to compute, and the avoidance there is nothing to route around. Upstream
 * charges for the call either way, which is why this is refused before it is made rather than
 * answered with an empty list.
 *
 * @throws com.takaotech.ktravel.gunzou.server.endpoint.NavigatorException [ErrorCode.UNSUPPORTED_OPTION] when this mode is asked about tolls.
 */
private fun HereTransportMode.requireTollsAreAskable(
    returnAttributes: List<HereReturnAttribute>,
    avoid: List<HereAvoidFeature>,
) {
    if (hasTolls) return

    val asked = when {
        HereReturnAttribute.TOLLS in returnAttributes -> "returnAttributes: TOLLS"
        HereAvoidFeature.TOLL_ROAD in avoid -> "avoid: TOLL_ROAD"
        else -> return
    }

    throw NavigatorException(
        code = ErrorCode.UNSUPPORTED_OPTION,
        message = "A $pathSegment route pays no toll, so it cannot be asked for $asked",
    )
}
