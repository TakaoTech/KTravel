package com.takaotech.navigation.routing.model

/**
 * Routing optimization mode.
 * - FAST: Optimize for shortest travel time
 * - SHORT: Optimize for shortest distance
 */
enum class RoutingMode(val type: String) {
    FAST("fast"),

    SHORT("short");

    override fun toString(): String {
        return type
    }
}
