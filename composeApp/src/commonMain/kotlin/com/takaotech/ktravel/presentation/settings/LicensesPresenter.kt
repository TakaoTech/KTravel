package com.takaotech.ktravel.presentation.settings

import androidx.compose.runtime.Composable
import com.slack.circuit.codegen.annotations.CircuitInject
import com.slack.circuit.runtime.Navigator
import com.takaotech.ktravel.di.AppScope

/**
 * The licenses screen has no state of its own: the list is read from a packaged resource by the UI.
 *
 * The presenter exists to own the one thing the UI cannot, which is leaving.
 */
@CircuitInject(LicensesScreen::class, AppScope::class)
@Composable
fun LicensesPresenter(navigator: Navigator): LicensesNavState = LicensesNavState { event ->
    when (event) {
        LicensesEvent.Back -> navigator.pop()
    }
}
