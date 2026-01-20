package com.takaotech.navigation.publictransit.model

data class TransitMode(
    val include: List<TransitModeType>,
    val exclude: List<TransitModeType> = listOf()
) {
    /**
     * Converts a list of transit modes to a comma-separated query string.
     * Supports exclusion by prefixing with '-'.
     */
    fun toQueryString(): String {
        return buildList {
            include.forEach { add(it.toQueryString()) }
            exclude.forEach { add("-${it.toQueryString()}") }
        }.joinToString(",") { it }
    }
}

/**
 * Transit modes supported by the Public Transit API.
 */
enum class TransitModeType(val value: String) {
    HIGH_SPEED_TRAIN("highSpeedTrain"),
    INTERCITY_TRAIN("intercityTrain"),
    INTER_REGIONAL_TRAIN("interRegionalTrain"),
    REGIONAL_TRAIN("regionalTrain"),
    CITY_TRAIN("cityTrain"),
    BUS("bus"),
    FERRY("ferry"),
    SUBWAY("subway"),
    LIGHT_RAIL("lightRail"),
    PRIVATE_BUS("privateBus"),
    INCLINED("inclined"),
    AERIAL("aerial"),
    BUS_RAPID("busRapid"),
    MONORAIL("monorail"),
    FLIGHT("flight");

    /**
     * Converts the enum to the query string format expected by the API.
     */
    fun toQueryString(): String = value
}
