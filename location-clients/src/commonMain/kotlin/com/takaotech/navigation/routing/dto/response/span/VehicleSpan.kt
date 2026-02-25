package com.takaotech.navigation.routing.dto.response.span

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class VehicleSpan(
    @SerialName("offset") val offset: Int,
    @SerialName("length") val length: Int,
    @SerialName("speedLimit") val speedLimit: Double? = null,
    @SerialName("maxSpeed") val maxSpeed: Double? = null,
    @SerialName("dynamicSpeedInfo") val dynamicSpeedInfo: DynamicSpeedInfo? = null,
    @SerialName("functionalClass") val functionalClass: Int? = null,
    @SerialName("routingZones") val routingZones: List<Int>? = null,
    @SerialName("truckRoadTypes") val truckRoadTypes: List<Int>? = null,
    @SerialName("incidents") val incidents: List<Int>? = null,
    @SerialName("tollSystems") val tollSystems: List<Int>? = null,
    @SerialName("tolls") val tolls: List<Int>? = null,
    @SerialName("countryCode") val countryCode: String? = null,
    @SerialName("stateCode") val stateCode: String? = null
)
