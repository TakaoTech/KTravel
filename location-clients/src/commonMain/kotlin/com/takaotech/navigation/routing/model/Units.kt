package com.takaotech.navigation.routing.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Units of measurement for distance and other values.
 */
@Serializable
enum class Units {
    @SerialName("metric")
    METRIC,

    @SerialName("imperial")
    IMPERIAL
}
