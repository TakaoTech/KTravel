package com.takaotech.ktravel.presentation.plan.transport.options

import com.slack.circuit.runtime.screen.Screen
import com.takaotech.ktravel.domain.routing.RouteFeature
import com.takaotech.ktravel.domain.routing.RouteSelection
import com.takaotech.ktravel.domain.routing.RoutingMode
import com.takaotech.ktravel.domain.routing.RoutingOptionsSpec
import com.takaotech.ktravel.domain.routing.RoutingProfileId

// The rules of the routing family, and the only place a new one goes.
//
// Three kinds of rule meet on this screen and they do not live together:
//
//  - what the upstream API refuses      -> the catalog, declared by the server
//    (modesSupportingShortest, maxAlternatives, supportsTolls)
//  - which control to offer, to which vehicle -> the tables below
//  - how a choice becomes a request     -> reduce(), further down
//
// Keeping them apart is what lets a second road engine reuse this whole family: it arrives with its
// own catalog entry and inherits these tables, instead of needing a screen of its own.

/** Everything a motorised road vehicle can be asked to keep off. */
private val MOTORISED_AVOIDABLE: Set<RouteFeature> = setOf(
    RouteFeature.TOLL_ROAD,
    RouteFeature.CONTROLLED_ACCESS_HIGHWAY,
    RouteFeature.FERRY,
    RouteFeature.TUNNEL,
    RouteFeature.DIRT_ROAD,
    RouteFeature.CAR_SHUTTLE_TRAIN,
)

/**
 * What each vehicle can be asked to avoid.
 *
 * A vehicle missing from the table has nothing to avoid, which is not the same as having the
 * options switched off: a pedestrian is not asked about tolls because the question does not arise,
 * so the control is absent rather than disabled. The two toggles that *are* disabled rather than
 * hidden — tolls and shortest distance on a profile that refuses them — are refusals from upstream,
 * and those the traveller is told about.
 */
private val AVOIDABLE_BY_MODE: Map<String, Set<RouteFeature>> = mapOf(
    "CAR" to MOTORISED_AVOIDABLE,
    "TRUCK" to MOTORISED_AVOIDABLE,
    "TAXI" to MOTORISED_AVOIDABLE,
    "BUS" to MOTORISED_AVOIDABLE,
    "PRIVATE_BUS" to MOTORISED_AVOIDABLE,
    // A scooter pays no toll and is not allowed on a motorway, so neither question arises.
    "SCOOTER" to setOf(RouteFeature.FERRY, RouteFeature.TUNNEL, RouteFeature.DIRT_ROAD),
    // Pedestrian and bicycle are deliberately absent: nothing to avoid and nothing to optimize, so
    // they get no extras block at all.
)

/** What this vehicle can be asked to avoid. */
internal fun avoidableFor(mode: RoutingMode): Set<RouteFeature> = AVOIDABLE_BY_MODE[mode.id].orEmpty()

/**
 * The extras of the vehicle currently chosen, or null when that vehicle has none.
 *
 * This is the extension point for a rule that belongs to a mode rather than to a family: a vehicle
 * that grows its own controls gets its own screen here, and the family's UI does not change.
 */
internal fun routingModeExtrasScreen(
    travelId: String,
    profileId: RoutingProfileId,
    mode: RoutingMode,
    spec: RoutingOptionsSpec.RoutingSingleMode,
): Screen? {
    val avoidable = avoidableFor(mode)
    val supportsShortest = mode in spec.modesSupportingShortest

    if (avoidable.isEmpty() && !supportsShortest) return null

    return RoutingModeExtrasScreen.of(
        travelId = travelId,
        profileId = profileId,
        mode = mode,
        // Tolls stay catalog driven: the descriptor genuinely declares whether the profile
        // understands the question, and a profile that does not is told so rather than left to
        // wonder why the option is missing.
        avoidable = avoidable.filterNot { it == RouteFeature.TOLL_ROAD && !spec.supportsTolls }
            .toSet(),
        tollsUnsupported = RouteFeature.TOLL_ROAD in avoidable && !spec.supportsTolls,
        supportsShortest = supportsShortest,
    )
}

/** The request a profile starts on, before the traveller has touched anything. */
internal fun defaultRoutingSelection(
    profileId: RoutingProfileId,
    spec: RoutingOptionsSpec.RoutingSingleMode,
): RouteSelection.Routing = RouteSelection.Routing(
    profileId = profileId,
    mode = spec.modes.first(),
    alternatives = 1,
)

/** The stored request, when it is a road one for this profile. */
internal fun RouteSelection?.asRoutingFor(profileId: RoutingProfileId): RouteSelection.Routing? =
    (this as? RouteSelection.Routing)?.takeIf { it.profileId == profileId }

/**
 * Applies a choice, keeping the request coherent with what the profile accepts.
 *
 * The invariants used to be spread between the view model and the UI state, which is why changing
 * vehicle needed one place to remember to switch the distance option off. Here they are one
 * function, and one test.
 */
internal fun RouteSelection.Routing.reduce(
    event: RoutingRouteOptionsEvent,
    spec: RoutingOptionsSpec.RoutingSingleMode,
): RouteSelection.Routing = when (event) {
    is RoutingRouteOptionsEvent.SelectMode -> copy(
        mode = event.mode,
        // Neither option survives a vehicle it does not apply to: upstream refuses the first, and
        // the second would keep avoiding something this vehicle is never asked about.
        shortestDistance = shortestDistance && event.mode in spec.modesSupportingShortest,
        avoid = avoid intersect avoidableFor(event.mode),
    )

    is RoutingRouteOptionsEvent.SetAlternatives ->
        copy(alternatives = event.count.coerceIn(1, spec.maxAlternatives.coerceAtLeast(1)))
}

/** Applies a choice made in the extras block of one vehicle. */
internal fun RouteSelection.Routing.reduce(event: RoutingModeExtrasEvent): RouteSelection.Routing = when (event) {
    is RoutingModeExtrasEvent.ToggleAvoid ->
        copy(avoid = if (event.feature in avoid) avoid - event.feature else avoid + event.feature)

    is RoutingModeExtrasEvent.SetShortestDistance -> copy(shortestDistance = event.shortest)
}
