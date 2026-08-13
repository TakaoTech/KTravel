package com.takaotech.ktravel

import com.takaotech.ktravel.endpoint.NavigationEndpoint
import com.takaotech.ktravel.endpoint.NavigatorException
import com.takaotech.ktravel.endpoint.ProviderCatalog
import com.takaotech.ktravel.endpoint.ProviderCredentials
import com.takaotech.ktravel.endpoint.here.HereCarEndpoint
import com.takaotech.navigator.api.NavigatorApi
import com.takaotech.navigator.api.catalog.HealthResponse
import com.takaotech.navigator.api.catalog.ProviderProfileDescriptor
import com.takaotech.navigator.api.error.ErrorCode
import com.takaotech.navigator.api.here.HereCarRouteRequest
import io.ktor.server.application.Application
import io.ktor.server.request.ApplicationRequest
import io.ktor.server.request.header
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.RoutingContext
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.routing
import org.koin.ktor.ext.inject

/**
 * Every path this server answers on.
 *
 * One route per provider profile, wired by hand. There is no registry and no dispatch table: Ktor
 * already picks the handler by path, so a generic one would only add a way for `/v1/here/car` to
 * reach something that is not the HERE road endpoint. The price is a line per profile here, which is
 * the same line that would otherwise be a registry entry.
 */
fun Application.configureRouting() {
    val catalog: ProviderCatalog by inject()
    val hereCar: HereCarEndpoint by inject()

    routing {
        get(NavigatorApi.HEALTH) {
            call.respond(HealthResponse(version = NAVIGATOR_VERSION))
        }

        get(NavigatorApi.PROFILES) {
            call.respond(catalog.toResponse())
        }

        post(NavigatorApi.HERE_CAR) {
            respondWithRoute(hereCar, call.receive<HereCarRouteRequest>())
        }
    }
}

/**
 * Runs one endpoint and answers with its routes.
 *
 * The only place a request turns into a response, so credentials are checked in exactly one place
 * for every profile and a new endpoint cannot forget to do it. Failures are not caught here: they
 * leave as [NavigatorException] and are rendered by [configureStatusPages].
 */
private suspend fun <REQ : Any> RoutingContext.respondWithRoute(endpoint: NavigationEndpoint<REQ>, request: REQ) {
    val credentials = call.request.providerCredentials(endpoint.descriptor)

    call.respond(endpoint.route(request, credentials))
}

/**
 * Reads the caller's provider key off the request.
 *
 * A profile that needs no key ignores the header even when one is sent, so a client can keep sending
 * the same header to every endpoint without having to know which of them cares.
 *
 * @throws NavigatorException [ErrorCode.MISSING_CREDENTIALS] when the profile requires a key and
 *   none arrived.
 */
private fun ApplicationRequest.providerCredentials(descriptor: ProviderProfileDescriptor): ProviderCredentials? {
    val key = header(NavigatorApi.PROVIDER_KEY_HEADER)?.takeIf { it.isNotBlank() }

    if (descriptor.requiresApiKey && key == null) {
        throw NavigatorException(
            code = ErrorCode.MISSING_CREDENTIALS,
            message = "${descriptor.profile} requires a provider key in ${NavigatorApi.PROVIDER_KEY_HEADER}",
        )
    }

    return key?.let(::ProviderCredentials)
}
