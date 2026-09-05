package com.takaotech.ktravel.presentation.plan.transport

import androidx.compose.runtime.Composable
import com.slack.circuit.codegen.annotations.CircuitInject
import com.slack.circuit.runtime.Navigator
import com.takaotech.ktravel.di.AppScope
import com.takaotech.ktravel.presentation.plan.day.TransportStepScreen

/**
 * Navigation for the route preview; the alternatives it shows stay in [RoutePreviewViewModel].
 *
 * Filing an alternative takes the traveller to the leg it produced rather than back to the day:
 * that is the moment there is most to say about the leg, and its detail is the only screen it can
 * be written down on.
 */
@CircuitInject(RoutePreviewScreen::class, AppScope::class)
@Composable
fun RoutePreviewPresenter(screen: RoutePreviewScreen, navigator: Navigator): RoutePreviewNavState =
    RoutePreviewNavState { event ->
        when (event) {
            RoutePreviewEvent.Back -> navigator.pop()

            is RoutePreviewEvent.Saved -> navigator.goTo(
                TransportStepScreen(
                    travelId = screen.travelId,
                    dayId = screen.dayId,
                    stepId = event.stepId,
                ),
            )
        }
    }
