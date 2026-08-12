package com.takaotech.navigation.common.dto

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
    val title: String? = null,
    val status: Int? = null,
    val code: String? = null,
    val cause: String? = null,
    val action: String? = null,
    val correlationId: String? = null,
)
