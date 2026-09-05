package com.takaotech.ktravel.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.slack.circuit.foundation.NavigableCircuitContent
import com.slack.circuit.foundation.navstack.rememberSaveableNavStack
import com.slack.circuit.foundation.rememberCircuitNavigator
import com.slack.circuitx.navigation.intercepting.LoggingNavigationEventListener
import com.slack.circuitx.navigation.intercepting.LoggingNavigatorFailureNotifier
import com.slack.circuitx.navigation.intercepting.rememberInterceptingNavigator
import com.takaotech.ktravel.di.AppGraph
import com.takaotech.ktravel.navigation.interceptor.PlanningGraphInterceptor
import com.takaotech.ktravel.presentation.travels.TravelListScreen

/**
 * The back stack of the application, and the only place that knows how navigation is hosted.
 *
 * One stack, Circuit's: every destination is a `Screen` with a presenter, so there is nothing to
 * translate between models and no nested graphs to keep in step. The stack itself is saveable — it
 * is persisted through the serializing `CircuitSaver` configured in `AppGraph`, which is what lets
 * it survive process death without the screens having to be `Parcelable`.
 *
 * @param appGraph The application dependency graph, source of the trip scoped object graphs.
 * @param onRootPop What leaving the root screen means, which only the platform entry point knows:
 *   finishing the activity on Android, closing the window on desktop, nothing on iOS.
 * @param modifier The modifier applied to the navigable content.
 */
@Composable
internal fun KTravelNavigation(appGraph: AppGraph, onRootPop: () -> Unit, modifier: Modifier = Modifier) {
    val navStack = rememberSaveableNavStack(root = TravelListScreen)

    val circuitNavigator = rememberCircuitNavigator(
        navStack = navStack,
        onRootPop = { onRootPop() },
        // The intercepting navigator installs its own, and two would pop twice.
        enableBackHandler = false,
    )

    val navigationLogger = remember(appGraph) { KermitNavigationLogger(appGraph.logger) }

    val navigator = rememberInterceptingNavigator(
        navigator = circuitNavigator,
        interceptors = remember(appGraph) {
            listOf(PlanningGraphInterceptor(appGraph.planningGraphStore))
        },
        eventListeners = remember(navigationLogger) {
            listOf(LoggingNavigationEventListener(navigationLogger))
        },
        notifier = remember(navigationLogger) { LoggingNavigatorFailureNotifier(navigationLogger) },
    )

    NavigableCircuitContent(navigator = navigator, navStack = navStack, modifier = modifier)
}
