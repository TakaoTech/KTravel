package com.takaotech.ktravel.ui.plan.day

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.platform.WindowInfo
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.v2.runComposeUiTest
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe

/**
 * The layout branches on the window size class, which on every target is read off
 * [LocalWindowInfo]: overriding it is what lets one test cover a phone and a desktop alike.
 */
private class FixedWindowInfo(override val containerSize: IntSize) : WindowInfo {
    override val isWindowFocused = true
}

private const val ITINERARY_CONTENT = "itinerary_content"
private const val BACKLOG_CONTENT = "backlog_content"

@OptIn(ExperimentalTestApi::class)
class DayDetailLayoutTest : BehaviorSpec() {

    init {
        given("DayDetailLayout on a compact window") {
            `when`("the backlog is open") {
                then("the scrim should cover the itinerary") {
                    runComposeUiTest {
                        setContent { Layout(width = 390, backlogOpen = true) }
                        onNodeWithTag(DayDetailTestTags.BACKLOG_PANE).assertIsDisplayed()
                        onNodeWithTag(DayDetailTestTags.SCRIM).assertIsDisplayed()
                    }
                }

                then("clicking the scrim should dismiss the backlog") {
                    var dismissed = false
                    runComposeUiTest {
                        setContent {
                            Layout(width = 390, backlogOpen = true, onDismiss = { dismissed = true })
                        }
                        onNodeWithTag(DayDetailTestTags.SCRIM).performClick()
                    }
                    dismissed shouldBe true
                }
            }

            `when`("the backlog is closed") {
                then("neither the pane nor the scrim should exist") {
                    runComposeUiTest {
                        setContent { Layout(width = 390, backlogOpen = false) }
                        onNodeWithTag(DayDetailTestTags.BACKLOG_PANE).assertDoesNotExist()
                        onNodeWithTag(DayDetailTestTags.SCRIM).assertDoesNotExist()
                        onNodeWithTag(ITINERARY_CONTENT).assertIsDisplayed()
                    }
                }
            }
        }

        given("DayDetailLayout on an expanded window") {
            `when`("the backlog is open") {
                then("the pane should sit beside the itinerary, with no scrim") {
                    runComposeUiTest {
                        setContent { Layout(width = 1100, backlogOpen = true) }
                        onNodeWithTag(DayDetailTestTags.BACKLOG_PANE).assertIsDisplayed()
                        onNodeWithTag(ITINERARY_CONTENT).assertIsDisplayed()
                        onNodeWithTag(DayDetailTestTags.SCRIM).assertDoesNotExist()
                    }
                }
            }
        }
    }
}

@Composable
private fun Layout(width: Int, backlogOpen: Boolean, onDismiss: () -> Unit = {}) {
    val height = 800
    CompositionLocalProvider(
        LocalWindowInfo provides FixedWindowInfo(IntSize(width, height)),
    ) {
        Box(Modifier.size(width.dp, height.dp)) {
            DayDetailLayout(
                backlogOpen = backlogOpen,
                onBacklogDismiss = onDismiss,
                itinerary = { Box(Modifier.size(width.dp, height.dp).testTag(ITINERARY_CONTENT)) },
                backlog = { Box(Modifier.size(width.dp, height.dp).testTag(BACKLOG_CONTENT)) },
            )
        }
    }
}
