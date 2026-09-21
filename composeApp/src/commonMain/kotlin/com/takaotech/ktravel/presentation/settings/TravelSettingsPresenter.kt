package com.takaotech.ktravel.presentation.settings

import androidx.compose.runtime.Composable
import com.slack.circuit.codegen.annotations.CircuitInject
import com.slack.circuit.runtime.Navigator
import com.takaotech.ktravel.di.AppScope
import com.takaotech.ktravel.presentation.diagnostics.LogsScreen

/**
 * Navigation for the trip settings; the settings themselves stay in [TravelSettingsViewModel].
 *
 * @param screen The trip these settings belong to, which is also the trip its log is narrowed to.
 * @param navigator Where the screen can go.
 */
@CircuitInject(TravelSettingsScreen::class, AppScope::class)
@Composable
fun TravelSettingsPresenter(screen: TravelSettingsScreen, navigator: Navigator): TravelSettingsNavState =
    TravelSettingsNavState { event ->
        when (event) {
            TravelSettingsEvent.Back -> navigator.pop()
            TravelSettingsEvent.OpenDiagnostics -> navigator.goTo(LogsScreen(screen.travelId))
        }
    }
