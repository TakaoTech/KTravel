package com.takaotech.ktravel.ui.planning.detail

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.slack.circuit.codegen.annotations.CircuitInject
import com.takaotech.ktravel.di.AppScope
import com.takaotech.ktravel.presentation.planning.detail.PlanningDetailEvent
import com.takaotech.ktravel.presentation.planning.detail.PlanningDetailScreen
import com.takaotech.ktravel.presentation.planning.detail.PlanningDetailUiState

/**
 * `Ui` Circuit del dettaglio giorno (registrata via Metro `@CircuitInject`): renderizza lo stato e
 * inoltra gli eventi via `eventSink`, riusando la pagina stateless [PlanningDetailPage].
 */
@CircuitInject(PlanningDetailScreen::class, AppScope::class)
@Composable
fun PlanningDetailUi(state: PlanningDetailUiState, modifier: Modifier = Modifier) {
    val sink = state.eventSink
    PlanningDetailPage(
        steps = state.steps,
        places = state.places,
        modifier = modifier,
        onNavigationBackClick = { sink(PlanningDetailEvent.NavigateBack) },
        onAddPlaceClick = { sink(PlanningDetailEvent.AddPlace) },
        onMovePlaceToList = { sink(PlanningDetailEvent.MovePlaceToSteps(it)) },
        onDeletePlaceClick = { sink(PlanningDetailEvent.MovePlaceToGeneral(it)) },
        onDeletePermanentPlaceClick = { sink(PlanningDetailEvent.DeletePlace(it)) },
        onStepDeleteClicked = { sink(PlanningDetailEvent.StepDelete(it)) },
        onStepMoveUp = { sink(PlanningDetailEvent.StepMoveUp(it)) },
        onStepMoveDown = { sink(PlanningDetailEvent.StepMoveDown(it)) },
        onTransportAddClick = { startId, endId ->
            sink(PlanningDetailEvent.AddTransport(startId, endId))
        }
    )
}
