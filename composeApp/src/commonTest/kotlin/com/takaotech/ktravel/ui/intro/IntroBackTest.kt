package com.takaotech.ktravel.ui.intro

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.v2.runComposeUiTest
import com.takaotech.ktravel.domain.staticflows.IntroStep
import com.takaotech.ktravel.presentation.intro.IntroUiState
import io.kotest.core.spec.style.BehaviorSpec
import kotlinx.collections.immutable.persistentListOf

/**
 * Walking the introduction backwards.
 *
 * Forward is the one thing the introduction always offered; going back is what lets a reader check
 * a step they have already passed without answering the question first.
 */
@OptIn(ExperimentalTestApi::class)
class IntroBackTest :
    BehaviorSpec({
        given("an introduction of two steps") {
            `when`("the first step is the one being read") {
                then("there is nothing to go back to, and no arrow offering it") {
                    runComposeUiTest {
                        setContent { IntroContent(state = IntroUiState(steps = TEST_STEPS) {}) }

                        onNodeWithTag(IntroTestTags.step("welcome")).assertIsDisplayed()
                        onNodeWithTag(IntroTestTags.BACK).assertDoesNotExist()
                    }
                }
            }

            `when`("the reader has moved on and then asks to go back") {
                then("the step before is the one on screen again") {
                    runComposeUiTest {
                        setContent { IntroContent(state = IntroUiState(steps = TEST_STEPS) {}) }

                        onNodeWithTag(IntroTestTags.NEXT).performClick()
                        onNodeWithTag(IntroTestTags.step("decision")).assertIsDisplayed()

                        onNodeWithTag(IntroTestTags.BACK).performClick()

                        onNodeWithTag(IntroTestTags.step("welcome")).assertIsDisplayed()
                        onNodeWithTag(IntroTestTags.BACK).assertDoesNotExist()
                    }
                }
            }
        }
    })

private val TEST_STEPS = persistentListOf(
    IntroStep.Card(
        id = "welcome",
        title = "Welcome",
        body = "What this application is, in a sentence.",
    ),
    IntroStep.Decision(
        id = "decision",
        title = "Send diagnostics?",
        body = "Crashes and diagnostic logs only.",
        policyRef = "collection.automatic",
    ),
)
