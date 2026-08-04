package com.takaotech.ktravel.presentation.planning.detail

import androidx.compose.runtime.Composable
import com.slack.circuit.codegen.annotations.CircuitInject
import com.slack.circuit.foundation.onNavEvent
import com.slack.circuit.runtime.Navigator
import com.takaotech.ktravel.di.AppScope

/**
 * Presenter Circuit del dettaglio giorno (function-based, registrato via Metro `@CircuitInject`).
 *
 * Solo coordinamento: costruisce gli screen dei pannelli figli (che risolvono da sé le proprie
 * dipendenze, vincolo V3) e inoltra al [Navigator] i [NavEvent][com.slack.circuit.foundation.NavEvent]
 * dei figli non gestiti dal layout della pagina.
 */
@CircuitInject(PlanningDetailScreen::class, AppScope::class)
@Composable
fun PlanningDetailPresenter(
    screen: PlanningDetailScreen,
    navigator: Navigator
): PlanningDetailUiState =
    PlanningDetailUiState(
        stepsPaneScreen = StepsPaneScreen(screen.travelId, screen.dayId),
        placesBacklogScreen = PlacesBacklogScreen(screen.travelId, screen.dayId),
    ) { event ->
        when (event) {
            is PlanningDetailEvent.ChildNav -> navigator.onNavEvent(event.navEvent)
        }
    }
