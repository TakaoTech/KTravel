package com.takaotech.ktravel.presentation.intro

import androidx.compose.runtime.Composable
import com.slack.circuit.test.CircuitReceiveTurbine
import com.slack.circuit.test.FakeNavigator
import com.slack.circuit.test.presenterTestOf
import com.takaotech.ktravel.core.telemetry.TelemetryConsent
import com.takaotech.ktravel.data.staticflows.IntroFlowDataSource
import com.takaotech.ktravel.data.staticflows.PrivacyPolicyDataSource
import com.takaotech.ktravel.data.staticflows.StaticContentRepository
import com.takaotech.ktravel.domain.model.AppSettingsDomain
import com.takaotech.ktravel.domain.repository.AppSettingsRepository
import com.takaotech.ktravel.domain.staticflows.IntroFlow
import com.takaotech.ktravel.domain.staticflows.IntroRequirement
import com.takaotech.ktravel.domain.staticflows.IntroStep
import com.takaotech.ktravel.domain.staticflows.PrivacyDetail
import com.takaotech.ktravel.domain.staticflows.PrivacyPolicy
import com.takaotech.ktravel.domain.staticflows.PrivacyPolicySection
import com.takaotech.ktravel.presentation.privacy.PrivacyPolicyScreen
import com.takaotech.ktravel.presentation.travels.TravelListScreen
import com.takaotech.ktravel.testutil.testAppLogger
import io.kotest.assertions.nondeterministic.eventually
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.json.Json
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant

private const val INTRO_VERSION = 7

private const val POLICY_VERSION = 3

/**
 * The introduction.
 *
 * Three things are worth holding onto. The answer is stored with both versions it was given to and
 * the moment it was given, because that is what makes it expire — without them, consent given once
 * would stand forever. A privacy point opens the document itself on its own section, and a point
 * that names a section nobody wrote goes nowhere at all. And where answering leads depends on how
 * the introduction was reached: as the first screen it resets the stack onto the trip list,
 * reopened from elsewhere it goes back.
 */
