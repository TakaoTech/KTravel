package com.takaotech.ktravel.gunzou.server.endpoint.here

import com.takaotech.gunzou.api.error.ErrorCode
import com.takaotech.gunzou.here.common.HereApiResult
import com.takaotech.ktravel.gunzou.server.endpoint.NavigatorException

private const val HTTP_BAD_REQUEST = 400
private const val HTTP_UNAUTHORIZED = 401
private const val HTTP_FORBIDDEN = 403
private const val HTTP_NOT_FOUND = 404
private const val HTTP_TOO_MANY_REQUESTS = 429
private const val HTTP_SERVER_ERROR_FLOOR = 500

/**
 * Unwraps a HERE result, translating its failures into the contract's.
 *
 * The translation is the point of the server: a caller must be able to tell "your key is wrong" from
 * "HERE is down" from "there is no route" without knowing that HERE exists, let alone which of its
 * status codes means what. The upstream's own status and message are carried along unchanged, so the
 * information is available to whoever is debugging without being what the client branches on.
 */
fun <T> HereApiResult<T>.orThrowNavigatorException(): T = when (this) {
    is HereApiResult.Success -> data

    is HereApiResult.Error -> throw NavigatorException(
        code = httpStatusCode.toErrorCode(),
        message = errorResponse?.title ?: "The HERE API call failed",
        // Zero is what the client reports when the call never reached HERE, and it is not an HTTP
        // status: sending it on would be inventing one.
        providerStatus = httpStatusCode.takeIf { it != 0 },
        providerMessage = errorResponse?.cause ?: errorResponse?.title,
        cause = exception,
    )
}

/**
 * What a HERE status means in the contract's terms.
 *
 * A 400 becomes [ErrorCode.INVALID_REQUEST] even though the request that reached this server passed
 * validation: it means the query built from it was not one HERE accepts, which is a combination the
 * caller chose. The message carries HERE's explanation of which part.
 */
private fun Int.toErrorCode(): ErrorCode = when {
    this == HTTP_UNAUTHORIZED || this == HTTP_FORBIDDEN -> ErrorCode.PROVIDER_UNAUTHORIZED

    this == HTTP_TOO_MANY_REQUESTS -> ErrorCode.PROVIDER_RATE_LIMITED

    this == HTTP_BAD_REQUEST -> ErrorCode.INVALID_REQUEST

    this == HTTP_NOT_FOUND -> ErrorCode.NO_ROUTE_FOUND

    this >= HTTP_SERVER_ERROR_FLOOR -> ErrorCode.PROVIDER_UNAVAILABLE

    // Zero, and anything else unforeseen: the call did not produce an answer, and retrying is the
    // only thing the caller can usefully do.
    else -> ErrorCode.PROVIDER_UNAVAILABLE
}

/**
 * Rejects an answer that carries no route.
 *
 * HERE reports this as a success with an empty list, which is a shape a client would have to check
 * for separately from the failures. Turning it into an error means there is one place where routes
 * can be absent, and [com.takaotech.gunzou.api.response.RouteResponse.routes] is never empty.
 */
fun requireRouteFound(routeCount: Int) {
    if (routeCount == 0) {
        throw NavigatorException(
            code = ErrorCode.NO_ROUTE_FOUND,
            message = "The provider found no route between the requested places",
        )
    }
}
