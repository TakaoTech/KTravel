package com.takaotech.ktravel.ui.plan.overview

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.slack.circuit.codegen.annotations.CircuitInject
import com.takaotech.ktravel.di.AppScope
import com.takaotech.ktravel.presentation.plan.PlanOverviewEvent
import com.takaotech.ktravel.presentation.plan.PlanOverviewNavState
import com.takaotech.ktravel.presentation.plan.PlanOverviewScreen
import com.takaotech.ktravel.presentation.plan.PlanOverviewViewModel
import dev.zacsweers.metrox.viewmodel.assistedMetroViewModel

/** Circuit entry point of the trip overview, wrapping the existing [PlanOverviewPage]. */
@CircuitInject(PlanOverviewScreen::class, AppScope::class)
@Composable
fun PlanOverviewUi(screen: PlanOverviewScreen, state: PlanOverviewNavState, modifier: Modifier = Modifier) {
    val viewModel = assistedMetroViewModel<PlanOverviewViewModel, PlanOverviewViewModel.Factory>(
        key = screen.travelId,
    ) { _ -> create(screen.travelId) }

    PlanOverviewPage(
        viewModel = viewModel,
        onBackClick = { state.eventSink(PlanOverviewEvent.Back) },
        onAddPlaceClicked = { state.eventSink(PlanOverviewEvent.AddPlace) },
        onDateClicked = { state.eventSink(PlanOverviewEvent.OpenDay(it)) },
        onSettingClicked = { state.eventSink(PlanOverviewEvent.OpenSettings) },
        modifier = modifier,
    )
}
