package com.takaotech.ktravel.ui.plan.place

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.slack.circuit.codegen.annotations.CircuitInject
import com.takaotech.ktravel.di.AppScope
import com.takaotech.ktravel.presentation.place.PlaceInsertUiState
import com.takaotech.ktravel.presentation.plan.day.AddPlaceScreen

/** Circuit entry point of the «add places» screen. */
@CircuitInject(AddPlaceScreen::class, AppScope::class)
@Composable
fun PlaceInsertUi(state: PlaceInsertUiState, modifier: Modifier = Modifier) {
    PlaceInsertContent(state = state, modifier = modifier)
}
