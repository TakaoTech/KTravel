package com.takaotech.ktravel.ui.travels.list

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.slack.circuit.codegen.annotations.CircuitInject
import com.takaotech.ktravel.di.AppScope
import com.takaotech.ktravel.presentation.travels.TravelListEvent
import com.takaotech.ktravel.presentation.travels.TravelListNavState
import com.takaotech.ktravel.presentation.travels.TravelListScreen
import dev.zacsweers.metrox.viewmodel.metroViewModel

/** Circuit entry point of the travel list, wrapping the existing [TravelListPage]. */
@CircuitInject(TravelListScreen::class, AppScope::class)
@Composable
fun TravelListUi(state: TravelListNavState, modifier: Modifier = Modifier) {
    TravelListPage(
        viewModel = metroViewModel(),
        onTravelClick = { state.eventSink(TravelListEvent.OpenTravel(it)) },
        onNewTravelClick = { state.eventSink(TravelListEvent.NewTravel) },
        onAppSettingsClick = { state.eventSink(TravelListEvent.OpenAppSettings) },
        modifier = modifier,
    )
}
