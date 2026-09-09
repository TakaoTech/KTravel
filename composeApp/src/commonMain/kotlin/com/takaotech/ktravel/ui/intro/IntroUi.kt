package com.takaotech.ktravel.ui.intro

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.slack.circuit.codegen.annotations.CircuitInject
import com.takaotech.ktravel.di.AppScope
import com.takaotech.ktravel.presentation.intro.IntroFlowScreen
import com.takaotech.ktravel.presentation.intro.IntroUiState

/** Circuit entry point of the introduction. */
@CircuitInject(IntroFlowScreen::class, AppScope::class)
@Composable
fun IntroUi(state: IntroUiState, modifier: Modifier = Modifier) {
    IntroContent(state = state, modifier = modifier)
}
