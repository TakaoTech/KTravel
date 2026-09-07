package com.takaotech.ktravel.presentation.consent

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.text.intl.Locale
import com.slack.circuit.codegen.annotations.CircuitInject
import com.slack.circuit.runtime.Navigator
import com.takaotech.ktravel.data.consent.ConsentFlowRepository
import com.takaotech.ktravel.di.AppScope
import com.takaotech.ktravel.domain.repository.AppSettingsRepository
import com.takaotech.ktravel.domain.staticflows.ConsentFlow
import com.takaotech.ktravel.presentation.travels.TravelListScreen
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.launch
import kotlin.time.Clock

/**
 * Reads the notice in the device's language and records the answer.
 *
 * The answer is written with the version it was given to and the moment it was given, because both
 * are what make it expire: a newer notice, or a year gone by, and it is asked again.
 *
 * Where answering leads depends on how the notice was reached. As the first screen of the
 * application it has nothing behind it, so the back stack is reset onto the trip list; reopened from
 * the diagnostics screen, it is popped like any other screen and the user is back where they were.
 *
 * @param screen Says which of the two it is.
 * @param navigator Where the application goes once the question is answered.
 * @param consentFlowRepository Reads the packaged notice.
 * @param appSettingsRepository Where the answer is kept.
 */
@CircuitInject(PrivacyPolicyScreen::class, AppScope::class)
@Composable
fun ConsentPresenter(
    screen: PrivacyPolicyScreen,
    navigator: Navigator,
    consentFlowRepository: ConsentFlowRepository,
    appSettingsRepository: AppSettingsRepository,
): ConsentUiState {
    val language = Locale.current.language
    val scope = rememberCoroutineScope()
    val flow: ConsentFlow? by produceState(initialValue = null, language) {
        value = consentFlowRepository.flow(language)
    }

    return ConsentUiState(
        cards = flow?.introCards?.toImmutableList() ?: persistentListOf(),
    ) { event ->
        when (event) {
            is ConsentEvent.Answered -> {
                val version = flow?.version ?: return@ConsentUiState

                scope.launch {
                    appSettingsRepository.updateTelemetryConsent(
                        consent = event.consent,
                        flowVersion = version,
                        decidedAt = Clock.System.now(),
                    )

                    if (screen.fromStart) {
                        navigator.resetRoot(TravelListScreen)
                    } else {
                        navigator.pop()
                    }
                }
            }
        }
    }
}
