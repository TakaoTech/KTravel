package com.takaotech.gunzou.api.error

import kotlinx.serialization.SerialName
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
    @SerialName("code") val code: ErrorCode,
    @SerialName("message") val message: String,
    @SerialName("providerStatus") val providerStatus: Int? = null,
    @SerialName("providerMessage") val providerMessage: String? = null,
)

/**
 * The failure modes a client is expected to handle differently.
 *
 * Deliberately small. Every entry answers a question the caller actually asks — retry, ask for
 * credentials, fall back to another provider, or show the user something — and a code that would
 * not change any of those answers belongs in [ErrorResponse.message] instead.
 *
 * Growing this enum is a change older clients feel: an unknown constant fails to decode, and a
 * client that cannot read the failure reports it as [INTERNAL] instead. That is a survivable
 * degradation and not a crash, but it is the reason a new code has to earn its place.
 */
@Serializable
enum class ErrorCode {
    /** The request did not satisfy the contract: bad coordinates, a count out of range, a bad body. */
    @SerialName("INVALID_REQUEST")
    INVALID_REQUEST,

    /**
     * The navigator does not accept calls from this caller.
     *
     * Distinct from [MISSING_CREDENTIALS] because the remedy is a different one: this is about the
     * access token that says who may use *this server*, not about the key it uses to reach a routing
     * provider on the caller's behalf. A deployment that is open to anyone never produces it.
     */
    @SerialName("UNAUTHENTICATED")
    UNAUTHENTICATED,

    /** A profile that requires a provider key was called without one, and the server holds none. */
    @SerialName("MISSING_CREDENTIALS")
    MISSING_CREDENTIALS,

    /** The provider rejected the key. Asking again with the same one will not help. */
    @SerialName("PROVIDER_UNAUTHORIZED")
    PROVIDER_UNAUTHORIZED,

    /** The provider's quota is exhausted. Retrying later may work. */
    @SerialName("PROVIDER_RATE_LIMITED")
    PROVIDER_RATE_LIMITED,

    /** The provider is down, unreachable or timed out. Retrying may work. */
    @SerialName("PROVIDER_UNAVAILABLE")
    PROVIDER_UNAVAILABLE,

    /** The request is well formed but asks for something this profile cannot do. */
    @SerialName("UNSUPPORTED_OPTION")
    UNSUPPORTED_OPTION,

    /** The provider ran and found no route between the given points. */
    @SerialName("NO_ROUTE_FOUND")
    NO_ROUTE_FOUND,

    /** The server failed on its own account. */
    @SerialName("INTERNAL")
    INTERNAL,
}
