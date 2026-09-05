package com.takaotech.ktravel.ui.plan.settings

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.slack.circuit.codegen.annotations.CircuitInject
import com.takaotech.ktravel.di.AppScope
import com.takaotech.ktravel.presentation.settings.TravelSettingsEvent
import com.takaotech.ktravel.presentation.settings.TravelSettingsNavState
import com.takaotech.ktravel.presentation.settings.TravelSettingsScreen
import com.takaotech.ktravel.presentation.settings.TravelSettingsViewModel
import dev.zacsweers.metrox.viewmodel.assistedMetroViewModel

/** Circuit entry point of the trip settings, wrapping the existing [TravelSettingsPage]. */
@CircuitInject(TravelSettingsScreen::class, AppScope::class)
@Composable
fun TravelSettingsUi(screen: TravelSettingsScreen, state: TravelSettingsNavState, modifier: Modifier = Modifier) {
    val viewModel =
        assistedMetroViewModel<TravelSettingsViewModel, TravelSettingsViewModel.Factory>(
            key = "settings_${screen.travelId}",
        ) { _ -> create(screen.travelId) }

    TravelSettingsPage(
        viewModel = viewModel,
        onNavigationBackClick = { state.eventSink(TravelSettingsEvent.Back) },
        modifier = modifier,
    )
}
