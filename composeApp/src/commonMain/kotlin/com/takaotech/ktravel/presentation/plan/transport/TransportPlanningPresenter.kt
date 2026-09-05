package com.takaotech.ktravel.presentation.plan.transport

import androidx.compose.runtime.Composable
import com.slack.circuit.codegen.annotations.CircuitInject
import com.slack.circuit.runtime.Navigator
import com.takaotech.ktravel.di.AppScope
import com.takaotech.ktravel.presentation.plan.day.AddTransportScreen

/**
 * Navigation for the leg composer; the request it assembles stays in [TransportPlanningViewModel].
 */
@CircuitInject(AddTransportScreen::class, AppScope::class)
@Composable
fun TransportPlanningPresenter(screen: AddTransportScreen, navigator: Navigator): TransportPlanningNavState =
    TransportPlanningNavState { event ->
        when (event) {
            TransportPlanningEvent.Back -> navigator.pop()

            TransportPlanningEvent.ShowPreview -> navigator.goTo(
                RoutePreviewScreen(
                    travelId = screen.travelId,
                    dayId = screen.dayId,
                    startPlaceId = screen.startPlaceId,
                    endPlaceId = screen.endPlaceId,
                ),
            )
        }
    }
