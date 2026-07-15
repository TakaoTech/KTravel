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
import com.takaotech.ktravel.presentation.planning.TravelPlanUiMapper
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

/**
 * Presenter Circuit del pannello itinerario (function-based, registrato via Metro `@CircuitInject`).
 *
 * Risolve il repository con `getOrCreate(travelId)` (vincolo V3) e colleziona il flow del giorno
 * mappandolo in righe pronte per la UI ([buildStepRows]); il pannello backlog colleziona lo stesso
 * flow in parallelo — costo trascurabile, `getTravelDayFlow` è un `map` su uno `StateFlow`
 * condiviso (vedi `TravelPlanRepositoryImpl.planningState`).
 *
 * Gli eventi di dominio sono delegati al repository; quelli di navigazione passano per il
 * [Navigator] sintetico del `CircuitContent` annidato: la pagina padre intercetta [PlacesBacklogScreen]
 * (pane switching) e inoltra il resto all'host.
 */
@CircuitInject(StepsPaneScreen::class, AppScope::class)
@Composable
fun StepsPanePresenter(
    screen: StepsPaneScreen,
    navigator: Navigator,
    planningGraphStore: PlanningGraphStore
): StepsPaneUiState {
    val repository = remember(screen.travelId) {
        planningGraphStore.getOrCreate(screen.travelId).travelPlanRepository
    }
    val scope = rememberCoroutineScope()

    val rowsFlow = remember(repository, screen.dayId) {
        repository.getTravelDayFlow(screen.dayId)
            .map { day -> buildStepRows(with(TravelPlanUiMapper) { day.toUiDay() }.steps) }
    }
    val rows: ImmutableList<StepRow> by rowsFlow.collectAsState(initial = persistentListOf())

    return StepsPaneUiState(rows = rows) { event ->
        when (event) {
            StepsPaneEvent.NavigateBack -> navigator.pop()

            StepsPaneEvent.OpenBacklog -> navigator.goTo(
                PlacesBacklogScreen(screen.travelId, screen.dayId)
            )

            is StepsPaneEvent.DeleteStep -> scope.launch {
                repository.removeStep(event.step.id, screen.dayId)
            }

            is StepsPaneEvent.MoveStepUp -> scope.launch {
                repository.moveTravelStepUp(event.stepId, screen.dayId)
            }

            is StepsPaneEvent.MoveStepDown -> scope.launch {
                repository.moveTravelStepDown(event.stepId, screen.dayId)
            }

            is StepsPaneEvent.AddTransport -> navigator.goTo(
                AddTransportScreen(screen.dayId, event.startPlaceId, event.endPlaceId)
            )
        }
    }
}
