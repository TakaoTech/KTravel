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
import com.takaotech.ktravel.domain.staticflows.IntroFlow
import com.takaotech.ktravel.domain.staticflows.IntroRequirement
import com.takaotech.ktravel.domain.staticflows.PrivacyPolicy
import com.takaotech.ktravel.navigation.interceptor.PlanningGraphInterceptor
import com.takaotech.ktravel.presentation.intro.IntroFlowScreen
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
 * The first screen is not always the trip list: when the introduction is due — never seen, or its
 * privacy half brought back by a new policy or an answer that has expired — the introduction is the
 * root, and answering it resets the stack onto the trip list.
 *
 * @param appGraph The application dependency graph, source of the trip scoped object graphs.
 * @param onRootPop What leaving the root screen means, which only the platform entry point knows:
 *   finishing the activity on Android, closing the window on desktop, nothing on iOS.
 * @param modifier The modifier applied to the navigable content.
 */
@Composable
internal fun KTravelNavigation(appGraph: AppGraph, onRootPop: () -> Unit, modifier: Modifier = Modifier) {
    val language = Locale.current.language

    // TODO Change this, async this and push navigation to Intro
    val content: Pair<IntroFlow, PrivacyPolicy>? by produceState(
        initialValue = null,
        language,
        appGraph,
    ) {
        value = appGraph.staticContentRepository.introFlow(language) to
            appGraph.staticContentRepository.privacyPolicy(language)
    }
    val (introFlow, privacyPolicy) = content ?: return

    val requirement = remember(introFlow, privacyPolicy, appGraph) {
        appGraph.appSettingsRepository.settings.value.introRequirement(
            introVersion = introFlow.version,
            policyVersion = privacyPolicy.version,
            now = Clock.System.now(),
        )
    }

    val navStack = rememberSaveableNavStack(
        root = if (requirement == IntroRequirement.None) {
            TravelListScreen
        } else {
            IntroFlowScreen(requirement = requirement, fromStart = true)
        },
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
