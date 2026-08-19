package com.takaotech.ktravel.domain.routing

import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime

/**
 * Something a road route can be asked to keep off.
 *
 * A real enum, unlike [RoutingMode], and the difference is deliberate. Modes come from the catalog
 * and are not knowable here; which of these the app offers is the app's own decision, taken per
 * vehicle in the presentation layer, so the set is closed and the compiler can check it.
 */
enum class RouteFeature {
    /** Any road that charges a toll. */
    TOLL_ROAD,

    /** Motorways and other roads reachable only through a junction. */
    CONTROLLED_ACCESS_HIGHWAY,

    /** Crossings by boat. */
    FERRY,

    /** Tunnels, which some vehicles and cargoes may not enter. */
    TUNNEL,

    /** Unpaved roads. */
    DIRT_ROAD,

    /** Trains that carry the vehicle through a tunnel or over a pass. */
    CAR_SHUTTLE_TRAIN,
}

/**
 * What the traveller asked for.
 *
 * Sealed, for the same reason the contract has one request type per provider API rather than one with
 * optional fields: the two shapes do not take the same parameters, and a single type would carry
 * "avoid tolls" into a train journey and "which vehicles are acceptable" into a car route. Which
 * variant to build is not guessed — it follows from the
 * [RoutingOptionsSpec] of the profile the traveller picked.
 *
 * The variants are named after the kind of API rather than after HERE: a second road engine produces
 * a [Routing] too, and that is the point of the profile being chosen rather than the provider.
 */
sealed interface RouteSelection {

    /** The profile that will serve it, which is also what decides the path the request goes to. */
    val profileId: RoutingProfileId

    /** How many routes to ask for, bounded by [RoutingOptionsSpec.maxAlternatives]. */
    val alternatives: Int

    /** The day of departure, meaningful only together with [departureTime]. */
    val departureDate: LocalDate?

    /** The time of departure, meaningful only together with [departureDate]. */
    val departureTime: LocalTime?

    /**
     * A route on roads, travelled by one vehicle.
     *
     * @property mode The single vehicle, which upstream requires.
     * @property avoid What the route should stay away from. Only ever holds features that both the
     *   profile declares and the chosen [mode] can be asked about, which is why changing the vehicle
     *   narrows it rather than carrying a stale option across.
     * @property shortestDistance Optimizes for distance instead of time. Offered only for the modes
     *   in [RoutingOptionsSpec.RoutingSingleMode.modesSupportingShortest].
     */
    data class Routing(
        override val profileId: RoutingProfileId,
        val mode: RoutingMode,
        override val alternatives: Int = 1,
        val avoid: Set<RouteFeature> = emptySet(),
        val shortestDistance: Boolean = false,
        override val departureDate: LocalDate? = null,
        override val departureTime: LocalTime? = null,
    ) : RouteSelection

    /**
     * A journey on scheduled services, which is always several vehicles and some walking.
     *
     * @property modeFilter Which vehicles the answer may use. Empty means no restriction, which is
     *   the upstream default and not the same thing as "none allowed".
     * @property maxChanges Most transfers the traveller will accept. Null leaves it to the provider.
     * @property pedestrianSpeedMetersPerSecond How fast the traveller walks between stops, which
     *   shifts every connection the journey depends on. Null leaves it to the provider.
     * @property pedestrianMaxDistanceMeters How far the traveller will walk in one leg. Null leaves
     *   it to the provider.
     */
    data class Transit(
        override val profileId: RoutingProfileId,
        val modeFilter: Set<RoutingMode> = emptySet(),
        override val alternatives: Int = 1,
        val maxChanges: Int? = null,
        val pedestrianSpeedMetersPerSecond: Double? = null,
        val pedestrianMaxDistanceMeters: Int? = null,
        override val departureDate: LocalDate? = null,
        override val departureTime: LocalTime? = null,
    ) : RouteSelection
}
