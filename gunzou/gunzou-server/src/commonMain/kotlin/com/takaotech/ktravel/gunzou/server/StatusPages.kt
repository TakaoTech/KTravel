package com.takaotech.ktravel.gunzou.server

import com.takaotech.gunzou.api.error.ErrorCode
import com.takaotech.gunzou.api.error.ErrorResponse
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.plugins.BadRequestException
import io.ktor.server.plugins.requestvalidation.RequestValidationException
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.response.respond

/**
 * Turns every failure into the one body the contract defines.
 *
 * A client only ever branches on [ErrorCode], so the important property here is that nothing escapes
 * as an untyped 500 with a stack trace in it: a malformed body, a validation failure and a bug in an
 * endpoint all leave through this plugin as an [ErrorResponse].
 */
fun Application.configureStatusPages() {
    install(StatusPages) {
        exception<com.takaotech.ktravel.gunzou.server.endpoint.NavigatorException> { call, cause ->
            appLog.w(cause) { "${cause.code}: ${cause.message}" }
            call.respond(cause.code.toHttpStatus(), cause.toErrorResponse())
        }

        // Thrown by the RequestValidation plugin. Its reasons are written for a developer and are
        // safe to pass on: they describe the request the caller just sent, and nothing else.
        exception<RequestValidationException> { call, cause ->
            call.respond(
                HttpStatusCode.BadRequest,
                ErrorResponse(
                    code = ErrorCode.INVALID_REQUEST,
                    message = cause.reasons.joinToString("; ").ifEmpty { "The request is not valid" },
                ),
            )
        }

        // What `call.receive` throws when the body is not the JSON the endpoint expects. The
        // deserializer's own message names the offending field, which is the one thing that makes
        // a 400 actionable, so it is forwarded rather than swallowed.
        exception<BadRequestException> { call, cause ->
            call.respond(
                HttpStatusCode.BadRequest,
                ErrorResponse(
                    code = ErrorCode.INVALID_REQUEST,
                    message = cause.cause?.message ?: cause.message ?: "The request body could not be read",
                ),
            )
        }

        // Anything else is a bug. The detail goes to the log, where it can be fixed; the caller gets
        // nothing beyond the code, because there is nothing they could do with a stack trace and it
        // is not worth the chance of leaking what is in it.
        exception<Throwable> { call, cause ->
            appLog.e(cause) { "Unhandled failure while serving ${call.request.local.uri}" }
            call.respond(
                HttpStatusCode.InternalServerError,
                ErrorResponse(code = ErrorCode.INTERNAL, message = "The server failed to handle the request"),
            )
        }

        // The rate limit plugin refuses with a bare 429 and no body. The contract promises that
        // every failure carries an ErrorResponse, and without one a throttled client reads the
        // answer as an unparseable failure and loses the one piece of information that would tell
        // it to back off rather than retry immediately.
        status(HttpStatusCode.TooManyRequests) { call, status ->
            call.respond(
                status,
                ErrorResponse(
                    code = ErrorCode.PROVIDER_RATE_LIMITED,
                    message = "Too many requests from this caller; retry later",
                ),
            )
        }

        // A request to a path nobody serves. Answered with the same body as everything else so a
        // client never has to parse two shapes, and with INVALID_REQUEST because from the contract's
        // point of view that is what it is: a call that names an endpoint which does not exist.
        status(HttpStatusCode.NotFound) { call, status ->
            call.respond(
                status,
                ErrorResponse(
                    code = ErrorCode.INVALID_REQUEST,
                    message = "No endpoint at ${call.request.local.uri}",
                ),
            )
        }
    }
}

/**
 * The status each failure is reported with.
 *
 * [ErrorCode.NO_ROUTE_FOUND] is deliberately not a 404: the endpoint exists and ran, and reusing 404
 * would make it indistinguishable from a call to a path that does not exist for any client looking
 * at the status alone.
 */
private fun ErrorCode.toHttpStatus(): HttpStatusCode = when (this) {
    ErrorCode.INVALID_REQUEST -> HttpStatusCode.BadRequest

    // Unauthenticated is a 401 and missing provider credentials is too, which is not a collision to
    // resolve: both mean "a credential is missing", and which one the caller has to go and find is
    // exactly what the code in the body tells them.
    ErrorCode.UNAUTHENTICATED -> HttpStatusCode.Unauthorized

    ErrorCode.MISSING_CREDENTIALS -> HttpStatusCode.Unauthorized

    ErrorCode.PROVIDER_UNAUTHORIZED -> HttpStatusCode.Unauthorized

    ErrorCode.PROVIDER_RATE_LIMITED -> HttpStatusCode.TooManyRequests

    ErrorCode.PROVIDER_UNAVAILABLE -> HttpStatusCode.BadGateway

    ErrorCode.UNSUPPORTED_OPTION -> HttpStatusCode.UnprocessableEntity

    ErrorCode.NO_ROUTE_FOUND -> HttpStatusCode.UnprocessableEntity

    ErrorCode.INTERNAL -> HttpStatusCode.InternalServerError
}
