package com.takaotech.ktravel.presentation.planning.transport.options

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import com.slack.circuit.codegen.annotations.CircuitInject
import com.takaotech.ktravel.di.AppScope
import com.takaotech.ktravel.di.PlanningGraphStore
import kotlinx.collections.immutable.toImmutableList
import kotlinx.collections.immutable.toPersistentSet

/**
 * The transit family: which vehicles are acceptable, and how the journey may be shaped.
 *
 * Same contract as the routing family — it owns the request in the plan's draft and nothing above it
 * knows what a transfer is — with one difference that follows from the API rather than from taste:
 * there is no chosen vehicle, so there is no per-vehicle block to nest.
 */
@CircuitInject(TransitRouteOptionsScreen::class, AppScope::class)
@Composable
fun TransitRouteOptionsPresenter(
    screen: TransitRouteOptionsScreen,
    planningGraphStore: PlanningGraphStore,
): TransitRouteOptionsUiState {
    val draft = remember(screen.travelId) {
        planningGraphStore.getOrCreate(screen.travelId).routeOptionsDraft
    }
    val stored by draft.selection.collectAsState()

    val selection = stored.asTransitFor(screen.profileId) ?: defaultTransitSelection(screen.profileId)

    LaunchedEffect(screen) {
        if (draft.selection.value.asTransitFor(screen.profileId) == null) draft.update(selection)
    }

    return TransitRouteOptionsUiState(
        modes = screen.spec.modes.toImmutableList(),
        modeFilter = selection.modeFilter.toPersistentSet(),
        alternatives = selection.alternatives,
        maxAlternatives = screen.spec.maxAlternatives.coerceAtLeast(1),
        maxChanges = selection.maxChanges,
        walkingPace = WalkingPace.of(selection.pedestrianSpeedMetersPerSecond),
        maxWalkingDistanceMeters = selection.pedestrianMaxDistanceMeters,
    ) { event ->
        draft.update(selection.reduce(event, screen.spec))
    }
}
