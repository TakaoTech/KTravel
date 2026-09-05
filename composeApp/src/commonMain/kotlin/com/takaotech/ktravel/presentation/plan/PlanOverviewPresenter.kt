package com.takaotech.ktravel.presentation.plan

import androidx.compose.runtime.Composable
import com.slack.circuit.codegen.annotations.CircuitInject
import com.slack.circuit.runtime.Navigator
import com.takaotech.ktravel.di.AppScope
import com.takaotech.ktravel.presentation.plan.day.AddPlaceScreen
import com.takaotech.ktravel.presentation.plan.day.DayDetailScreen
import com.takaotech.ktravel.presentation.settings.TravelSettingsScreen

/**
 * Everything reachable from an open trip; the trip itself stays in [PlanOverviewViewModel].
 *
 * Leaving closes the trip's object graph, but not here: the navigation interceptor does it, so the
 * back arrow and the system back gesture cannot disagree about it the way three hand-written call
 * sites used to.
 *
 * @see com.takaotech.ktravel.navigation.interceptor.PlanningGraphInterceptor
 */
@CircuitInject(PlanOverviewScreen::class, AppScope::class)
@Composable
fun PlanOverviewPresenter(screen: PlanOverviewScreen, navigator: Navigator): PlanOverviewNavState =
    PlanOverviewNavState { event ->
        when (event) {
            PlanOverviewEvent.Back -> navigator.pop()

            PlanOverviewEvent.AddPlace -> navigator.goTo(AddPlaceScreen(screen.travelId))

            is PlanOverviewEvent.OpenDay -> navigator.goTo(
                DayDetailScreen(
                    screen.travelId,
                    event.dayId,
                ),
            )

            PlanOverviewEvent.OpenSettings -> navigator.goTo(TravelSettingsScreen(screen.travelId))
        }
    }
