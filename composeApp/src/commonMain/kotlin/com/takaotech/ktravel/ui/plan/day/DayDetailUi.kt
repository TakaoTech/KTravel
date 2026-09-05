package com.takaotech.ktravel.ui.plan.day

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.slack.circuit.codegen.annotations.CircuitInject
import com.takaotech.ktravel.di.AppScope
import com.takaotech.ktravel.presentation.plan.day.DayDetailScreen
import com.takaotech.ktravel.presentation.plan.day.DayDetailUiState

/** Draws one day of a trip, through [DayDetailContent], which composes its two panes. */
@CircuitInject(DayDetailScreen::class, AppScope::class)
@Composable
fun DayDetailUi(state: DayDetailUiState, modifier: Modifier = Modifier) {
    DayDetailContent(
        state = state,
        modifier = modifier,
    )
}
