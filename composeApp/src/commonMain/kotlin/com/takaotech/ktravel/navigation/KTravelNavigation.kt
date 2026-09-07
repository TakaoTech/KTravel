package com.takaotech.ktravel.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.intl.Locale
import com.slack.circuit.foundation.NavigableCircuitContent
import com.slack.circuit.foundation.navstack.rememberSaveableNavStack
import com.slack.circuit.foundation.rememberCircuitNavigator
import com.slack.circuitx.navigation.intercepting.LoggingNavigationEventListener
import com.slack.circuitx.navigation.intercepting.LoggingNavigatorFailureNotifier
import com.slack.circuitx.navigation.intercepting.rememberInterceptingNavigator
import com.takaotech.ktravel.di.AppGraph
import com.takaotech.ktravel.domain.staticflows.ConsentFlow
import com.takaotech.ktravel.navigation.interceptor.PlanningGraphInterceptor
import com.takaotech.ktravel.presentation.consent.PrivacyPolicyScreen
import com.takaotech.ktravel.presentation.travels.TravelListScreen
import kotlin.time.Clock

/**
 * The back stack of the application, and the only place that knows how navigation is hosted.
 *
 * One stack, Circuit's: every destination is a `Screen` with a presenter, so there is nothing to
 * translate between models and no nested graphs to keep in step. The stack itself is saveable — it
 * is persisted through the serializing `CircuitSaver` configured in `AppGraph`, which is what lets
 * it survive process death without the screens having to be `Parcelable`.
 *
 * The first screen is not always the trip list: when the privacy notice is due — never answered,
 * answered to an older version, or answered more than a year ago — the notice is the root, and
 * answering it resets the stack onto the trip list.
 *
 * @param appGraph The application dependency graph, source of the trip scoped object graphs.
 * @param onRootPop What leaving the root screen means, which only the platform entry point knows:
 *   finishing the activity on Android, closing the window on desktop, nothing on iOS.
 * @param modifier The modifier applied to the navigable content.
 */
@Composable
internal fun KTravelNavigation(appGraph: AppGraph, onRootPop: () -> Unit, modifier: Modifier = Modifier) {
    // The notice decides the first screen, so it has to be read before there is one. Nothing is
    // drawn until it has been: showing the trip list and then replacing it with the notice would be
    // both a flash and, for a moment, an application the user has not agreed to use yet.
    val language = Locale.current.language
    val consentFlow: ConsentFlow? by produceState(initialValue = null, language, appGraph) {
        value = appGraph.consentFlowRepository.flow(language)
    }
    val flow = consentFlow ?: return

    val startsWithNotice = remember(flow, appGraph) {
        appGraph.appSettingsRepository.settings.value.needsConsentFlow(flow.version, Clock.System.now())
    }

    val navStack = rememberSaveableNavStack(
        root = if (startsWithNotice) PrivacyPolicyScreen(fromStart = true) else TravelListScreen,
    )

    val circuitNavigator = rememberCircuitNavigator(
        navStack = navStack,
        onRootPop = { onRootPop() },
        // The intercepting navigator installs its own, and two would pop twice.
        enableBackHandler = false,
    )

    val navigationLogger = remember(appGraph) { AppNavigationLogger(appGraph.appLogger) }

    val navigator = rememberInterceptingNavigator(
        navigator = circuitNavigator,
        interceptors = remember(appGraph) {
            listOf(PlanningGraphInterceptor(appGraph.planningGraphStore, appGraph.logScope))
        },
        eventListeners = remember(navigationLogger) {
            listOf(LoggingNavigationEventListener(navigationLogger))
        },
        notifier = remember(navigationLogger) { LoggingNavigatorFailureNotifier(navigationLogger) },
    )

    NavigableCircuitContent(navigator = navigator, navStack = navStack, modifier = modifier)
}
