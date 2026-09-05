package com.takaotech.ktravel.ui.travels.creation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.slack.circuit.codegen.annotations.CircuitInject
import com.takaotech.ktravel.di.AppScope
import com.takaotech.ktravel.presentation.travels.TravelCreationEvent
import com.takaotech.ktravel.presentation.travels.TravelCreationNavState
import com.takaotech.ktravel.presentation.travels.TravelCreationScreen

/** Circuit entry point of the creation form, wrapping the existing [TravelCreationPage]. */
@CircuitInject(TravelCreationScreen::class, AppScope::class)
@Composable
fun TravelCreationUi(state: TravelCreationNavState, modifier: Modifier = Modifier) {
    TravelCreationPage(
        onBackClick = { state.eventSink(TravelCreationEvent.Back) },
        onNavigateToPlanning = { state.eventSink(TravelCreationEvent.Created(it)) },
        modifier = modifier,
    )
}
