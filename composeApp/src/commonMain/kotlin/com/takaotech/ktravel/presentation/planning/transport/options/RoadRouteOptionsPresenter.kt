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

/**
 * The road family: which vehicle, and how many routes.
 *
 * Owns what does not depend on the vehicle. Everything that does — what to keep off the route, and
 * whether to optimize for distance — belongs to [RoadModeExtrasScreen], which this presenter picks
 * out of the rules and hands to the UI to render inside itself.
 *
 * The request lives in the plan's [com.takaotech.ktravel.presentation.planning.transport.RouteOptionsDraft]
 * rather than in this composition, because the screen leaves the composition when a route is
 * computed and the traveller expects to find their choices on the way back.
 */
@CircuitInject(RoadRouteOptionsScreen::class, AppScope::class)
@Composable
fun RoadRouteOptionsPresenter(
    screen: RoadRouteOptionsScreen,
    planningGraphStore: PlanningGraphStore,
): RoadRouteOptionsUiState {
    val draft = remember(screen.travelId) {
        planningGraphStore.getOrCreate(screen.travelId).routeOptionsDraft
    }
    val stored by draft.selection.collectAsState()

    // Rendered from the default immediately and published a frame later, so the first frame shows a
    // configured screen instead of an empty one and Calculate lights up on its own.
    val selection = stored.asRoadFor(screen.profileId) ?: defaultRoadSelection(screen.profileId, screen.spec)

    LaunchedEffect(screen) {
        if (draft.selection.value.asRoadFor(screen.profileId) == null) draft.update(selection)
    }

    return RoadRouteOptionsUiState(
        modes = screen.spec.modes.toImmutableList(),
        selectedMode = selection.mode,
        alternatives = selection.alternatives,
        maxAlternatives = screen.spec.maxAlternatives.coerceAtLeast(1),
        modeExtrasScreen = roadModeExtrasScreen(
            travelId = screen.travelId,
            profileId = screen.profileId,
            mode = selection.mode,
            spec = screen.spec,
        ),
    ) { event ->
        draft.update(selection.reduce(event, screen.spec))
    }
}
