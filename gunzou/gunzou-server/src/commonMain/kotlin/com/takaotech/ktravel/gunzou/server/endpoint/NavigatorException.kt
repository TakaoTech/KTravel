package com.takaotech.ktravel.gunzou.server.endpoint

import com.takaotech.gunzou.api.error.ErrorCode
import com.takaotech.gunzou.api.error.ErrorResponse

/**
 * A failure the caller is meant to act on, carrying everything the contract promises about it.
 *
 * The only exception the server throws on purpose. Everything else that escapes an endpoint is a bug
 * and is reported as [ErrorCode.INTERNAL] with no detail, because the detail of a bug is for the
 * server's log and not for a client that cannot do anything with it.
 *
 * @property code What went wrong, in the contract's terms.
 * @property providerStatus HTTP status the upstream answered with, when the failure came from one.
 * @property providerMessage The upstream's own error text, kept verbatim.
 */
class NavigatorException(
    val code: ErrorCode,
    override val message: String,
    val providerStatus: Int? = null,
    val providerMessage: String? = null,
    cause: Throwable? = null,
) : Exception(message, cause) {
    /** The body to answer with. */
    fun toErrorResponse(): ErrorResponse = ErrorResponse(
        code = code,
        message = message,
        providerStatus = providerStatus,
        providerMessage = providerMessage,
    )
}
