package com.takaotech.navigation.common.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Units of measurement for distance and other values.
 *
 * @property value Representation used by the HERE APIs, both in JSON and in query strings
 */
@Serializable
enum class Units(val value: String) {
    /** Meters and kilometers. */
    @SerialName("metric")
    METRIC("metric"),

    /** Feet and miles. */
    @SerialName("imperial")
    IMPERIAL("imperial"),
    ;

    /** Converts the unit to its query string representation. */
    fun toQueryString(): String = value
}
