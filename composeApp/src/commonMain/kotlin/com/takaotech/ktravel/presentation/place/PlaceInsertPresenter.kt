package com.takaotech.ktravel.presentation.place

import androidx.compose.runtime.Composable
import com.slack.circuit.codegen.annotations.CircuitInject
import com.slack.circuit.runtime.Navigator
import com.takaotech.ktravel.di.AppScope
import com.takaotech.ktravel.presentation.plan.day.AddPlaceScreen

/**
 * Navigation for the place form; the search and the form stay in [PlaceInsertViewModel].
 *
 * Both ways out are the same movement — the form is done either way — which is why saving does not
 * navigate anywhere new.
 */
@CircuitInject(AddPlaceScreen::class, AppScope::class)
@Composable
fun PlaceInsertPresenter(navigator: Navigator): PlaceInsertNavState = PlaceInsertNavState { event ->
    when (event) {
        PlaceInsertEvent.Exit, PlaceInsertEvent.Saved -> navigator.pop()
    }
}
