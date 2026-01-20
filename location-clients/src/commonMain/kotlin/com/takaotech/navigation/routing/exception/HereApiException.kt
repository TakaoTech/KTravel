package com.takaotech.navigation.routing.exception

/**
 * Exception thrown when HERE API returns an error.
 *
 * @property httpStatusCode HTTP status code
 * @property errorResponse Parsed error response from API (if available)
 */
class HereApiException(
    val httpStatusCode: Int,
    val errorResponse: com.takaotech.navigation.routing.dto.response.ErrorResponse? = null,
    message: String = errorResponse?.title ?: "HERE API error: $httpStatusCode",
    cause: Throwable? = null
) : Exception(message, cause)

/**
 * Sealed class representing the result of a HERE API call.
 */
sealed class HereApiResult<out T> {
    /**
     * Successful API response.
     */
    data class Success<T>(val data: T) : HereApiResult<T>()

    /**
     * API error response.
     */
    data class Error(
        val httpStatusCode: Int,
        val errorResponse: com.takaotech.navigation.routing.dto.response.ErrorResponse? = null,
        val exception: Throwable? = null
    ) : HereApiResult<Nothing>()

    /**
     * Returns the data if successful, or null if error.
     */
    fun getOrNull(): T? = when (this) {
        is Success -> data
        is Error -> null
    }

    /**
     * Returns the data if successful, or throws the exception.
     */
    fun getOrThrow(): T = when (this) {
        is Success -> data
        is Error -> throw HereApiException(
            httpStatusCode = httpStatusCode,
            errorResponse = errorResponse,
            cause = exception
        )
    }

    /**
     * Returns true if this is a successful result.
     */
    val isSuccess: Boolean get() = this is Success

    /**
     * Returns true if this is an error result.
     */
    val isError: Boolean get() = this is Error
}
