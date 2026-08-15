package com.takaotech.ktravel

import com.takaotech.navigator.api.NavigatorApi
import com.takaotech.navigator.api.catalog.HealthResponse
import com.takaotech.navigator.api.catalog.ProviderCatalogResponse
import com.takaotech.navigator.api.error.ErrorCode
import com.takaotech.navigator.api.error.ErrorResponse
import com.takaotech.navigator.api.here.HereCarRouteRequest
import com.takaotech.navigator.api.here.HereTransitRouteRequest
import com.takaotech.navigator.api.response.RouteResponse
import io.ktor.http.HttpStatusCode
import io.ktor.openapi.OpenApiDoc
import io.ktor.openapi.OpenApiDocDsl
import io.ktor.openapi.OpenApiInfo
import io.ktor.openapi.Operation
import io.ktor.openapi.jsonSchema
import io.ktor.server.application.Application
import io.ktor.server.routing.openapi.RouteOperationFunction
// AUTH DISABLED: import io.ktor.server.routing.openapi.findSecuritySchemes
import io.ktor.server.routing.openapi.plus
import io.ktor.server.routing.routingRoot

/*
 * Everything the OpenAPI document says about this server.
 *
 * The document is generated from the routing tree by `ktor-server-routing-openapi`: the paths, the
 * methods, the header and query parameters and the security requirement of each operation are read
 * off the routes themselves, and every schema is inferred from the `kotlinx.serialization`
 * descriptor of the type the endpoint actually receives or answers with. What is left — the prose,
 * the tags and the status codes an endpoint can fail with — is what this file supplies.
 *
 * That split is the point of generating rather than writing the document. The hand written
 * `documentation.yaml` this replaces had already drifted from the contract it described: it declared
 * a `ProviderCatalogResponse` without the `version` field the type has carried for some time, and
 * nothing in the build could notice. A schema that comes from the serializer cannot drift, because
 * it is the same descriptor the endpoint encodes through.
 *
 * The descriptions live here rather than beside each route so that routing stays about routing, and
 * so that the whole document reads in one place — which is the one property the single YAML file
 * did have.
 */

/** Computing routes. Rate limited. */
private const val TAG_ROUTING = "routing"

/** What this server can do, and whether it is alive. */
private const val TAG_DISCOVERY = "discovery"

/**
 * The parts of the document that describe the server rather than one of its paths.
 *
 * Written against [OpenApiDocDsl] rather than against a concrete builder so the same description
 * serves Swagger UI on the JVM and the plain document a test or another target asks for.
 *
 * The security schemes are deliberately absent: `ktor-server-routing-openapi` infers them from the
 * installed `Authentication` providers, so the bearer scheme was described once — where it is
 * configured, in [configureSecurity] — instead of once there and once here. With authentication
 * switched off there is no provider installed and therefore no scheme to publish.
 *
 * @param version The build being documented, which is the build answering.
 */
internal fun OpenApiDocDsl.navigatorApiDocument(version: String) {
    info = OpenApiInfo(
        title = "gunzo-navigator",
        version = version,
        description = """
            One HTTP contract in front of several routing engines.

            The requests are asymmetric on purpose: there is a path per provider API, each with its
            own fully typed body, because the inputs of a road router and of a timetable have nothing
            in common and flattening them would mean a denominator that fits neither. The *answers*
            are one shape, `RouteResponse`, and that is where the value is — a client draws a route
            without knowing which engine produced it.

            The rule for new paths is one path per distinct provider API, never one per mode of
            transport: a car and a bicycle route are the same API with a different field, so they
            share `${NavigatorApi.HERE_CAR}`.
        """.trimIndent(),
    )

    servers {
        server("http://127.0.0.1:8080") {
            description = "Embedded in the application, on a port the operating system assigns"
        }
        server("https://navigator.example.com") {
            description = "Remote deployment"
        }
    }

    tag(TAG_ROUTING)
    tag(TAG_DISCOVERY)
}

/**
 * The whole document, as this application would serve it.
 *
 * Assembled from what this file says and what the routing tree carries, so that a caller that is not
 * Swagger UI can get the same document Swagger UI renders. The third source the library keeps
 * separate — what the installed authentication providers imply — is switched off with them.
 *
 * Reads the routing tree, so it is only meaningful once the routes are installed.
 *
 * @param version The build being documented.
 */
internal fun Application.navigatorOpenApiDoc(version: String): OpenApiDoc =
    // AUTH DISABLED: `+ findSecuritySchemes()` published the bearer scheme inferred from the
    // installed provider. There is none to infer while authentication is off.
    OpenApiDoc.build { navigatorApiDocument(version) } +
//            + findSecuritySchemes()
        routingRoot.descendants()

/**
 * `GET /v1/health`.
 *
 * Outside the rate limit, and — like every other path now that authentication is switched off —
 * outside any security requirement, which the generated document says on its own.
 */
