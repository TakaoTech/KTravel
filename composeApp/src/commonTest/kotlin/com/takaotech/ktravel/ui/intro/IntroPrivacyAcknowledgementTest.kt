package com.takaotech.ktravel.ui.intro

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertIsOff
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performFirstLinkClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.v2.runComposeUiTest
import com.takaotech.ktravel.domain.staticflows.IntroStep
import com.takaotech.ktravel.domain.staticflows.PrivacyDetail
import com.takaotech.ktravel.presentation.intro.IntroEvent
import com.takaotech.ktravel.presentation.intro.IntroUiState
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import kotlinx.collections.immutable.persistentListOf

/**
 * The one step of the introduction that has to be answered before it is left.
 *
 * The privacy step is where the reader is told what the application does with what it is given, and
 * the tick under it is the record that they were told. Until it is given, there is no way on.
 */
@OptIn(ExperimentalTestApi::class)
class IntroPrivacyAcknowledgementTest :
    BehaviorSpec({
        given("an introduction whose privacy step comes before the question") {
            `when`("the privacy step is on screen and nothing has been ticked") {
                then("the way on is shown, and refuses to be taken") {
                    runComposeUiTest {
                        setContent { IntroContent(state = IntroUiState(steps = TEST_STEPS) {}) }

                        onNodeWithTag(IntroTestTags.step("privacy")).assertIsDisplayed()
                        onNodeWithTag(IntroTestTags.NEXT).assertIsNotEnabled()
                    }
                }
            }

            `when`("the reader ticks the acknowledgement") {
                then("the way on opens, and the next step is the question") {
                    runComposeUiTest {
                        setContent { IntroContent(state = IntroUiState(steps = TEST_STEPS) {}) }

                        // The step scrolls, so the tick has to be brought into view before it
                        // can be touched: off screen, the click lands on nothing.
                        onNodeWithTag(IntroTestTags.ACKNOWLEDGE)
                            .performScrollTo()
                            .performClick()
                            .assertIsOn()

                        onNodeWithTag(IntroTestTags.NEXT).assertIsEnabled().performClick()
                        onNodeWithTag(IntroTestTags.step("decision")).assertIsDisplayed()
                    }
                }
            }

            `when`("the reader touches the link the acknowledgement ends on") {
                then("the whole policy is asked for, and nothing is ticked by the touch") {
                    runComposeUiTest {
                        val events = mutableListOf<IntroEvent>()

                        setContent { IntroContent(state = IntroUiState(steps = TEST_STEPS) { events += it }) }

                        onNodeWithTag(IntroTestTags.ACKNOWLEDGE).performScrollTo()
                        // The link lives inside the toggleable row, which merges what is under it:
                        // unmerged is the only tree the text it is annotated on is a node of.
                        onNodeWithTag(IntroTestTags.ACKNOWLEDGE_LINK, useUnmergedTree = true)
                            .performFirstLinkClick()

                        events shouldBe listOf(IntroEvent.PolicyOpened(policyRef = null))
                        onNodeWithTag(IntroTestTags.ACKNOWLEDGE).assertIsOff()
                    }
                }
            }
        }
    })

private val TEST_STEPS = persistentListOf(
    IntroStep.Privacy(
        id = "privacy",
        title = "Where your trips live",
        body = "The short version, in one point.",
        details = listOf(
            PrivacyDetail(
                id = "provided",
                title = "Your trips stay on this device",
                body = "No account, no server of ours.",
                policyRef = "collection.provided",
            ),
        ),
    ),
    IntroStep.Decision(
        id = "decision",
        title = "Send diagnostics?",
        body = "Crashes and diagnostic logs only.",
        policyRef = "collection.automatic",
    ),
)
