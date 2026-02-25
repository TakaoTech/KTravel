package com.takaotech.navigation.routing.dto.response.toll

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TollCost(
    @SerialName("id") val id: String,
    @SerialName("systemId") val systemId: String? = null,
    @SerialName("amount") val amount: Double? = null,
    @SerialName("currency") val currency: String? = null
)
