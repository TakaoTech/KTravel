package com.takaotech.gunzou.here.common

import com.takaotech.gunzou.here.common.dto.HereErrorResponse

/**
 * Exception thrown when a HERE API returns an error.
 *
 * @property httpStatusCode HTTP status code, or 0 when the call never reached the API
 * @property errorResponse Parsed error response from the API (if available)
 */
class HereApiException(
    val httpStatusCode: Int,
    val errorResponse: HereErrorResponse? = null,
    message: String = errorResponse?.title ?: "HERE API error: $httpStatusCode",
    cause: Throwable? = null,
) : Exception(message, cause)
