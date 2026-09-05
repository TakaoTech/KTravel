package com.takaotech.ktravel.presentation.travels

import androidx.compose.runtime.Composable
import com.slack.circuit.codegen.annotations.CircuitInject
import com.slack.circuit.runtime.Navigator
import com.takaotech.ktravel.di.AppScope
import com.takaotech.ktravel.presentation.plan.PlanOverviewScreen

/**
 * Navigation for the creation form; the form itself stays in [TravelCreationViewModel].
 *
 * The created trip replaces the form on the stack rather than stacking on it, so coming back out of
 * a trip lands on the list and never on the form the trip was made in.
 */
@CircuitInject(TravelCreationScreen::class, AppScope::class)
@Composable
fun TravelCreationPresenter(navigator: Navigator): TravelCreationNavState = TravelCreationNavState { event ->
    when (event) {
        TravelCreationEvent.Back -> navigator.pop()

        is TravelCreationEvent.Created -> {
            navigator.pop()
            navigator.goTo(PlanOverviewScreen(event.travelId))
        }
    }
}
