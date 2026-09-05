package com.takaotech.ktravel.presentation.plan.transport.options

import com.takaotech.ktravel.domain.routing.RouteSelection
import com.takaotech.ktravel.domain.routing.RoutingOptionsSpec
import com.takaotech.ktravel.domain.routing.RoutingProfileId

// The rules of the transit family. Same division as the road one: the catalog says what the profile
// serves, these tables say what the app offers, and reduce() turns a choice into a request.
//
// Unlike the routing family nothing here depends on a chosen vehicle, because there is no chosen
// vehicle: the modes are a filter over what the answer may use. That is why this family has no
// per-mode extras screen.

/** The transfer counts the API accepts, from the HERE Public Transit v8 `changes` parameter. */
internal const val MAX_CHANGES_LIMIT: Int = 6

/** How far the traveller will walk in one step, in metres, from the same source. */
internal const val WALK_DISTANCE_MIN: Int = 500
internal const val WALK_DISTANCE_MAX: Int = 6000
internal const val WALK_DISTANCE_STEP: Int = 500
internal const val WALK_DISTANCE_DEFAULT: Int = 2000

/**
 * How fast the traveller walks, as something a person can answer.
 *
 * The API takes metres per second, which nobody thinks in. Three named paces cover the useful range
 * of a parameter whose only job is to shift which connections are catchable, and [NORMAL] sends
 * nothing at all so the provider's own default applies.
 */
enum class WalkingPace(val metersPerSecond: Double?) {
    SLOW(0.8),
    NORMAL(null),
    FAST(1.5),
    ;

    companion object {
        /** The pace a stored speed came from, defaulting to [NORMAL] for anything unrecognized. */
        fun of(metersPerSecond: Double?): WalkingPace =
            entries.firstOrNull { it.metersPerSecond == metersPerSecond } ?: NORMAL
    }
}

/** The request a transit profile starts on: no restriction of any kind. */
internal fun defaultTransitSelection(profileId: RoutingProfileId): RouteSelection.Transit =
    RouteSelection.Transit(profileId = profileId, alternatives = 1)

/** The stored request, when it is a transit one for this profile. */
internal fun RouteSelection?.asTransitFor(profileId: RoutingProfileId): RouteSelection.Transit? =
    (this as? RouteSelection.Transit)?.takeIf { it.profileId == profileId }

/** Applies a choice, keeping the request within what the profile accepts. */
internal fun RouteSelection.Transit.reduce(
    event: TransitRouteOptionsEvent,
    spec: RoutingOptionsSpec.TransitFilter,
): RouteSelection.Transit = when (event) {
    is TransitRouteOptionsEvent.ToggleMode -> copy(
        // An empty filter is no restriction, which is not the same as nothing being allowed, so
        // unticking the last vehicle is a valid state and not one to guard against.
        modeFilter = if (event.mode in modeFilter) modeFilter - event.mode else modeFilter + event.mode,
    )

    is TransitRouteOptionsEvent.SetAlternatives ->
        copy(alternatives = event.count.coerceIn(1, spec.maxAlternatives.coerceAtLeast(1)))

    is TransitRouteOptionsEvent.SetMaxChanges ->
        copy(maxChanges = event.changes?.coerceIn(0, MAX_CHANGES_LIMIT))

    is TransitRouteOptionsEvent.SetWalkingPace ->
        copy(pedestrianSpeedMetersPerSecond = event.pace.metersPerSecond)

    is TransitRouteOptionsEvent.SetMaxWalkingDistance ->
        copy(pedestrianMaxDistanceMeters = event.meters?.coerceIn(WALK_DISTANCE_MIN, WALK_DISTANCE_MAX))
}
