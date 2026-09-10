package com.takaotech.ktravel.ui.intro

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsSelected
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
 * The step that asks the question, and what pressing the button on it actually answers.
 *
 * Nothing is picked for the reader, and nothing can be recorded until they pick: an introduction
 * tapped through without being read must not be able to leave a consent behind it. What they last
 * selected is what leaves, and nothing else.
 */
@OptIn(ExperimentalTestApi::class)
class IntroDecisionTest :
    BehaviorSpec({
        given("the step that asks whether diagnostics may be sent") {
            `when`("it is reached and nothing has been touched") {
                then("neither answer is picked, and there is nothing to confirm yet") {
                    runComposeUiTest {
                        setContent { IntroContent(state = IntroUiState(steps = TEST_STEPS) {}) }

                        onNodeWithTag(IntroTestTags.ALLOW).performScrollTo().assertIsNotSelected()
                        onNodeWithTag(IntroTestTags.DENY).assertIsNotSelected()
                        onNodeWithTag(IntroTestTags.FINISH).assertIsNotEnabled()
                    }
                }
            }

            `when`("the reader picks keeping everything local") {
                then("that is the consent that leaves") {
                    var answered: TelemetryConsent? = null

                    runComposeUiTest {
                        setContent {
                            IntroContent(
                                state = IntroUiState(steps = TEST_STEPS) { event ->
                                    if (event is IntroEvent.Answered) answered = event.consent
                                },
                            )
                        }

                        onNodeWithTag(IntroTestTags.DENY)
                            .performScrollTo()
                            .performClick()
                            .assertIsSelected()

                        onNodeWithTag(IntroTestTags.FINISH).assertIsEnabled().performClick()
                    }

                    answered shouldBe TelemetryConsent.Denied
                }
            }

            `when`("the reader picks sending the diagnostics") {
                then("that is the consent that leaves") {
                    var answered: TelemetryConsent? = null

                    runComposeUiTest {
                        setContent {
                            IntroContent(
                                state = IntroUiState(steps = TEST_STEPS) { event ->
                                    if (event is IntroEvent.Answered) answered = event.consent
                                },
                            )
                        }

                        onNodeWithTag(IntroTestTags.ALLOW)
                            .performScrollTo()
                            .performClick()
                            .assertIsSelected()
                        onNodeWithTag(IntroTestTags.DENY).assertIsNotSelected()

                        onNodeWithTag(IntroTestTags.FINISH).assertIsEnabled().performClick()
                    }

                    answered shouldBe TelemetryConsent.Granted
                }
            }
        }
    })

private val TEST_STEPS = persistentListOf(
    IntroStep.Decision(
        id = "decision",
        title = "Send diagnostics?",
        body = "Crashes and diagnostic logs only.",
        policyRef = "collection.automatic",
    ),
)
