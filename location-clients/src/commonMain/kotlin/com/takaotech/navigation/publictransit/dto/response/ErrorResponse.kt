package com.takaotech.navigation.publictransit.dto.response

import kotlinx.serialization.Serializable

/**
 * Error response from the Public Transit API.
 */
@Serializable
data class ErrorResponse(
    val title: String? = null,
    val status: Int? = null,
    val code: String? = null,
    val cause: String? = null,
    val action: String? = null,
    val correlationId: String? = null
)
