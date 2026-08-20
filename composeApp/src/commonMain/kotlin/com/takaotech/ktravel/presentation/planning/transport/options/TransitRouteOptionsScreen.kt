package com.takaotech.ktravel.presentation.planning.transport.options

import com.slack.circuit.runtime.CircuitUiEvent
import com.slack.circuit.runtime.CircuitUiState
import com.slack.circuit.runtime.screen.Screen
import com.takaotech.ktravel.core.annotation.IgnoreOnParcel
import com.takaotech.ktravel.core.annotation.Parcelize
import com.takaotech.ktravel.domain.routing.RoutingMode
import com.takaotech.ktravel.domain.routing.RoutingOptionsSpec
import com.takaotech.ktravel.domain.routing.RoutingProfileId
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableSet

/**
 * The options of a transit profile: which vehicles are acceptable, and how the journey may be shaped.
 *
 * No per-vehicle extras screen, unlike the routing family, and the asymmetry is the point: there is no
 * chosen vehicle here to hang a rule on. The modes restrict what the answer may use, and the rest of
 * the options — transfers, walking — apply to the journey as a whole.
 *
 * Flattened for the same reason as [RoutingRouteOptionsScreen].
 */
@Parcelize
data class TransitRouteOptionsScreen(
    val travelId: String,
    val provider: String,
    val profile: String,
    val modeIds: List<String>,
    val maxAlternatives: Int,
) : Screen {

    @IgnoreOnParcel
    val profileId: RoutingProfileId = RoutingProfileId(provider = provider, profile = profile)

    @IgnoreOnParcel
    val spec: RoutingOptionsSpec.TransitFilter = RoutingOptionsSpec.TransitFilter(
        modes = modeIds.map(::RoutingMode),
        maxAlternatives = maxAlternatives,
    )

    companion object {

        fun of(
            travelId: String,
            profileId: RoutingProfileId,
            spec: RoutingOptionsSpec.TransitFilter,
        ): TransitRouteOptionsScreen = TransitRouteOptionsScreen(
            travelId = travelId,
            provider = profileId.provider,
            profile = profileId.profile,
            modeIds = spec.modes.map { it.id },
            maxAlternatives = spec.maxAlternatives,
        )
    }
}

/**
 * @property modeFilter The vehicles ticked. Empty admits all of them.
 * @property maxChanges Null when the traveller has not limited transfers, which leaves it to the
 *   provider.
 * @property maxWalkingDistanceMeters Null under the same rule.
 */
data class TransitRouteOptionsUiState(
    val modes: ImmutableList<RoutingMode>,
    val modeFilter: ImmutableSet<RoutingMode>,
    val alternatives: Int,
    val maxAlternatives: Int,
    val maxChanges: Int?,
    val walkingPace: WalkingPace,
    val maxWalkingDistanceMeters: Int?,
    val eventSink: (TransitRouteOptionsEvent) -> Unit,
) : CircuitUiState

sealed interface TransitRouteOptionsEvent : CircuitUiEvent {
    /** Adds or removes one kind of vehicle from the filter. */
    data class ToggleMode(val mode: RoutingMode) : TransitRouteOptionsEvent

    /** Sets how many journeys to ask for. */
    data class SetAlternatives(val count: Int) : TransitRouteOptionsEvent

    /** Limits the transfers, or lifts the limit with null. */
    data class SetMaxChanges(val changes: Int?) : TransitRouteOptionsEvent

    /** Sets how fast the traveller walks between stops. */
    data class SetWalkingPace(val pace: WalkingPace) : TransitRouteOptionsEvent

    /** Limits how far the traveller will walk in one step, or lifts the limit with null. */
    data class SetMaxWalkingDistance(val meters: Int?) : TransitRouteOptionsEvent
}
