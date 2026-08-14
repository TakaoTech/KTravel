package com.takaotech.ktravel.domain.routing

import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime

/**
 * What the traveller asked for.
 *
 * Sealed, for the same reason the contract has one request type per provider API rather than one with
 * optional fields: the two shapes do not take the same parameters, and a single type would carry
 * "avoid tolls" into a train journey and "which vehicles are acceptable" into a car route. Which
 * variant to build is not guessed — it follows from
 * [RoutingProfileInfo.modeSelection] of the profile the traveller picked.
 *
 * The variants are named after the kind of API rather than after HERE: a second road engine produces
 * a [Road] too, and that is the point of the profile being chosen rather than the provider.
 */
sealed interface RouteSelection {

    /** The profile that will serve it, which is also what decides the path the request goes to. */
    val profileId: RoutingProfileId

    /** How many routes to ask for, bounded by [RoutingProfileInfo.maxAlternatives]. */
    val alternatives: Int

    /** The day of departure, meaningful only together with [departureTime]. */
    val departureDate: LocalDate?

    /** The time of departure, meaningful only together with [departureDate]. */
    val departureTime: LocalTime?

    /**
     * A route on roads, travelled by one vehicle.
     *
     * @property mode The single vehicle, which upstream requires.
     * @property avoidTolls Offered only where [RoutingProfileInfo.supportsTolls] says the profile
     *   understands the question.
     * @property shortestDistance Optimizes for distance instead of time. Offered only for the modes
     *   in [RoutingProfileInfo.modesSupportingShortest].
     */
    data class Road(
        override val profileId: RoutingProfileId,
        val mode: RoutingMode,
        override val alternatives: Int = 1,
        val avoidTolls: Boolean = false,
        val shortestDistance: Boolean = false,
        override val departureDate: LocalDate? = null,
        override val departureTime: LocalTime? = null,
    ) : RouteSelection

    /**
     * A journey on scheduled services, which is always several vehicles and some walking.
     *
     * @property modeFilter Which vehicles the answer may use. Empty means no restriction, which is
     *   the upstream default and not the same thing as "none allowed".
     */
    data class Transit(
        override val profileId: RoutingProfileId,
        val modeFilter: Set<RoutingMode> = emptySet(),
        override val alternatives: Int = 1,
        override val departureDate: LocalDate? = null,
        override val departureTime: LocalTime? = null,
    ) : RouteSelection
}
