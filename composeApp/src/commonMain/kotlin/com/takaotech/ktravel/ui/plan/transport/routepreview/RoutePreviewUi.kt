package com.takaotech.ktravel.ui.plan.transport.routepreview

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.slack.circuit.codegen.annotations.CircuitInject
import com.takaotech.ktravel.di.AppScope
import com.takaotech.ktravel.presentation.plan.transport.RoutePreviewEvent
import com.takaotech.ktravel.presentation.plan.transport.RoutePreviewNavState
import com.takaotech.ktravel.presentation.plan.transport.RoutePreviewScreen
import com.takaotech.ktravel.presentation.plan.transport.RoutePreviewViewModel
import dev.zacsweers.metrox.viewmodel.assistedMetroViewModel

/**
 * Circuit entry point of the route preview, wrapping the existing [RoutePreviewPage].
 *
 * Filing an alternative is asynchronous and answers with the id of the leg it produced, so the
 * navigation waits for that id rather than guessing it.
 */
@CircuitInject(RoutePreviewScreen::class, AppScope::class)
@Composable
fun RoutePreviewUi(screen: RoutePreviewScreen, state: RoutePreviewNavState, modifier: Modifier = Modifier) {
    val viewModel = assistedMetroViewModel<RoutePreviewViewModel, RoutePreviewViewModel.Factory>(
        key = "preview_${screen.dayId}_${screen.startPlaceId}_${screen.endPlaceId}",
    ) { _ -> create(screen.travelId, screen.dayId, screen.startPlaceId) }

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.savedStepId) {
        val stepId = uiState.savedStepId ?: return@LaunchedEffect
        state.eventSink(RoutePreviewEvent.Saved(stepId))
        viewModel.onTransportStepOpened()
    }

    RoutePreviewPage(
        result = uiState.result,
        selectedIndex = uiState.selectedIndex,
        onSelectionChange = { viewModel.selectRoute(it) },
        onConfirm = { viewModel.saveSelectedRoute() },
        onNavigationBackClick = { state.eventSink(RoutePreviewEvent.Back) },
        modifier = modifier,
    )
}
