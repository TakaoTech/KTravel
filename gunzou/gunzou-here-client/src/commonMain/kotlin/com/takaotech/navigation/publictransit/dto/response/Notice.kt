package com.takaotech.navigation.publictransit.dto.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Notice attached to a route or section.
 */
@Serializable
data class Notice(
    @SerialName("title") val title: String? = null,
    @SerialName("code") val code: String? = null,
    @SerialName("severity") val severity: String? = null,
)
