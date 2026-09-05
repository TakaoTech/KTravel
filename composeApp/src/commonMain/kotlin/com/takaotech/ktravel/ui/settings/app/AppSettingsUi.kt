package com.takaotech.ktravel.ui.settings.app

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.slack.circuit.codegen.annotations.CircuitInject
import com.takaotech.ktravel.di.AppScope
import com.takaotech.ktravel.presentation.settings.AppSettingsEvent
import com.takaotech.ktravel.presentation.settings.AppSettingsNavState
import com.takaotech.ktravel.presentation.settings.AppSettingsScreen
import dev.zacsweers.metrox.viewmodel.metroViewModel

/** Circuit entry point of the app settings, wrapping the existing [AppSettingsPage]. */
@CircuitInject(AppSettingsScreen::class, AppScope::class)
@Composable
fun AppSettingsUi(state: AppSettingsNavState, modifier: Modifier = Modifier) {
    AppSettingsPage(
        viewModel = metroViewModel(),
        onNavigationBackClick = { state.eventSink(AppSettingsEvent.Back) },
        onLicensesClick = { state.eventSink(AppSettingsEvent.OpenLicenses) },
        modifier = modifier,
    )
}
