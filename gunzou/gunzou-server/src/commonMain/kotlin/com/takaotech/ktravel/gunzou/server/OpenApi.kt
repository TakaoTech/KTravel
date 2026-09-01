package com.takaotech.ktravel.gunzou.server

import com.takaotech.gunzou.api.NavigatorApi
import com.takaotech.gunzou.api.catalog.HealthResponse
import com.takaotech.gunzou.api.catalog.ProviderCatalogResponse
import com.takaotech.gunzou.api.error.ErrorCode
import com.takaotech.gunzou.api.error.ErrorResponse
import com.takaotech.gunzou.api.here.HereRoutingRequest
import com.takaotech.gunzou.api.here.HereTransitRouteRequest
import com.takaotech.gunzou.api.response.RoutingRouteResponse
import com.takaotech.gunzou.api.response.TransitJourneyResponse
import io.ktor.http.HttpStatusCode
import io.ktor.openapi.JsonSchema
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

            There is a path per provider API, each with its own fully typed body, because the inputs
            of a road router and of a timetable have nothing in common and flattening them would
            mean a denominator that fits neither. The answers follow the same rule: `RoutingRouteResponse`
            for a route on roads, `TransitJourneyResponse` for a journey on scheduled services.

            The two were one shape for a while, and what that bought was every road section carrying
            an empty list of stops and every journey step an empty list of tolls, with no way for a
            reader to tell a field that is off from one that does not apply. What is genuinely shared
            — a duration, a manoeuvre, an advisory — is shared, and nothing else is.

            The rule for new paths and new answers is one per distinct *family* of API, not one per
            provider: a second road engine answers in `RoutingRouteResponse`, and an OpenTripPlanner in
            `TransitJourneyResponse`. Within the road profile the mode of transport is the last
            segment of the path — `${NavigatorApi.HERE_ROUTING_TEMPLATE}` — so a car and a bicycle
            route are addressed apart while remaining one upstream API and one body.
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

/** `POST /v1/here/routing/{transportMode}`. */
internal val HereRoutingOperation: RouteOperationFunction = {
    tag(TAG_ROUTING)
    summary = "Route on roads, whatever the vehicle"
    description = """
        One path per mode of transport — `car`, `truck`, `pedestrian`, `bicycle`, `scooter`, `taxi`,
        `bus`, `privateBus` — all served by the same upstream API and taking the same body.

        A mode that pays no toll, which is `pedestrian` and `bicycle`, refuses to be asked about
        them: neither `TOLLS` among the return attributes nor `TOLL_ROAD` among the features to
        avoid, since both are empty by construction and the call upstream is paid for all the same.
    """.trimIndent()

    providerKeyHeader()
    requestBody {
        required = true
        schema = jsonSchema<HereRoutingRequest>()
    }
    routeResponses(jsonSchema<RoutingRouteResponse>(), "The alternatives, best first")
}

/** `POST /v1/here/transit`. */
internal val HereTransitOperation: RouteOperationFunction = {
    tag(TAG_ROUTING)
    summary = "Route on public transport"
    description = """
        A separate path from the road profile because it is a separate upstream API, with parameters
        that have no meaning on the other one — there is no `changes` on a car route and no
        `routingMode` on a timetable.

        The answer is separate too. A journey is a sequence of departures the traveller has to be at
        on time, run by operators, calling at stops; each of its steps is either a `walk` or a `ride`,
        discriminated on `type`, which is the same shape the upstream API uses and the reason a
        client's handling of the two cannot silently fall through.
    """.trimIndent()

    providerKeyHeader()
    requestBody {
        required = true
        schema = jsonSchema<HereTransitRouteRequest>()
    }
    routeResponses(jsonSchema<TransitJourneyResponse>(), "The journeys, best first")
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
 * The success schema is a parameter because the two profiles no longer answer in one type; the
 * failures are what they still share, and listing them once is the point of this function.
 *
 * The failures are listed one by one rather than folded into a `default` response because the status
 * is half of what a client branches on, and a document that only promises "some error" is one a
 * generated client cannot type. Every one of them carries [ErrorResponse]: that is the contract, and
 * [configureStatusPages] is what makes it true of the responses this server has not thought about
 * either.
 */
private fun Operation.Builder.routeResponses(success: JsonSchema, meaning: String) {
    responses {
        response(HttpStatusCode.OK.value) {
            description = meaning
            schema = success
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
