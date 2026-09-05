package com.takaotech.ktravel.presentation.plan.day

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import com.slack.circuit.codegen.annotations.CircuitInject
import com.slack.circuit.retained.rememberRetained
import com.slack.circuit.runtime.Navigator
import com.takaotech.ktravel.di.AppScope
import com.takaotech.ktravel.di.PlanningGraphStore
import com.takaotech.ktravel.presentation.plan.PlaceUi
import com.takaotech.ktravel.presentation.plan.TravelDayUi
import com.takaotech.ktravel.presentation.plan.TravelPlanUiMapper
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

/**
 * The places waiting to be given a day, and what the user does with them.
 *
 * Deleting one for good is confirmed first, and the pending place is held in
 * [PlacesBacklogUiState.pendingPermanentDelete] rather than in the UI: a dialog driven from the
 * state is one the presenter's own tests can open and answer.
 */
@CircuitInject(PlacesBacklogScreen::class, AppScope::class)
@Composable
fun PlacesBacklogPresenter(
    screen: PlacesBacklogScreen,
    navigator: Navigator,
    planningGraphStore: PlanningGraphStore,
): PlacesBacklogUiState {
    val repository = remember(screen.travelId) {
        planningGraphStore.getOrCreate(screen.travelId).travelPlanRepository
    }
    val scope = rememberCoroutineScope()

    val dayFlow = remember(repository, screen.dayId) {
        repository.getTravelDayFlow(screen.dayId)
            .map { with(TravelPlanUiMapper) { it.toUiDay() } }
    }
    val day by dayFlow.collectAsState(initial = TravelDayUi.EMPTY)

    var pendingPermanentDelete by rememberRetained { mutableStateOf<PlaceUi?>(null) }

    return PlacesBacklogUiState(
        places = day.places,
        pendingPermanentDelete = pendingPermanentDelete,
    ) { event ->
        when (event) {
            PlacesBacklogEvent.Close -> navigator.pop()

            PlacesBacklogEvent.AddPlace -> navigator.goTo(AddPlaceScreen(screen.travelId, screen.dayId))

            is PlacesBacklogEvent.MovePlaceToSteps -> scope.launch {
                repository.movePlaceToStep(event.placeId, screen.dayId)
            }

            is PlacesBacklogEvent.MovePlaceToBacklog -> scope.launch {
                repository.movePlaceToGeneral(event.placeId, screen.dayId)
            }

            is PlacesBacklogEvent.PermanentDeleteRequested -> {
                pendingPermanentDelete = day.places.firstOrNull { it.id == event.placeId }
            }

            PlacesBacklogEvent.PermanentDeleteConfirmed -> {
                pendingPermanentDelete?.let { place ->
                    scope.launch {
                        repository.deletePlace(place.id, screen.dayId)
                    }
                }
                pendingPermanentDelete = null
            }

            PlacesBacklogEvent.PermanentDeleteDismissed -> {
                pendingPermanentDelete = null
            }
        }
    }
}
