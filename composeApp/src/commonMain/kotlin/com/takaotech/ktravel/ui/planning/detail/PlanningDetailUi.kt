package com.takaotech.ktravel.ui.planning.detail

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.slack.circuit.codegen.annotations.CircuitInject
import com.takaotech.ktravel.di.AppScope
import com.takaotech.ktravel.presentation.planning.detail.PlanningDetailScreen
import com.takaotech.ktravel.presentation.planning.detail.PlanningDetailUiState

/**
 * `Ui` Circuit del dettaglio giorno (registrata via Metro `@CircuitInject`): delega alla pagina
 * [PlanningDetailPage], che compone i pannelli figli come `CircuitContent` annidati.
 */
@CircuitInject(PlanningDetailScreen::class, AppScope::class)
@Composable
fun PlanningDetailUi(state: PlanningDetailUiState, modifier: Modifier = Modifier) {
    PlanningDetailPage(
        state = state,
        modifier = modifier,
    )
}
