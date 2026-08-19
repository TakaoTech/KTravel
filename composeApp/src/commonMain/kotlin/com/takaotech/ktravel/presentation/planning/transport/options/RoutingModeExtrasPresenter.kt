package com.takaotech.ktravel.presentation.planning.transport.options

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import com.slack.circuit.codegen.annotations.CircuitInject
import com.takaotech.ktravel.di.AppScope
import com.takaotech.ktravel.di.PlanningGraphStore
import kotlinx.collections.immutable.persistentSetOf
import kotlinx.collections.immutable.toPersistentSet

/**
 * The options of one road vehicle.
 *
 * Writes to the same draft as [RoutingRouteOptionsPresenter] and never seeds it: by the time this block
 * is composed the family above has already put a request there. Nor does it ever touch the vehicle —
 * so a click landing in the frame where the vehicle is changing still applies to the current request
 * rather than reviving the previous one.
 */
@CircuitInject(RoutingModeExtrasScreen::class, AppScope::class)
@Composable
fun RoutingModeExtrasPresenter(
    screen: RoutingModeExtrasScreen,
    planningGraphStore: PlanningGraphStore,
): RoutingModeExtrasUiState {
    val draft = remember(screen.travelId) {
        planningGraphStore.getOrCreate(screen.travelId).routeOptionsDraft
    }
    val stored by draft.selection.collectAsState()
    val selection = stored.asRoutingFor(screen.profileId)

    return RoutingModeExtrasUiState(
        avoidable = screen.avoidable,
        avoided = selection?.avoid?.toPersistentSet() ?: persistentSetOf(),
        tollsUnsupported = screen.tollsUnsupported,
        supportsShortest = screen.supportsShortest,
        shortestDistance = selection?.shortestDistance == true,
    ) { event ->
        selection?.let { draft.update(it.reduce(event)) }
    }
}
