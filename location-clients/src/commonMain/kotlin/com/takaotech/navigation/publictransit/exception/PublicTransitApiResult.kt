package com.takaotech.navigation.publictransit.exception

/**
 * Sealed class representing the result of a Public Transit API call.
 */
sealed class PublicTransitApiResult<out T> {
    /**
     * Successful API response.
     */
    data class Success<T>(val data: T) : PublicTransitApiResult<T>()

    /**
     * Error response from the API.
     */
    data class Error(
        val httpStatusCode: Int,
        val errorResponse: com.takaotech.navigation.publictransit.dto.response.ErrorResponse? = null,
        val exception: Exception? = null
    ) : PublicTransitApiResult<Nothing>()

    /**
     * Returns true if this is a successful result.
     */
    val isSuccess: Boolean get() = this is Success

    /**
     * Returns true if this is an error result.
     */
    val isError: Boolean get() = this is Error

    /**
     * Returns the data if successful, null otherwise.
     */
    fun getOrNull(): T? = when (this) {
        is Success -> data
        is Error -> null
    }

    /**
     * Returns the data if successful, throws exception otherwise.
     */
    fun getOrThrow(): T = when (this) {
        is Success -> data
        is Error -> throw exception
            ?: PublicTransitApiException(
                errorResponse,
                httpStatusCode
            )
    }
}