internal val HealthOperation: RouteOperationFunction = {
    tag(TAG_DISCOVERY)
    summary = "Whether the server is answering"
    description = """
        Liveness only: it says nothing about the providers behind it. Checking those would turn a
        health probe into a call to a paid third party on every poll. Deliberately outside the rate
        limit, so a probe never reports an outage the server does not have.
    """.trimIndent()

    responses {
        response(HttpStatusCode.OK.value) {
            description = "Serving"
            schema = jsonSchema<HealthResponse>()
        }
    }
}

/** `GET /v1/profiles`. */
internal val ProfilesOperation: RouteOperationFunction = {
    tag(TAG_DISCOVERY)
    summary = "What this server can route with"
    description = """
        What lets a client draw a provider selector from the server instead of from a `when` it would
        have to ship an update to change. A profile is listed because an endpoint exists for it.
    """.trimIndent()

    responses {
        response(HttpStatusCode.OK.value) {
            description = "The mounted profiles"
            schema = jsonSchema<ProviderCatalogResponse>()
        }
    }
}

/** `POST /v1/here/car`. */
internal val HereCarOperation: RouteOperationFunction = {
    tag(TAG_ROUTING)
    summary = "Route on roads, whatever the vehicle"
    description = """
        `car` names the profile, not the vehicle: pedestrian, bicycle and truck routes all come from
        the same upstream API with a different `transportMode`.
    """.trimIndent()

    providerKeyHeader()
    requestBody {
        required = true
        schema = jsonSchema<HereCarRouteRequest>()
    }
    routeResponses()
}

/** `POST /v1/here/transit`. */
internal val HereTransitOperation: RouteOperationFunction = {
    tag(TAG_ROUTING)
    summary = "Route on public transport"
    description = """
        A separate path from the road profile because it is a separate upstream API, with parameters
        that have no meaning on the other one — there is no `changes` on a car route and no
        `routingMode` on a timetable.
    """.trimIndent()

    providerKeyHeader()
    requestBody {
        required = true
        schema = jsonSchema<HereTransitRouteRequest>()
    }
    routeResponses()
}

/**
 * The header every routing endpoint reads credentials from.
 *
 * Optional, and optional in both directions: a caller that sends none reaches a deployment holding a
 * key of its own, and a profile that needs no key ignores it either way.
 */
private fun Operation.Builder.providerKeyHeader() {
    parameters {
        header(NavigatorApi.PROVIDER_KEY_HEADER) {
            required = false
            description = """
                The caller's own key for the routing provider behind the requested profile.

                Embedded, the server runs inside the application that owns the key and this is how it
                gets there. A remote deployment can instead hold a key of its own
                (`NAVIGATOR_PROVIDER_API_KEY`), which is what stops a provider key having to live on
                every device and cross the network on every request; a key sent here still wins when
                both exist.
            """.trimIndent()
        }
    }
}

/**
 * What a routing endpoint can answer, in every case.
 *
 * The failures are listed one by one rather than folded into a `default` response because the status
 * is half of what a client branches on, and a document that only promises "some error" is one a
 * generated client cannot type. Every one of them carries [ErrorResponse]: that is the contract, and
 * [configureStatusPages] is what makes it true of the responses this server has not thought about
 * either.
 */
private fun Operation.Builder.routeResponses() {
    responses {
        response(HttpStatusCode.OK.value) {
            description = "The alternatives, best first"
            schema = jsonSchema<RouteResponse>()
        }

        for ((status, meaning) in ROUTING_FAILURES) {
            response(status.value) {
                description = meaning
                schema = jsonSchema<ErrorResponse>()
            }
        }
    }
}

/**
 * The statuses a routing endpoint refuses with, each named in the contract's own terms.
 *
 * The sentence is the [ErrorCode] a client will find in the body rather than the reason phrase of
 * the status, because the status alone does not say which of them it is: a `401` is two different
 * remedies — send a provider key, or replace the one that was rejected — and only the code
 * distinguishes them. See [ErrorCode] for the mapping this mirrors.
 *
 * `404` is absent on purpose: the endpoints exist, and the one thing this server answers a `404` to
 * is a path nobody serves, which belongs to no operation.
 */
private val ROUTING_FAILURES: List<Pair<HttpStatusCode, String>> = listOf(
    HttpStatusCode.BadRequest to
        "INVALID_REQUEST: the body or a parameter does not satisfy the contract",
    // AUTH DISABLED: UNAUTHENTICATED was the third code this status carried, and nothing raises it
    // while the bearer provider is switched off.
    HttpStatusCode.Unauthorized to
        "MISSING_CREDENTIALS or PROVIDER_UNAUTHORIZED: which credential is missing, and from whom, " +
        "is what the code in the body says",
    HttpStatusCode.UnprocessableEntity to
        "UNSUPPORTED_OPTION or NO_ROUTE_FOUND: the request is well formed and could not be served",
    HttpStatusCode.TooManyRequests to
        "PROVIDER_RATE_LIMITED: this caller has asked for too much; retry later",
    HttpStatusCode.BadGateway to
        "PROVIDER_UNAVAILABLE: the routing engine behind this profile did not answer",
)
