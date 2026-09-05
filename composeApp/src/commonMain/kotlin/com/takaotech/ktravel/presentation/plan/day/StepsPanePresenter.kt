package com.takaotech.ktravel.presentation.plan.day

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import com.slack.circuit.codegen.annotations.CircuitInject
import com.slack.circuit.runtime.Navigator
import com.takaotech.ktravel.di.AppScope
import com.takaotech.ktravel.di.PlanningGraphStore
import com.takaotech.ktravel.presentation.plan.TravelDayUi
import com.takaotech.ktravel.presentation.plan.TravelPlanUiMapper
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

/**
 * Turns a day into the rows the itinerary draws, and answers what the user does to them.
 *
 * The backlog pane collects the same day flow in parallel, which costs almost nothing:
 * `getTravelDayFlow` is a `map` over a shared `StateFlow`, so the two panes read one source rather
 * than querying twice.
 *
 * What changes the plan goes to the repository; what moves elsewhere goes to the navigator, where
 * the day's layout takes the events that only switch pane and lets the rest travel on.
 */
@CircuitInject(StepsPaneScreen::class, AppScope::class)
@Composable
fun StepsPanePresenter(
    screen: StepsPaneScreen,
    navigator: Navigator,
    planningGraphStore: PlanningGraphStore,
): StepsPaneUiState {
    val repository = remember(screen.travelId) {
        planningGraphStore.getOrCreate(screen.travelId).travelPlanRepository
    }
    val scope = rememberCoroutineScope()

    val dayFlow = remember(repository, screen.dayId) {
        repository.getTravelDayFlow(screen.dayId)
            .map { day -> with(TravelPlanUiMapper) { day.toUiDay() } }
    }
    val day: TravelDayUi? by dayFlow.collectAsState(initial = null)
    val rows: ImmutableList<StepRow> = remember(day) {
        day?.let { buildStepRows(it.steps) } ?: persistentListOf()
    }
    // `getTravelDayFlow` emits `TravelDayDomain.EMPTY` — id blank, epoch date — while the day is
    // missing from the plan, a placeholder date that must not reach the toolbar.
    val date = day?.takeIf { it.id.isNotEmpty() }?.date

    return StepsPaneUiState(rows = rows, date = date) { event ->
        when (event) {
            StepsPaneEvent.NavigateBack -> navigator.pop()

            StepsPaneEvent.OpenBacklog -> navigator.goTo(
                PlacesBacklogScreen(screen.travelId, screen.dayId),
            )

            is StepsPaneEvent.OpenStepDetail -> navigator.goTo(
                PlaceStepScreen(screen.travelId, screen.dayId, event.stepId),
            )

            is StepsPaneEvent.OpenTransportDetail -> navigator.goTo(
                TransportStepScreen(screen.travelId, screen.dayId, event.stepId),
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
                AddTransportScreen(
                    screen.travelId,
                    screen.dayId,
                    event.startPlaceId,
                    event.endPlaceId,
                ),
            )

            is StepsPaneEvent.SetStartTime -> scope.launch {
                repository.updatePlaceStartTime(screen.dayId, event.stepId, event.time)
            }

            is StepsPaneEvent.SetEndTime -> scope.launch {
                repository.updatePlaceEndTime(screen.dayId, event.stepId, event.time)
            }
        }
    }
}
