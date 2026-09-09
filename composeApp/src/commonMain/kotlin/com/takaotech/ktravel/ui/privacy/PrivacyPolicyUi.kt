package com.takaotech.ktravel.ui.privacy

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.slack.circuit.codegen.annotations.CircuitInject
import com.takaotech.ktravel.di.AppScope
import com.takaotech.ktravel.presentation.privacy.PrivacyPolicyScreen
import com.takaotech.ktravel.presentation.privacy.PrivacyPolicyUiState

/** Circuit entry point of the privacy policy document. */
@CircuitInject(PrivacyPolicyScreen::class, AppScope::class)
@Composable
fun PrivacyPolicyUi(state: PrivacyPolicyUiState, modifier: Modifier = Modifier) {
    PrivacyPolicyContent(state = state, modifier = modifier)
}
