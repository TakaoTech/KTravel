package com.takaotech.ktravel.ui.plan.transport.composer

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.slack.circuit.codegen.annotations.CircuitInject
import com.takaotech.ktravel.di.AppScope
import com.takaotech.ktravel.presentation.plan.day.AddTransportScreen
import com.takaotech.ktravel.presentation.plan.transport.TransportPlanningEvent
import com.takaotech.ktravel.presentation.plan.transport.TransportPlanningNavState
import com.takaotech.ktravel.presentation.plan.transport.TransportPlanningViewModel
import dev.zacsweers.metrox.viewmodel.assistedMetroViewModel

/**
 * Circuit entry point of the leg composer, wrapping the existing [TransportPlanningPage].
 *
 * A successful calculation is reported as a one-shot in the view model's state rather than as an
 * event stream, and acknowledged here — so coming back from the preview does not open it again.
 */
@CircuitInject(AddTransportScreen::class, AppScope::class)
@Composable
fun TransportPlanningUi(screen: AddTransportScreen, state: TransportPlanningNavState, modifier: Modifier = Modifier) {
    val viewModel = assistedMetroViewModel<TransportPlanningViewModel, TransportPlanningViewModel.Factory>(
        key = "transport_${screen.dayId}_${screen.startPlaceId}_${screen.endPlaceId}",
    ) { _ -> create(screen.travelId, screen.dayId, screen.startPlaceId, screen.endPlaceId) }

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.previewRequested) {
        if (!uiState.previewRequested) return@LaunchedEffect
        state.eventSink(TransportPlanningEvent.ShowPreview)
        viewModel.onPreviewOpened()
    }

    TransportPlanningPage(
        viewModel = viewModel,
        onNavigationBackClick = { state.eventSink(TransportPlanningEvent.Back) },
        modifier = modifier,
    )
}
