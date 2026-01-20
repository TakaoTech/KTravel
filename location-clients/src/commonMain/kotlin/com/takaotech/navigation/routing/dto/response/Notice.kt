package com.takaotech.navigation.routing.dto.response

import kotlinx.serialization.Serializable

/**
 * Notice about issues encountered during route processing.
 *
 * @property title Human-readable title
 * @property code Machine-readable code
 * @property severity Severity level (critical, info, warning)
 */
@Serializable
data class Notice(
    val title: String,
    val code: String? = null,
    val severity: String? = null
)
