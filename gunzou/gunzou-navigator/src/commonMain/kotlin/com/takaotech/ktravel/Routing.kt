package com.takaotech.ktravel

import com.takaotech.ktravel.endpoint.NavigationEndpoint
import com.takaotech.ktravel.endpoint.NavigatorException
import com.takaotech.ktravel.endpoint.ProviderCatalog
import com.takaotech.ktravel.endpoint.ProviderCredentials
import com.takaotech.ktravel.endpoint.here.HereRoutingCall
import com.takaotech.ktravel.endpoint.here.HereRoutingEndpoint
import com.takaotech.ktravel.endpoint.here.HereTransitEndpoint
import com.takaotech.navigator.api.NavigatorApi
import com.takaotech.navigator.api.catalog.HealthResponse
import com.takaotech.navigator.api.catalog.ProviderProfileDescriptor
import com.takaotech.navigator.api.error.ErrorCode
import com.takaotech.navigator.api.here.HereRoutingRequest
import com.takaotech.navigator.api.here.HereTransitRouteRequest
import com.takaotech.navigator.api.here.HereTransportMode
import io.ktor.server.application.Application
// AUTH DISABLED: authentication is switched off; uncomment these imports together with every other
// `AUTH DISABLED` marker to put the routing paths back behind the bearer provider.
// import io.ktor.server.auth.authenticate
// import io.ktor.server.auth.principal
import io.ktor.server.plugins.ratelimit.rateLimit
import io.ktor.server.request.ApplicationRequest
import io.ktor.server.request.header
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.RoutingContext
import io.ktor.server.routing.get
import io.ktor.server.routing.openapi.describe
import io.ktor.server.routing.post
import io.ktor.server.routing.routing
import io.ktor.utils.io.ExperimentalKtorApi
import org.koin.ktor.ext.inject

/**
 * Every path this server answers on.
 *
 * One route per provider profile, wired by hand. There is no registry and no dispatch table: Ktor
 * already picks the handler by path, so a generic one would only add a way for
 * `/v1/here/routing/{transportMode}` to reach something that is not the HERE road endpoint. The
 * price is a line per profile here, which is the same line that would otherwise be a registry entry.
 *
 * The road profile is the one path that carries a parameter: the vehicle is the last segment rather
 * than a field of the body, so every mode is its own address and nothing has to read a payload to
 * know whether a call was a walk or a truck.
 *
 * The routing paths are the only ones behind a rate limit. `/v1/health` is left open on purpose: it
 * is what a load balancer polls to decide whether this instance is alive, it carries nothing worth
 * protecting, and a probe that can be throttled is a probe that reports an outage the server does
 * not have.
 *
 * Authentication is switched off. Every caller reaches the routing paths, and the only credential
 * left is the routing provider key in [NavigatorApi.PROVIDER_KEY_HEADER], resolved per request by
 * [providerCredentials]. What used to guard them is kept below as commented out code.
 *
 * Each route carries the operation that documents it. The tree built here *is* the OpenAPI document:
 * `ktor-server-routing-openapi` reads the paths, the methods and the security requirements off it,
 * and [OpenApi.kt][HealthOperation] supplies the prose and the schemas. A route added without an
 * operation is still documented, only without a description — which is a far smaller drift than a
 * separate file that describes an endpoint nobody serves any more.
 */
@OptIn(ExperimentalKtorApi::class)
fun Application.configureRouting(config: NavigatorServerConfig) {
    val catalog: ProviderCatalog by inject()
    val hereRouting: HereRoutingEndpoint by inject()
    val hereTransit: HereTransitEndpoint by inject()

    routing {
        get(NavigatorApi.HEALTH) {
            call.respond(HealthResponse(version = config.version))
        }.describe(HealthOperation)

        get(NavigatorApi.PROFILES) {
            call.respond(catalog.toResponse(config.version))
        }.describe(ProfilesOperation)

        // AUTH DISABLED: the routing paths used to sit inside this block. `optional` always, so an
        // unknown caller reached the handler and was refused there with the contract's error body —
        // the plugin's own challenge answers an empty 401, which a client would have to special case.
        // authenticate(NAVIGATOR_AUTH, optional = true) {
        rateLimit(NAVIGATOR_ROUTING_LIMIT) {
            post(NavigatorApi.HERE_ROUTING_TEMPLATE) {
                // AUTH DISABLED: requireCaller(config)
                // The vehicle comes from the path and the rest from the body, which is what makes
                // one endpoint out of two halves of the same request.
                val road = HereRoutingCall(transportMode(), call.receive<HereRoutingRequest>())
                respondWithRoute(hereRouting, road, config)
            }.describe(HereRoutingOperation)

            post(NavigatorApi.HERE_TRANSIT) {
                // AUTH DISABLED: requireCaller(config)
                respondWithRoute(hereTransit, call.receive<HereTransitRouteRequest>(), config)
            }.describe(HereTransitOperation)
        }
        // }
    }
}

