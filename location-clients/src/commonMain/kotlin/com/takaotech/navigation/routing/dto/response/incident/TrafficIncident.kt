package com.takaotech.navigation.routing.dto.response.incident

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TrafficIncident(
    @SerialName("id") val id: String,
    @SerialName("type") val type: String? = null,
    @SerialName("severity") val severity: String? = null,
    @SerialName("summary") val summary: String? = null,
    @SerialName("description") val description: String? = null
)
