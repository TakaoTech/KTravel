package com.takaotech.ktravel.ui.plan.day.placesbacklog

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.slack.circuit.codegen.annotations.CircuitInject
import com.takaotech.ktravel.di.AppScope
import com.takaotech.ktravel.presentation.plan.day.PlacesBacklogEvent
import com.takaotech.ktravel.presentation.plan.day.PlacesBacklogScreen
import com.takaotech.ktravel.presentation.plan.day.PlacesBacklogUiState

/** Draws the places waiting for a day, through the stateless [PlacesBacklogContent]. */
@CircuitInject(PlacesBacklogScreen::class, AppScope::class)
@Composable
fun PlacesBacklogUi(state: PlacesBacklogUiState, modifier: Modifier = Modifier) {
    val sink = state.eventSink
    PlacesBacklogContent(
        places = state.places,
        pendingPermanentDelete = state.pendingPermanentDelete,
        modifier = modifier,
        onCloseClick = { sink(PlacesBacklogEvent.Close) },
        onAddPlaceClick = { sink(PlacesBacklogEvent.AddPlace) },
        onMovePlaceToStepsClick = { sink(PlacesBacklogEvent.MovePlaceToSteps(it)) },
        onMovePlaceToBacklogClick = { sink(PlacesBacklogEvent.MovePlaceToBacklog(it)) },
        onPermanentDeleteRequest = { sink(PlacesBacklogEvent.PermanentDeleteRequested(it)) },
        onPermanentDeleteConfirm = { sink(PlacesBacklogEvent.PermanentDeleteConfirmed) },
        onPermanentDeleteDismiss = { sink(PlacesBacklogEvent.PermanentDeleteDismissed) },
    )
}
