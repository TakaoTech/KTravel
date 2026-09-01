package com.takaotech.gunzou.here.common

import com.takaotech.gunzou.here.common.dto.HereErrorResponse

/**
 * Result of a call to any HERE API.
 */
sealed class HereApiResult<out T> {
    /**
     * Successful API response.
     *
     * @property data Deserialized response payload
     */
    data class Success<T>(val data: T) : HereApiResult<T>()

    /**
     * API error response.
     *
     * @property httpStatusCode HTTP status code, or 0 when the call never reached the API
     * @property errorResponse Error payload, either parsed from the response or synthesised locally
     * @property exception Underlying failure, when the call failed before a response was read
     */
    data class Error(
        val httpStatusCode: Int,
        val errorResponse: HereErrorResponse? = null,
        val exception: Throwable? = null,
    ) : HereApiResult<Nothing>()

    /**
     * Returns true if this is a successful result.
     */
    val isSuccess: Boolean get() = this is Success

    /**
     * Returns true if this is an error result.
     */
    val isError: Boolean get() = this is Error

    /**
     * Returns the data if successful, or null if error.
     */
    fun getOrNull(): T? = when (this) {
        is Success -> data
        is Error -> null
    }

    /**
     * Returns the data if successful, or throws a [HereApiException] carrying the original failure
     * as its cause.
     */
    fun getOrThrow(): T = when (this) {
        is Success -> data

        is Error -> throw HereApiException(
            httpStatusCode = httpStatusCode,
            errorResponse = errorResponse,
            cause = exception,
        )
    }
}
