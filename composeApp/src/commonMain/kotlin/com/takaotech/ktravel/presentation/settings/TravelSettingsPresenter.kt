package com.takaotech.ktravel.presentation.settings

import androidx.compose.runtime.Composable
import com.slack.circuit.codegen.annotations.CircuitInject
import com.slack.circuit.runtime.Navigator
import com.takaotech.ktravel.di.AppScope

/** Navigation for the trip settings; the settings themselves stay in [TravelSettingsViewModel]. */
@CircuitInject(TravelSettingsScreen::class, AppScope::class)
@Composable
fun TravelSettingsPresenter(navigator: Navigator): TravelSettingsNavState = TravelSettingsNavState { event ->
    when (event) {
        TravelSettingsEvent.Back -> navigator.pop()
    }
}
