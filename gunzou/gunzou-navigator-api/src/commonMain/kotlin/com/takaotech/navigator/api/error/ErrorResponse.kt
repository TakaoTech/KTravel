package com.takaotech.navigator.api.error

import kotlinx.serialization.Serializable

/**
 * The body of every failing response, whatever the status code.
 *
 * A client decides what to do from [code] alone; the free text is for logs and for the developer
 * reading them. [providerStatus] and [providerMessage] are the upstream's own words, kept
 * unmodified: when a provider rejects a request the useful sentence is almost always theirs, and
 * losing it turns a five minute fix into an afternoon.
 *
 * @property code What went wrong, in this contract's terms.
 * @property message Human readable detail. Never localized: it is not shown to a traveller.
 * @property providerStatus HTTP status the provider answered with, when the failure came from one.
 * @property providerMessage The provider's own error text, when it sent one.
 */
@Serializable
data class ErrorResponse(
    val code: ErrorCode,
    val message: String,
    val providerStatus: Int? = null,
    val providerMessage: String? = null,
)

/**
 * The failure modes a client is expected to handle differently.
 *
 * Deliberately small. Every entry answers a question the caller actually asks — retry, ask for
 * credentials, fall back to another provider, or show the user something — and a code that would
 * not change any of those answers belongs in [ErrorResponse.message] instead.
 */
@Serializable
enum class ErrorCode {
    /** The request did not satisfy the contract: bad coordinates, a count out of range, a bad body. */
    INVALID_REQUEST,

    /** A profile that requires a provider key was called without one. */
    MISSING_CREDENTIALS,

    /** The provider rejected the key. Asking again with the same one will not help. */
    PROVIDER_UNAUTHORIZED,

    /** The provider's quota is exhausted. Retrying later may work. */
    PROVIDER_RATE_LIMITED,

    /** The provider is down, unreachable or timed out. Retrying may work. */
    PROVIDER_UNAVAILABLE,

    /** The request is well formed but asks for something this profile cannot do. */
    UNSUPPORTED_OPTION,

    /** The provider ran and found no route between the given points. */
    NO_ROUTE_FOUND,

    /** The server failed on its own account. */
    INTERNAL,
}
