package com.takaotech.ktravel.presentation.plan.day

import androidx.compose.runtime.Composable
import com.slack.circuit.codegen.annotations.CircuitInject
import com.slack.circuit.foundation.onNavEvent
import com.slack.circuit.runtime.Navigator
import com.takaotech.ktravel.di.AppScope

/**
 * Coordination only: the day screen builds its two panes and forwards what they cannot answer.
 *
 * The panes resolve their own dependencies from the trip they carry, so nothing is threaded through
 * here — which is why this presenter holds no state of its own.
 */
@CircuitInject(DayDetailScreen::class, AppScope::class)
@Composable
fun DayDetailPresenter(screen: DayDetailScreen, navigator: Navigator): DayDetailUiState = DayDetailUiState(
    stepsPaneScreen = StepsPaneScreen(screen.travelId, screen.dayId),
    placesBacklogScreen = PlacesBacklogScreen(screen.travelId, screen.dayId),
) { event ->
    when (event) {
        is DayDetailEvent.ChildNav -> navigator.onNavEvent(event.navEvent)
    }
}
