package com.takaotech.ktravel.presentation.settings

import androidx.compose.runtime.Composable
import com.slack.circuit.codegen.annotations.CircuitInject
import com.slack.circuit.runtime.Navigator
import com.takaotech.ktravel.di.AppScope
import com.takaotech.ktravel.presentation.consent.PrivacyPolicyScreen
import com.takaotech.ktravel.presentation.diagnostics.LogsScreen

/**
 * Navigation for the app settings; the settings themselves stay in [AppSettingsViewModel].
 *
 * Reading the licenses is a screen of its own rather than a section of this one, because the list
 * is long, generated at build time, and has nothing to do with what the settings change.
 */
@CircuitInject(AppSettingsScreen::class, AppScope::class)
@Composable
fun AppSettingsPresenter(navigator: Navigator): AppSettingsNavState = AppSettingsNavState { event ->
    when (event) {
        AppSettingsEvent.Back -> navigator.pop()
        AppSettingsEvent.OpenLicenses -> navigator.goTo(LicensesScreen)
        AppSettingsEvent.OpenDiagnostics -> navigator.goTo(LogsScreen())
        AppSettingsEvent.PrivacyPolicy -> navigator.goTo(PrivacyPolicyScreen(fromStart = false))
    }
}
