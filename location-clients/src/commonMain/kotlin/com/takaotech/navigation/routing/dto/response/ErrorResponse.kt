package com.takaotech.navigation.routing.dto.response

import kotlinx.serialization.Serializable

/**
 * Error response from HERE API.
 *
 * @property title Human-readable error title
 * @property status HTTP status code
 * @property code Machine-readable error code
 * @property cause Description of the error cause
 * @property action Suggested action to resolve the error
 * @property correlationId Unique identifier for support requests
 */
@Serializable
data class ErrorResponse(
    val title: String,
    val status: Int,
    val code: String? = null,
    val cause: String? = null,
    val action: String? = null,
    val correlationId: String? = null
)
