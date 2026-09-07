package com.takaotech.ktravel.presentation.consent

import androidx.compose.runtime.Composable
import com.slack.circuit.test.FakeNavigator
import com.slack.circuit.test.presenterTestOf
import com.takaotech.ktravel.core.telemetry.TelemetryConsent
import com.takaotech.ktravel.data.consent.ConsentFlowDataSource
import com.takaotech.ktravel.data.consent.ConsentFlowRepository
import com.takaotech.ktravel.domain.model.AppSettingsDomain
import com.takaotech.ktravel.domain.repository.AppSettingsRepository
import com.takaotech.ktravel.domain.staticflows.ConsentCard
import com.takaotech.ktravel.domain.staticflows.ConsentFlow
import com.takaotech.ktravel.presentation.travels.TravelListScreen
import io.kotest.assertions.nondeterministic.eventually
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.json.Json
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant

private const val FLOW_VERSION = 7

/**
 * The privacy notice.
 *
 * Two things are worth holding onto. The answer is stored with the version it was given to and the
 * moment it was given, because that pair is what makes it expire — without either, consent given
 * once would stand forever. And where answering leads depends on how the notice was reached: as the
 * first screen it resets the stack onto the trip list, reopened from the diagnostics screen it goes
 * back to it.
 */
class ConsentPresenterTest :
    BehaviorSpec({
        given("the notice shown as the first screen") {
            `when`("the user allows diagnostics") {
                then("the answer is stored with its version and the trip list becomes the root") {
                    val settings = FakeAppSettings()
                    val screen = PrivacyPolicyScreen(fromStart = true)
                    val navigator = FakeNavigator(screen)

                    presenterTestOf({ presenter(screen, navigator, settings) }) {
                        awaitLoaded().eventSink(ConsentEvent.Answered(TelemetryConsent.Granted))

                        eventually(2.seconds) {
                            settings.settings.value.telemetryConsent shouldBe TelemetryConsent.Granted
                            settings.settings.value.acknowledgedConsentVersion shouldBe FLOW_VERSION
                            (settings.settings.value.consentDecidedAt != null) shouldBe true
                        }

                        navigator.awaitResetRoot().newRoot shouldBe TravelListScreen
                        cancelAndIgnoreRemainingEvents()
                    }
                }
            }
        }

        given("the notice reopened from the diagnostics screen") {
            `when`("the user refuses") {
                then("the refusal is stored and the screen is popped, not the whole stack reset") {
                    val settings = FakeAppSettings()
                    val screen = PrivacyPolicyScreen(fromStart = false)
                    val navigator = FakeNavigator(screen)

                    presenterTestOf({ presenter(screen, navigator, settings) }) {
                        awaitLoaded().eventSink(ConsentEvent.Answered(TelemetryConsent.Denied))

                        eventually(2.seconds) {
                            settings.settings.value.telemetryConsent shouldBe TelemetryConsent.Denied
                        }

                        navigator.awaitPop()
                        cancelAndIgnoreRemainingEvents()
                    }
                }
            }
        }
    })

/** The first state whose cards have been read; before that the notice has nothing to draw. */
private suspend fun com.slack.circuit.test.CircuitReceiveTurbine<ConsentUiState>.awaitLoaded(): ConsentUiState {
    var state = awaitItem()
    while (state.cards.isEmpty()) {
        state = awaitItem()
    }
    return state
}

/** The presenter over a notice of our own, so the test does not depend on the shipped text. */
@Composable
private fun presenter(screen: PrivacyPolicyScreen, navigator: FakeNavigator, settings: AppSettingsRepository) =
    ConsentPresenter(
        screen = screen,
        navigator = navigator,
        consentFlowRepository = ConsentFlowRepository(
            ConsentFlowDataSource { Json.encodeToString(TEST_FLOW).encodeToByteArray() },
        ),
        appSettingsRepository = settings,
    )

private val TEST_FLOW = ConsentFlow(
    version = FLOW_VERSION,
    language = "en",
    introCards = listOf(
        ConsentCard(id = "welcome", title = "Welcome", message = "Everything **local**."),
        ConsentCard(id = "decision", title = "Send diagnostics?", message = "Your call.", isDecision = true),
    ),
)

/** The preferences, in memory, with the one write this test observes. */
private class FakeAppSettings : AppSettingsRepository {

    private val state = MutableStateFlow(AppSettingsDomain())

    override val settings: StateFlow<AppSettingsDomain> = state

    override suspend fun updateNavigatorRemote(baseUrl: String) = error("Not written here")

    override suspend fun updateTelemetryConsent(consent: TelemetryConsent, flowVersion: Int, decidedAt: Instant) {
        state.value = state.value.copy(
            telemetryConsent = consent,
            acknowledgedConsentVersion = flowVersion,
            consentDecidedAt = decidedAt,
        )
    }

    override suspend fun updateLogRetentionDays(days: Int) = error("Not written here")

    override suspend fun installationId(): String = "test-installation"
}
