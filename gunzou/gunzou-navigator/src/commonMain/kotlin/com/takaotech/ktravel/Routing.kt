package com.takaotech.ktravel

import com.takaotech.ktravel.endpoint.NavigationEndpoint
import com.takaotech.ktravel.endpoint.NavigatorException
import com.takaotech.ktravel.endpoint.ProviderCatalog
import com.takaotech.ktravel.endpoint.ProviderCredentials
import com.takaotech.ktravel.endpoint.here.HereCarEndpoint
import com.takaotech.ktravel.endpoint.here.HereTransitEndpoint
import com.takaotech.navigator.api.NavigatorApi
import com.takaotech.navigator.api.catalog.HealthResponse
import com.takaotech.navigator.api.catalog.ProviderProfileDescriptor
import com.takaotech.navigator.api.error.ErrorCode
import com.takaotech.navigator.api.here.HereCarRouteRequest
import com.takaotech.navigator.api.here.HereTransitRouteRequest
import io.ktor.server.application.Application
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.principal
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
 * already picks the handler by path, so a generic one would only add a way for `/v1/here/car` to
 * reach something that is not the HERE road endpoint. The price is a line per profile here, which is
 * the same line that would otherwise be a registry entry.
 *
 * The routing paths are the only ones behind authentication and a rate limit. `/v1/health` is left
 * open on purpose: it is what a load balancer polls to decide whether this instance is alive, it
 * carries nothing worth protecting, and a probe that has to hold a token — or that can be throttled
 * — is a probe that reports an outage the server does not have.
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
    val hereCar: HereCarEndpoint by inject()
    val hereTransit: HereTransitEndpoint by inject()

    routing {
        get(NavigatorApi.HEALTH) {
            call.respond(HealthResponse(version = config.version))
        }.describe(HealthOperation)

        get(NavigatorApi.PROFILES) {
            call.respond(catalog.toResponse(config.version))
        }.describe(ProfilesOperation)

        // `optional` always, so an unknown caller reaches the handler and is refused there with the
        // contract's error body. The plugin's own challenge answers an empty 401, which a client
        // would have to special case.
        authenticate(NAVIGATOR_AUTH, optional = true) {
            rateLimit(NAVIGATOR_ROUTING_LIMIT) {
                post(NavigatorApi.HERE_CAR) {
                    requireCaller(config)
                    respondWithRoute(hereCar, call.receive<HereCarRouteRequest>(), config)
                }.describe(HereCarOperation)

                post(NavigatorApi.HERE_TRANSIT) {
                    requireCaller(config)
                    respondWithRoute(hereTransit, call.receive<HereTransitRouteRequest>(), config)
                }.describe(HereTransitOperation)
            }
        }
    }
}

/**
 * Refuses a caller this server does not know.
 *
 * @throws NavigatorException [ErrorCode.UNAUTHENTICATED] when the deployment requires an access
 *   token and the request carried none this server accepts.
 */
private fun RoutingContext.requireCaller(config: NavigatorServerConfig) {
    // Null when nothing was presented, and present-but-unknown when the token was not one of ours.
    // Both are the same refusal, and neither reaches a provider.
    if (config.requiresAccessToken && call.principal<NavigatorCaller>()?.isKnown != true) {
        throw NavigatorException(
            code = ErrorCode.UNAUTHENTICATED,
            message = "This navigator only answers callers presenting a known access token",
        )
    }
}

/**
 * Runs one endpoint and answers with its routes.
 *
 * The only place a request turns into a response, so credentials are resolved in exactly one place
 * for every profile and a new endpoint cannot forget to do it. Failures are not caught here: they
 * leave as [NavigatorException] and are rendered by [configureStatusPages].
 */
private suspend fun <REQ : Any> RoutingContext.respondWithRoute(
    endpoint: NavigationEndpoint<REQ>,
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