/**
 * The vehicle this call is for, which is the last segment of the road path.
 *
 * A segment that names no mode is a request for a profile this server does not have, and it is
 * answered as such: the alternative is Ktor matching the path and the endpoint being handed a
 * default the caller never asked for.
 *
 * @throws NavigatorException [ErrorCode.INVALID_REQUEST] when the segment is not a mode this
 *   contract knows.
 */
private fun RoutingContext.transportMode(): HereTransportMode {
    val segment = call.parameters[NavigatorApi.HERE_ROUTING_TRANSPORT_MODE_PARAMETER].orEmpty()

    return HereTransportMode.fromPathSegment(segment) ?: throw NavigatorException(
        code = ErrorCode.INVALID_REQUEST,
        message = "'$segment' is not a mode this navigator routes on roads; it serves " +
            HereTransportMode.entries.joinToString { it.pathSegment },
    )
}

// AUTH DISABLED: what refused a caller this server did not know. It threw
// `NavigatorException(ErrorCode.UNAUTHENTICATED)` when the deployment required an access token and
// the request carried none this server accepts.
// private fun RoutingContext.requireCaller(config: NavigatorServerConfig) {
//     // Null when nothing was presented, and present-but-unknown when the token was not one of
//     // ours. Both are the same refusal, and neither reaches a provider.
//     if (config.requiresAccessToken && call.principal<NavigatorCaller>()?.isKnown != true) {
//         throw NavigatorException(
//             code = ErrorCode.UNAUTHENTICATED,
//             message = "This navigator only answers callers presenting a known access token",
//         )
//     }
// }

/**
 * Runs one endpoint and answers with what it produced.
 *
 * The only place a request turns into a response, so credentials are resolved in exactly one place
 * for every profile and a new endpoint cannot forget to do it. Failures are not caught here: they
 * leave as [NavigatorException] and are rendered by [configureStatusPages].
 *
 * Inline and reified because the two profiles no longer answer in one type, and `respond` picks the
 * serializer from the static type of what it is given. The alternative — one erased response type
 * for both — is exactly what this contract stopped doing.
 */
private suspend inline fun <REQ : Any, reified RES : Any> RoutingContext.respondWithRoute(
    endpoint: NavigationEndpoint<REQ, RES>,
    request: REQ,
    config: NavigatorServerConfig,
) {
    val credentials = call.request.providerCredentials(endpoint.descriptor, config)

    call.respond(endpoint.route(request, credentials))
}

/**
 * The key this call will reach the provider with.
 *
 * The caller's own key wins when they send one, which is the embedded case and stays exactly as it
 * was. A deployment that holds a key of its own covers the callers that send none — which is the
 * point of holding one: it is what lets an app talk to a remote navigator without putting a provider
 * key on every device and across every network hop.
 *
 * A profile that needs no key ignores both, so a client can keep sending the same header everywhere
 * without knowing which endpoints care.
 *
 * @throws NavigatorException [ErrorCode.MISSING_CREDENTIALS] when the profile needs a key and
 *   neither the caller nor this server has one.
 */
private fun ApplicationRequest.providerCredentials(
    descriptor: ProviderProfileDescriptor,
    config: NavigatorServerConfig,
): ProviderCredentials? {
    val key = header(NavigatorApi.PROVIDER_KEY_HEADER)?.takeIf { it.isNotBlank() }
        ?: config.providerApiKey?.takeIf { it.isNotBlank() }

    if (descriptor.requiresApiKey && key == null) {
        throw NavigatorException(
            code = ErrorCode.MISSING_CREDENTIALS,
            message = "${descriptor.profile} needs a provider key in ${NavigatorApi.PROVIDER_KEY_HEADER}, " +
                "and this navigator holds none of its own",
        )
    }

    return key?.let(::ProviderCredentials)
}
