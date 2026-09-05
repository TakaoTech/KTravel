package com.takaotech.ktravel.presentation.travels

import androidx.compose.runtime.Composable
import com.slack.circuit.codegen.annotations.CircuitInject
import com.slack.circuit.runtime.Navigator
import com.takaotech.ktravel.di.AppScope
import com.takaotech.ktravel.presentation.plan.PlanOverviewScreen
import com.takaotech.ktravel.presentation.settings.AppSettingsScreen

/**
 * Where the trips are chosen from; the list itself stays in [TravelListViewModel].
 *
 * Opening a trip does not open its object graph here: that is the navigation interceptor's job, so
 * that every way into and out of a trip agrees about it without three call sites having to.
 *
 * @see com.takaotech.ktravel.navigation.interceptor.PlanningGraphInterceptor
 */
@CircuitInject(TravelListScreen::class, AppScope::class)
@Composable
fun TravelListPresenter(navigator: Navigator): TravelListNavState = TravelListNavState { event ->
    when (event) {
        is TravelListEvent.OpenTravel -> navigator.goTo(PlanOverviewScreen(event.travelId))
        TravelListEvent.NewTravel -> navigator.goTo(TravelCreationScreen)
        TravelListEvent.OpenAppSettings -> navigator.goTo(AppSettingsScreen)
    }
}
