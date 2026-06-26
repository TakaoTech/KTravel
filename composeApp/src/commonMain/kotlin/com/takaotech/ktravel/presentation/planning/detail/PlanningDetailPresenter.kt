package com.takaotech.ktravel.presentation.planning.detail

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import com.slack.circuit.codegen.annotations.CircuitInject
import com.slack.circuit.runtime.Navigator
import com.takaotech.ktravel.di.AppScope
import com.takaotech.ktravel.di.PlanningGraphStore
import com.takaotech.ktravel.presentation.planning.TravelDayUi
import com.takaotech.ktravel.presentation.planning.TravelPlanUiMapper
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

/**
 * Presenter Circuit del dettaglio giorno (function-based, registrato via Metro `@CircuitInject`).
 *
 * `screen` e `navigator` sono iniettati come assisted da Circuit; [PlanningGraphStore] dal grafo
 * Metro. Risolve il repository con `getOrCreate(travelId)` (vincolo V3). Gli eventi di dominio sono
 * delegati al repository; quelli di navigazione passano per il [Navigator] (tradotti dall'host nel
 * NavController esistente).
 */
@CircuitInject(PlanningDetailScreen::class, AppScope::class)
@Composable
fun PlanningDetailPresenter(
    screen: PlanningDetailScreen,
    navigator: Navigator,
    planningGraphStore: PlanningGraphStore
): PlanningDetailUiState {
    val repository = remember(screen.travelId) {
        planningGraphStore.getOrCreate(screen.travelId).travelPlanRepository
    }
    val scope = rememberCoroutineScope()

    val dayFlow = remember(repository, screen.dayId) {
        repository.getTravelDayFlow(screen.dayId)
            .map { with(TravelPlanUiMapper) { it.toUiDay() } }
    }
    val day by dayFlow.collectAsState(initial = TravelDayUi.EMPTY)

    return PlanningDetailUiState(
        steps = day.steps,
        places = day.places
    ) { event ->
        when (event) {
            PlanningDetailEvent.NavigateBack -> navigator.pop()
            PlanningDetailEvent.AddPlace -> navigator.goTo(AddPlaceScreen(day.id))
            is PlanningDetailEvent.MovePlaceToSteps -> scope.launch {
                repository.movePlaceToStep(event.placeId, screen.dayId)
            }

            is PlanningDetailEvent.MovePlaceToGeneral -> scope.launch {
                repository.movePlaceToGeneral(event.placeId, screen.dayId)
            }

            is PlanningDetailEvent.DeletePlace -> scope.launch {
                repository.deletePlace(event.placeId, screen.dayId)
            }

            is PlanningDetailEvent.StepDelete -> scope.launch {
                repository.removeStep(event.step.id, screen.dayId)
            }

            is PlanningDetailEvent.StepMoveUp -> scope.launch {
                repository.moveTravelStepUp(event.stepId, screen.dayId)
            }

            is PlanningDetailEvent.StepMoveDown -> scope.launch {
                repository.moveTravelStepDown(event.stepId, screen.dayId)
            }

            is PlanningDetailEvent.AddTransport -> navigator.goTo(
                AddTransportScreen(day.id, event.startId, event.endId)
            )
        }
    }
}
