package com.takaotech.navigation.routing.model

/**
 * A kind of road or crossing the route should stay away from, as the `avoid[features]` parameter of
 * HERE Routing v8 spells it.
 *
 * HERE treats these as a preference and not a guarantee: where there is no alternative it routes
 * through one anyway and says so with a notice in the response.
 *
 * @property type The name the query parameter takes.
 */
enum class AvoidFeature(val type: String) {
    /** Roads that close for part of the year, such as an Alpine pass in winter. */
    SEASONAL_CLOSURE("seasonalClosure"),

    /** Any road that charges a toll. */
    TOLL_ROAD("tollRoad"),

    /** Motorways and other roads reachable only through a junction. */
    CONTROLLED_ACCESS_HIGHWAY("controlledAccessHighway"),

    /** Crossings by boat. */
    FERRY("ferry"),

    /** Trains that carry the vehicle through a tunnel or over a pass. */
    CAR_SHUTTLE_TRAIN("carShuttleTrain"),

    /** Turns that are hard to take, such as an unprotected left across traffic. */
    DIFFICULT_TURNS("difficultTurns"),

    /** Unpaved roads. */
    DIRT_ROAD("dirtRoad"),

    /** Tunnels, which some vehicles and cargoes may not enter. */
    TUNNEL("tunnel"),

    /** Manoeuvres that reverse the direction of travel. */
    U_TURNS("uTurns"),
    ;

    override fun toString(): String = type
}

/**
 * The `avoid` parameter of the /routes endpoint.
 *
 * A container with one field today because the parameter is a family: HERE also takes
 * `avoid[areas]`, `avoid[segments]` and `avoid[zoneCategories]` under the same prefix, and adding
 * one of those should not change the shape of the request again.
 *
 * @property features Road and crossing types to keep off the route where possible.
 */
data class AvoidOptions(val features: List<AvoidFeature> = emptyList()) {

    /** The value of `avoid[features]`, or null when there is nothing to send. */
    fun featuresQueryString(): String? = features
        .takeIf { it.isNotEmpty() }
        ?.joinToString(",") { it.type }
}
