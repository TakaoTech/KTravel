package com.takaotech.navigation.common.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Error response returned by any HERE API.
 *
 * Every field is optional: the APIs document them as always present, but an error produced by a
 * gateway rather than by the service itself may carry only part of them.
 *
 * @property title Human-readable error title
 * @property status HTTP status code
 * @property code Machine-readable error code
 * @property cause Description of the error cause
 * @property action Suggested action to resolve the error
 * @property correlationId Unique identifier for support requests
 */
@Serializable
data class HereErrorResponse(
    @SerialName("title") val title: String? = null,
    @SerialName("status") val status: Int? = null,
    @SerialName("code") val code: String? = null,
    @SerialName("cause") val cause: String? = null,
    @SerialName("action") val action: String? = null,
    @SerialName("correlationId") val correlationId: String? = null,
)
