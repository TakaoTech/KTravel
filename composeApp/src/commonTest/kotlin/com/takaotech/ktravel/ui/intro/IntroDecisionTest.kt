package com.takaotech.ktravel.ui.intro

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsOff
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.v2.runComposeUiTest
import com.takaotech.ktravel.core.telemetry.TelemetryConsent
import com.takaotech.ktravel.domain.staticflows.IntroStep
import com.takaotech.ktravel.presentation.intro.IntroEvent
import com.takaotech.ktravel.presentation.intro.IntroUiState
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import kotlinx.collections.immutable.persistentListOf

/**
 * The last step, and where the switch on it leaves diagnostics.
 *
 * Nothing is asked here, so nothing blocks: the step opens on diagnostics running and the button
 * that closes the introduction is live from the first frame. The one thing that must hold is that
 * what leaves is what the switch was last showing — an introduction tapped through must record the
 * state the user was looking at, and an objection must survive being made.
 */
@OptIn(ExperimentalTestApi::class)
class IntroDecisionTest :
    BehaviorSpec({
        given("the step that says diagnostics are on") {
            `when`("it is reached and nothing has been touched") {
                then("the switch is on and there is nothing left to answer") {
                    runComposeUiTest {
                        setContent { IntroContent(state = IntroUiState(steps = TEST_STEPS) {}) }

                        onNodeWithTag(IntroTestTags.DIAGNOSTICS_TOGGLE).performScrollTo().assertIsOn()
                        onNodeWithTag(IntroTestTags.FINISH).assertIsEnabled()
                    }
                }
            }

            `when`("the reader closes the introduction without touching the switch") {
                then("diagnostics are recorded as left on") {
                    var answered: TelemetryConsent? = null

                    runComposeUiTest {
                        setContent {
                            IntroContent(
                                state = IntroUiState(steps = TEST_STEPS) { event ->
                                    if (event is IntroEvent.Answered) answered = event.consent
                                },
                            )
                        }

                        onNodeWithTag(IntroTestTags.FINISH).performClick()
                    }

                    answered shouldBe TelemetryConsent.Granted
                }
            }

            `when`("the reader turns the switch off") {
                then("the objection is what leaves") {
                    var answered: TelemetryConsent? = null

                    runComposeUiTest {
                        setContent {
                            IntroContent(
                                state = IntroUiState(steps = TEST_STEPS) { event ->
                                    if (event is IntroEvent.Answered) answered = event.consent
                                },
                            )
                        }

                        onNodeWithTag(IntroTestTags.DIAGNOSTICS_TOGGLE)
                            .performScrollTo()
                            .performClick()
                            .assertIsOff()

                        onNodeWithTag(IntroTestTags.FINISH).assertIsEnabled().performClick()
                    }

                    answered shouldBe TelemetryConsent.Denied
                }
            }

            `when`("the reader turns it off and on again") {
                then("the last position is the one recorded") {
                    var answered: TelemetryConsent? = null

                    runComposeUiTest {
                        setContent {
                            IntroContent(
                                state = IntroUiState(steps = TEST_STEPS) { event ->
                                    if (event is IntroEvent.Answered) answered = event.consent
                                },
                            )
                        }

                        onNodeWithTag(IntroTestTags.DIAGNOSTICS_TOGGLE)
                            .performScrollTo()
                            .performClick()
                            .performClick()
                            .assertIsOn()

                        onNodeWithTag(IntroTestTags.FINISH).performClick()
                    }

                    answered shouldBe TelemetryConsent.Granted
                }
            }
        }

        given("an installation that had already objected") {
            `when`("the step opens") {
                then("the switch is off, and stays off unless it is touched") {
                    var answered: TelemetryConsent? = null

                    runComposeUiTest {
                        setContent {
                            IntroContent(
                                state = IntroUiState(
                                    steps = TEST_STEPS,
                                    initialConsent = TelemetryConsent.Denied,
                                ) { event ->
                                    if (event is IntroEvent.Answered) answered = event.consent
                                },
                            )
                        }

                        onNodeWithTag(IntroTestTags.DIAGNOSTICS_TOGGLE).performScrollTo().assertIsOff()
                        onNodeWithTag(IntroTestTags.FINISH).performClick()
                    }

                    answered shouldBe TelemetryConsent.Denied
                }
            }
        }
    })

private val TEST_STEPS = persistentListOf(
    IntroStep.Decision(
        id = "decision",
        title = "Diagnostics is on",
        body = "Crashes and technical logs only.",
        policyRef = "collection.automatic",
    ),
)
