package com.takaotech.ktravel.presentation.intro

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.text.intl.Locale
import com.slack.circuit.codegen.annotations.CircuitInject
import com.slack.circuit.runtime.Navigator
import com.takaotech.ktravel.core.logging.AppLogger
import com.takaotech.ktravel.data.staticflows.StaticContentRepository
import com.takaotech.ktravel.di.AppScope
import com.takaotech.ktravel.domain.repository.AppSettingsRepository
import com.takaotech.ktravel.domain.staticflows.IntroFlow
import com.takaotech.ktravel.domain.staticflows.IntroRequirement
import com.takaotech.ktravel.domain.staticflows.IntroStep
import com.takaotech.ktravel.domain.staticflows.PrivacyPolicy
import com.takaotech.ktravel.domain.staticflows.section
import com.takaotech.ktravel.presentation.privacy.PrivacyPolicyScreen
import com.takaotech.ktravel.presentation.travels.TravelListScreen
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.launch
import kotlin.time.Clock

/**
 * Reads the introduction and the policy in the device's language, and records the answer.
 *
 * The answer is written with both versions it was given to and the moment it was given, because all
 * three are what make it expire: a newer policy, or a year gone by, and the question comes back.
 *
 * Where answering leads depends on how the introduction was reached. As the first screen of the
 * application it has nothing behind it, so the back stack is reset onto the trip list; reopened from
 * somewhere else, it is popped like any other screen and the user is back where they were.
 *
 * @param screen Says how much is due, and which of the two cases it is.
 * @param navigator Where the application goes once the question is answered.
 * @param staticContentRepository Reads the packaged introduction and policy.
 * @param appSettingsRepository Where the answer is kept.
 * @param appLogger Where a point that names a section nobody wrote is reported.
 */
@CircuitInject(IntroFlowScreen::class, AppScope::class)
@Composable
fun IntroPresenter(
    screen: IntroFlowScreen,
    navigator: Navigator,
    staticContentRepository: StaticContentRepository,
    appSettingsRepository: AppSettingsRepository,
    appLogger: AppLogger,
): IntroUiState {
    val language = Locale.current.language
    val logger = remember(appLogger) { appLogger.withTag("IntroPresenter") }
    val scope = rememberCoroutineScope()

    val content: IntroContent? by produceState(initialValue = null, language, staticContentRepository) {
        value = IntroContent(
            flow = staticContentRepository.introFlow(language),
            policy = staticContentRepository.privacyPolicy(language),
        )
    }

    val steps = remember(content, screen.requirement) {
        content?.flow?.steps.orEmpty().filter { it.isDueUnder(screen.requirement) }.toImmutableList()
    }

    return IntroUiState(steps = steps) { event ->
        when (event) {
            is IntroEvent.Answered -> {
                val loaded = content ?: return@IntroUiState

                scope.launch {
                    appSettingsRepository.updateTelemetryConsent(
                        consent = event.consent,
                        introVersion = loaded.flow.version,
                        privacyVersion = loaded.policy.version,
                        decidedAt = Clock.System.now(),
                    )

                    if (screen.fromStart) {
                        navigator.resetRoot(TravelListScreen)
                    } else {
                        navigator.pop()
                    }
                }
            }

            // A point that names a section nobody wrote goes nowhere rather than opening a document
            // that would scroll to nothing: the introduction is content, and content must not be
            // able to take the first screen of the application down. The document asked for in
            // full names no section, so there is nothing to check: it opens at the top.
            is IntroEvent.PolicyOpened -> {
                val anchor = event.policyRef

                if (anchor == null || content?.policy?.section(anchor) != null) {
                    navigator.goTo(PrivacyPolicyScreen(anchor = anchor))
                } else {
                    logger.w { "The privacy policy has no section at $anchor" }
                }
            }
        }
    }
}

/**
 * The two packaged files, read together.
 *
 * Held as one value so the screen has either both or neither: a privacy page whose points cannot be
 * opened yet would be a page that fails when it is touched.
 *
 * @property flow The introduction.
 * @property policy The policy its points point at.
 */
private data class IntroContent(val flow: IntroFlow, val policy: PrivacyPolicy)

/** Whether this step is shown under [requirement]; only the reading cards are ever left out. */
private fun IntroStep.isDueUnder(requirement: IntroRequirement): Boolean =
    requirement != IntroRequirement.PrivacyOnly || this !is IntroStep.Card
