package com.takaotech.navigation.routing.dto.response.notice

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class VehicleNotice(
    @SerialName("title") val title: String,
    @SerialName("code") val code: String? = null,
    @SerialName("severity") val severity: String? = null,
    @SerialName("details") val details: List<VehicleNoticeDetail>? = null
)
