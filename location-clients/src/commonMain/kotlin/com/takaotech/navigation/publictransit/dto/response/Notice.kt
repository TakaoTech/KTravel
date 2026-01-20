package com.takaotech.navigation.publictransit.dto.response

import kotlinx.serialization.Serializable

/**
 * Notice attached to a route or section.
 */
@Serializable
data class Notice(
    val title: String? = null,
    val code: String? = null,
    val severity: String? = null
)
