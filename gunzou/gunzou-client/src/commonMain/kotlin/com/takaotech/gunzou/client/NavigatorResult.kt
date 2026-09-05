package com.takaotech.gunzou.client

import com.takaotech.gunzou.api.error.ErrorResponse

/**
 * What came back from a call to gunzo-navigator.
 *
 * Three cases and not two, because the third is the one a caller acts on differently. A
 * [ServerError] means the navigator answered and said no — the key is wrong, there is no route, the
 * option is unsupported — and repeating the call changes nothing. A [TransportError] means it never
 * answered, which for an embedded server usually means the process was suspended and the socket is
 * gone: the response is to restart it and try again, and there is no way to reach that conclusion
 * from a failure that has been flattened into one shape.
 */
sealed interface NavigatorResult<out T> {

    /**
     * The navigator answered.
     *
     * @property value What it answered with.
     */
    data class Success<T>(val value: T) : NavigatorResult<T>

    /**
     * The navigator answered with a failure.
     *
     * @property status The HTTP status it used.
     * @property error The contract's own account of what went wrong, which is what to branch on.
     */
    data class ServerError(val status: Int, val error: ErrorResponse) : NavigatorResult<Nothing>

    /**
     * The navigator was not reached, or what came back was not the contract.
     *
     * @property cause The connection failure, timeout or decoding failure underneath.
     */
    data class TransportError(val cause: Throwable) : NavigatorResult<Nothing>

    /** The value, or null for either kind of failure. */
    fun getOrNull(): T? = (this as? Success)?.value
}

/**
 * The value, or the failure as an exception.
 *
 * For call sites that have no per failure behaviour and only want the happy path to read straight.
 */
fun <T> NavigatorResult<T>.getOrThrow(): T = when (this) {
    is NavigatorResult.Success -> value

    is NavigatorResult.ServerError -> throw NavigatorException(message = "${error.code}: ${error.message}")

    is NavigatorResult.TransportError -> throw NavigatorException(
        message = "Could not reach the navigator: ${cause.message}",
        cause = cause,
    )
}

/** Thrown by [getOrThrow]. Carries no contract of its own: the result does. */
class NavigatorException(message: String, cause: Throwable? = null) : Exception(message, cause)
