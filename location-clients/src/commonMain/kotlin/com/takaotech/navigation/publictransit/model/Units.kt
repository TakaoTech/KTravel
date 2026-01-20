package com.takaotech.navigation.publictransit.model

/**
 * Units of measurement used in the Public Transit API.
 */
enum class Units(val value: String) {
    METRIC("metric"),

    IMPERIAL("imperial");

    fun toQueryString(): String = value
}
