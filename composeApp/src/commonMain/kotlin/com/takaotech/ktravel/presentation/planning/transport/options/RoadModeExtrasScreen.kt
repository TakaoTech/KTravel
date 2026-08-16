package com.takaotech.ktravel.presentation.planning.transport.options

import com.slack.circuit.runtime.CircuitUiEvent
import com.slack.circuit.runtime.CircuitUiState
import com.slack.circuit.runtime.screen.Screen
import com.takaotech.ktravel.core.annotation.IgnoreOnParcel
import com.takaotech.ktravel.core.annotation.Parcelize
import com.takaotech.ktravel.domain.routing.RouteFeature
import com.takaotech.ktravel.domain.routing.RoutingMode
import com.takaotech.ktravel.domain.routing.RoutingProfileId
import kotlinx.collections.immutable.ImmutableSet
import kotlinx.collections.immutable.toPersistentSet

/**
 * The options that belong to one vehicle rather than to the road family as a whole.
 *
 * Separate from [RoadRouteOptionsScreen] because the answer to "which controls apply" changes with
 * the vehicle and not with the profile: a car is asked about tolls and motorways, a scooter about
 * neither, a pedestrian about nothing at all. Keying a screen on the vehicle is what lets a new one
 * arrive with its own controls without the family's block knowing.
 *
 * @property avoidableNames What this vehicle can be asked to keep off, as [RouteFeature] names.
 *   Never empty together with [supportsShortest] false — that combination produces no screen at all.
 * @property tollsUnsupported Whether tolls are missing from the list because the profile declares it
 *   cannot handle them, rather than because this vehicle is never asked. The two look the same on
 *   screen unless the first one is said out loud.
 * @property supportsShortest Whether upstream will optimize this vehicle for distance.
 */
@Parcelize
data class RoadModeExtrasScreen(
    val travelId: String,
    val provider: String,
    val profile: String,
    val modeId: String,
    val avoidableNames: Set<String>,
    val tollsUnsupported: Boolean,
    val supportsShortest: Boolean,
) : Screen {

    @IgnoreOnParcel
    val profileId: RoutingProfileId = RoutingProfileId(provider = provider, profile = profile)

    @IgnoreOnParcel
    val mode: RoutingMode = RoutingMode(modeId)

    @IgnoreOnParcel
    val avoidable: ImmutableSet<RouteFeature> =
        avoidableNames.mapNotNull { name -> RouteFeature.entries.firstOrNull { it.name == name } }.toPersistentSet()

    companion object {

        fun of(
            travelId: String,
            profileId: RoutingProfileId,
            mode: RoutingMode,
            avoidable: Set<RouteFeature>,
            tollsUnsupported: Boolean,
            supportsShortest: Boolean,
        ): RoadModeExtrasScreen = RoadModeExtrasScreen(
            travelId = travelId,
            provider = profileId.provider,
            profile = profileId.profile,
            modeId = mode.id,
            avoidableNames = avoidable.map { it.name }.toSet(),
            tollsUnsupported = tollsUnsupported,
            supportsShortest = supportsShortest,
        )
    }
}

data class RoadModeExtrasUiState(
    val avoidable: ImmutableSet<RouteFeature>,
    val avoided: ImmutableSet<RouteFeature>,
    val tollsUnsupported: Boolean,
    val supportsShortest: Boolean,
    val shortestDistance: Boolean,
    val eventSink: (RoadModeExtrasEvent) -> Unit,
) : CircuitUiState

sealed interface RoadModeExtrasEvent : CircuitUiEvent {
    /** Adds or removes one thing the route should stay away from. */
    data class ToggleAvoid(val feature: RouteFeature) : RoadModeExtrasEvent

    /** Optimizes for distance instead of time. */
    data class SetShortestDistance(val shortest: Boolean) : RoadModeExtrasEvent
}
