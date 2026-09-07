package com.takaotech.ktravel.ui.consent

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.slack.circuit.codegen.annotations.CircuitInject
import com.takaotech.ktravel.di.AppScope
import com.takaotech.ktravel.presentation.consent.ConsentEvent
import com.takaotech.ktravel.presentation.consent.ConsentUiState
import com.takaotech.ktravel.presentation.consent.PrivacyPolicyScreen

/** Circuit entry point of the privacy notice. */
@CircuitInject(PrivacyPolicyScreen::class, AppScope::class)
@Composable
fun ConsentUi(state: ConsentUiState, modifier: Modifier = Modifier) {
    ConsentContent(
        cards = state.cards,
        onAnswer = { state.eventSink(ConsentEvent.Answered(it)) },
        modifier = modifier,
    )
}
