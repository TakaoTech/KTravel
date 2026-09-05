package com.takaotech.ktravel.ui.plan.place

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.slack.circuit.codegen.annotations.CircuitInject
import com.takaotech.ktravel.di.AppScope
import com.takaotech.ktravel.presentation.place.PlaceInsertEvent
import com.takaotech.ktravel.presentation.place.PlaceInsertNavState
import com.takaotech.ktravel.presentation.place.PlaceInsertViewModel
import com.takaotech.ktravel.presentation.plan.day.AddPlaceScreen
import dev.zacsweers.metrox.viewmodel.assistedMetroViewModel

/** Circuit entry point of the place form, wrapping the existing [PlaceInsertPage]. */
@CircuitInject(AddPlaceScreen::class, AppScope::class)
@Composable
fun PlaceInsertUi(screen: AddPlaceScreen, state: PlaceInsertNavState, modifier: Modifier = Modifier) {
    val viewModel = assistedMetroViewModel<PlaceInsertViewModel, PlaceInsertViewModel.Factory>(
        key = "place_${screen.travelId}_${screen.dayId}",
    ) { _ -> create(screen.travelId, screen.dayId) }

    PlaceInsertPage(
        viewModel = viewModel,
        onExit = { state.eventSink(PlaceInsertEvent.Exit) },
        onSaveClicked = { state.eventSink(PlaceInsertEvent.Saved) },
        modifier = modifier,
    )
}
