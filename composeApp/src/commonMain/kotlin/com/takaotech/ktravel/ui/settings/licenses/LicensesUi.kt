package com.takaotech.ktravel.ui.settings.licenses

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.slack.circuit.codegen.annotations.CircuitInject
import com.takaotech.ktravel.di.AppScope
import com.takaotech.ktravel.presentation.settings.LicensesEvent
import com.takaotech.ktravel.presentation.settings.LicensesNavState
import com.takaotech.ktravel.presentation.settings.LicensesScreen

/** Circuit entry point of the licenses screen, wrapping the existing [LicensesPage]. */
@CircuitInject(LicensesScreen::class, AppScope::class)
@Composable
fun LicensesUi(state: LicensesNavState, modifier: Modifier = Modifier) {
    LicensesPage(
        onNavigationBackClick = { state.eventSink(LicensesEvent.Back) },
        modifier = modifier,
    )
}
