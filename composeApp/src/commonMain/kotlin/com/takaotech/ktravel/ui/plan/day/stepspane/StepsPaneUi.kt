package com.takaotech.ktravel.ui.plan.day.stepspane

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.slack.circuit.codegen.annotations.CircuitInject
import com.takaotech.ktravel.di.AppScope
import com.takaotech.ktravel.presentation.plan.day.StepsPaneEvent
import com.takaotech.ktravel.presentation.plan.day.StepsPaneScreen
import com.takaotech.ktravel.presentation.plan.day.StepsPaneUiState
import com.takaotech.ktravel.ui.plan.day.LocalBacklogPaneOpen

/**
 * Circuit `Ui` of the itinerary pane (registered through Metro `@CircuitInject`): renders the state
 * and forwards events via `eventSink`, reusing the stateless [StepsPaneContent].
 */
@CircuitInject(StepsPaneScreen::class, AppScope::class)
@Composable
fun StepsPaneUi(state: StepsPaneUiState, modifier: Modifier = Modifier) {
    val sink = state.eventSink
    StepsPaneContent(
        rows = state.rows,
        dayDate = state.date,
        backlogOpen = LocalBacklogPaneOpen.current,
        modifier = modifier,
        onNavigationBackClick = { sink(StepsPaneEvent.NavigateBack) },
        onOpenBacklogClick = { sink(StepsPaneEvent.OpenBacklog) },
        onStepClick = { sink(StepsPaneEvent.OpenStepDetail(it)) },
        onTransportClick = { sink(StepsPaneEvent.OpenTransportDetail(it)) },
        onDeleteStepClick = { sink(StepsPaneEvent.DeleteStep(it)) },
        onMoveStepUpClick = { sink(StepsPaneEvent.MoveStepUp(it)) },
        onMoveStepDownClick = { sink(StepsPaneEvent.MoveStepDown(it)) },
        onAddTransportClick = { startPlaceId, endPlaceId ->
            sink(StepsPaneEvent.AddTransport(startPlaceId, endPlaceId))
        },
        onSetArrivalTime = { stepId, time -> sink(StepsPaneEvent.SetStartTime(stepId, time)) },
        onSetDepartureTime = { stepId, time -> sink(StepsPaneEvent.SetEndTime(stepId, time)) },
    )
}
