package com.takaotech.ktravel.presentation.planning.transport.options

import com.slack.circuit.runtime.CircuitUiEvent
import com.slack.circuit.runtime.CircuitUiState
import com.slack.circuit.runtime.screen.Screen
import com.takaotech.ktravel.domain.routing.RoutingMode
import com.takaotech.ktravel.domain.routing.RoutingOptionsSpec
import com.takaotech.ktravel.domain.routing.RoutingProfileId
import kotlinx.collections.immutable.ImmutableList
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

/**
 * The options of a road profile: one vehicle, and how many routes to ask for.
 *
 * Carries the profile's capabilities flattened into identifiers and numbers rather than the domain
 * types themselves. A Circuit `Screen` is `Parcelable` on Android, and the alternative — marking
 * the routing model with a platform interface — would put an Android concern in the domain to serve
 * a screen that is never actually saved. [spec] puts the domain shape back for the presenter.
 *
 * @property travelId Resolves the plan's graph, and with it the draft the presenter writes to.
 */
@Serializable
data class RoutingRouteOptionsScreen(
    val travelId: String,
    val provider: String,
    val profile: String,
    val modeIds: List<String>,
    val maxAlternatives: Int,
    val shortestModeIds: Set<String>,
    val supportsTolls: Boolean,
) : Screen {

    @Transient
    val profileId: RoutingProfileId = RoutingProfileId(provider = provider, profile = profile)

    @Transient
    val spec: RoutingOptionsSpec.RoutingSingleMode = RoutingOptionsSpec.RoutingSingleMode(
        modes = modeIds.map(::RoutingMode),
        maxAlternatives = maxAlternatives,
        modesSupportingShortest = shortestModeIds.map(::RoutingMode).toSet(),
        supportsTolls = supportsTolls,
    )

    companion object {

        /** Flattens a profile the catalog produced into the key this screen is addressed by. */
        fun of(
            travelId: String,
            profileId: RoutingProfileId,
            spec: RoutingOptionsSpec.RoutingSingleMode,
        ): RoutingRouteOptionsScreen = RoutingRouteOptionsScreen(
            travelId = travelId,
            provider = profileId.provider,
            profile = profileId.profile,
            modeIds = spec.modes.map { it.id },
            maxAlternatives = spec.maxAlternatives,
            shortestModeIds = spec.modesSupportingShortest.map { it.id }.toSet(),
            supportsTolls = spec.supportsTolls,
        )
    }
}

/**
 * @property modeExtrasScreen The controls belonging to the vehicle currently chosen, or null when
 *   that vehicle has none. Rendered by the UI as a nested `CircuitContent`, so a vehicle that grows
 *   its own options does not change this block.
 */
data class RoutingRouteOptionsUiState(
    val modes: ImmutableList<RoutingMode>,
    val selectedMode: RoutingMode,
    val alternatives: Int,
    val maxAlternatives: Int,
    val modeExtrasScreen: Screen?,
    val eventSink: (RoutingRouteOptionsEvent) -> Unit,
) : CircuitUiState

sealed interface RoutingRouteOptionsEvent : CircuitUiEvent {
    /** Picks the single vehicle the route is computed for. */
    data class SelectMode(val mode: RoutingMode) : RoutingRouteOptionsEvent

    /** Sets how many routes to ask for. */
    data class SetAlternatives(val count: Int) : RoutingRouteOptionsEvent
}
