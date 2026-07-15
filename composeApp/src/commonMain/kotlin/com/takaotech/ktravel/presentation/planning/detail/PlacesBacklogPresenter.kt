package com.takaotech.ktravel.presentation.planning.detail

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
import com.takaotech.ktravel.presentation.planning.PlaceUi
import com.takaotech.ktravel.presentation.planning.TravelDayUi
import com.takaotech.ktravel.presentation.planning.TravelPlanUiMapper
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

/**
 * Presenter Circuit del pannello backlog posti (function-based, registrato via Metro
 * `@CircuitInject`).
 *
 * Risolve il repository con `getOrCreate(travelId)` (vincolo V3); colleziona lo stesso flow del
 * giorno del pannello itinerario — costo trascurabile, `getTravelDayFlow` è un `map` su uno
 * `StateFlow` condiviso. Il dialog di conferma eliminazione è guidato dal presenter tramite
 * [PlacesBacklogUiState.pendingPermanentDelete] (stato retained, testabile).
 */
@CircuitInject(PlacesBacklogScreen::class, AppScope::class)
@Composable
fun PlacesBacklogPresenter(
    screen: PlacesBacklogScreen,
    navigator: Navigator,
    planningGraphStore: PlanningGraphStore
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
        pendingPermanentDelete = pendingPermanentDelete
    ) { event ->
        when (event) {
            PlacesBacklogEvent.Close -> navigator.pop()

            PlacesBacklogEvent.AddPlace -> navigator.goTo(AddPlaceScreen(screen.dayId))

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