class IntroPresenterTest :
    BehaviorSpec({
        given("the introduction shown as the first screen") {
            `when`("the user allows diagnostics") {
                then("the answer is stored with both versions and the trip list becomes the root") {
                    val settings = FakeAppSettings()
                    val screen = IntroFlowScreen(requirement = IntroRequirement.Full, fromStart = true)
                    val navigator = FakeNavigator(screen)

                    presenterTestOf({ presenter(screen, navigator, settings) }) {
                        awaitLoaded().eventSink(IntroEvent.Answered(TelemetryConsent.Granted))

                        eventually(2.seconds) {
                            settings.settings.value.telemetryConsent shouldBe TelemetryConsent.Granted
                            settings.settings.value.acknowledgedIntroVersion shouldBe INTRO_VERSION
                            settings.settings.value.acknowledgedPrivacyVersion shouldBe POLICY_VERSION
                            (settings.settings.value.consentDecidedAt != null) shouldBe true
                        }

                        navigator.awaitResetRoot().newRoot shouldBe TravelListScreen
                        cancelAndIgnoreRemainingEvents()
                    }
                }
            }

            `when`("the whole introduction is due") {
                then("the reading cards are part of it") {
                    val screen = IntroFlowScreen(requirement = IntroRequirement.Full, fromStart = true)

                    presenterTestOf({ presenter(screen, FakeNavigator(screen), FakeAppSettings()) }) {
                        awaitLoaded().steps.map { it.id } shouldBe listOf("welcome", "privacy", "decision")

                        cancelAndIgnoreRemainingEvents()
                    }
                }
            }
        }

        given("only the privacy half being due") {
            `when`("the steps are read") {
                then("the reading cards are left out and the question is not") {
                    val screen = IntroFlowScreen(requirement = IntroRequirement.PrivacyOnly, fromStart = true)

                    presenterTestOf({ presenter(screen, FakeNavigator(screen), FakeAppSettings()) }) {
                        awaitLoaded().steps.map { it.id } shouldBe listOf("privacy", "decision")

                        cancelAndIgnoreRemainingEvents()
                    }
                }
            }
        }

        given("a privacy point") {
            `when`("it is touched") {
                then("the policy is opened on the section it summarises") {
                    val screen = IntroFlowScreen()
                    val navigator = FakeNavigator(screen)

                    presenterTestOf({ presenter(screen, navigator, FakeAppSettings()) }) {
                        awaitLoaded().eventSink(IntroEvent.PolicyOpened("collection.automatic"))

                        navigator.awaitNextScreen() shouldBe PrivacyPolicyScreen(anchor = "collection.automatic")
                        cancelAndIgnoreRemainingEvents()
                    }
                }
            }

            `when`("it names a section nobody wrote") {
                then("nowhere is navigated to") {
                    val screen = IntroFlowScreen()
                    val navigator = FakeNavigator(screen)

                    presenterTestOf({ presenter(screen, navigator, FakeAppSettings()) }) {
                        awaitLoaded().eventSink(IntroEvent.PolicyOpened("collection.nowhere"))

                        expectNoEvents()
                        navigator.assertGoToIsEmpty()

                        cancelAndIgnoreRemainingEvents()
                    }
                }
            }
        }

        given("the introduction reopened from somewhere else") {
            `when`("the user refuses") {
                then("the refusal is stored and the screen is popped, not the whole stack reset") {
                    val settings = FakeAppSettings()
                    val screen = IntroFlowScreen(requirement = IntroRequirement.PrivacyOnly, fromStart = false)
                    val navigator = FakeNavigator(screen)

                    presenterTestOf({ presenter(screen, navigator, settings) }) {
                        awaitLoaded().eventSink(IntroEvent.Answered(TelemetryConsent.Denied))

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

/** The first state whose steps have been read; before that the introduction has nothing to draw. */
private suspend fun CircuitReceiveTurbine<IntroUiState>.awaitLoaded(): IntroUiState {
    var state = awaitItem()
    while (state.steps.isEmpty()) {
        state = awaitItem()
    }
    return state
}

/** The presenter over content of our own, so the test does not depend on the shipped text. */
@Composable
private fun presenter(screen: IntroFlowScreen, navigator: FakeNavigator, settings: AppSettingsRepository) =
    IntroPresenter(
        screen = screen,
        navigator = navigator,
        staticContentRepository = StaticContentRepository(
            introDataSource = IntroFlowDataSource { Json.encodeToString(TEST_FLOW).encodeToByteArray() },
            privacyDataSource = PrivacyPolicyDataSource { Json.encodeToString(TEST_POLICY).encodeToByteArray() },
        ),
        appSettingsRepository = settings,
        appLogger = testAppLogger(),
    )

private val TEST_FLOW = IntroFlow(
    version = INTRO_VERSION,
    language = "en",
    steps = listOf(
        IntroStep.Card(id = "welcome", title = "Welcome", body = "Everything **local**."),
        IntroStep.Privacy(
            id = "privacy",
            title = "In short",
            details = listOf(
                PrivacyDetail(
                    id = "automatic",
                    title = "Only if you say so",
                    body = "Nothing leaves before you answer.",
                    policyRef = "collection.automatic",
                ),
            ),
        ),
        IntroStep.Decision(id = "decision", title = "Send diagnostics?", body = "Your call."),
    ),
)

private val TEST_POLICY = PrivacyPolicy(
    version = POLICY_VERSION,
    language = "en",
    sections = listOf(
        PrivacyPolicySection(
            id = "collection",
            title = "1. Information We Collect",
            body = "What is handled.",
            subsections = listOf(
                PrivacyPolicySection(
                    id = "automatic",
                    title = "1.2 Information Collected Automatically",
                    body = "Only if you say so.",
                ),
            ),
        ),
    ),
)

/** The preferences, in memory, with the one write this test observes. */
private class FakeAppSettings : AppSettingsRepository {

    private val state = MutableStateFlow(AppSettingsDomain())

    override val settings: StateFlow<AppSettingsDomain> = state

    override suspend fun updateNavigatorRemote(baseUrl: String) = error("Not written here")

    override suspend fun updateTelemetryConsent(
        consent: TelemetryConsent,
        introVersion: Int,
        privacyVersion: Int,
        decidedAt: Instant,
    ) {
        state.value = state.value.copy(
            telemetryConsent = consent,
            acknowledgedIntroVersion = introVersion,
            acknowledgedPrivacyVersion = privacyVersion,
            consentDecidedAt = decidedAt,
        )
    }

    override suspend fun updateLogRetentionDays(days: Int) = error("Not written here")

    override suspend fun installationId(): String = "test-installation"
}
