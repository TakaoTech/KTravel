package com.takaotech.ktravel.ui.shared.dialog

import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.v2.runComposeUiTest
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactly

private const val SHOW_BUTTON = "show_dialog_button"

/** Collects the payloads received by the confirmation callback. */
private class CallbackSink {
    val received = mutableListOf<String>()
}

/**
 * The confirmation lambda captures [callbackTag], so it gets a new identity every time that state
 * changes. This is what happens in the real screens too, where the lambda captures a ViewModel or a
 * piece of ui state: the state holder must survive it, otherwise an open dialog closes by itself.
 *
 * The dialog is queried by test tag rather than by label: labels come from
 * [org.jetbrains.compose.resources.stringResource], which resolves in the JVM default locale, so
 * matching their wording would tie the test to whichever language the host machine runs in.
 */
@OptIn(ExperimentalTestApi::class)
class DisruptiveOperationDialogTest : BehaviorSpec() {

    init {
        given("a dialog opened through rememberDisruptiveOperationDialog") {
            `when`("the confirmation lambda changes identity while the dialog is open") {
                then("the dialog should stay open") {
                    val sink = CallbackSink()
                    val callbackTag = mutableStateOf("first")

                    runComposeUiTest {
                        setContent {
                            val currentTag = callbackTag.value
                            val dialogState = rememberDisruptiveOperationDialog<String> { id ->
                                sink.received.add("$currentTag:$id")
                            }

                            DisruptiveOperationDialog(state = dialogState)

                            Button(
                                modifier = Modifier.testTag(SHOW_BUTTON),
                                onClick = { dialogState.show("id-1") },
                            ) {
                                Text(text = "show")
                            }
                        }

                        onNodeWithTag(SHOW_BUTTON).performClick()
                        onNodeWithTag(DisruptiveOperationDialogTestTags.DIALOG).assertIsDisplayed()

                        // A state holder keyed on the lambda would be rebuilt here, resetting
                        // showDialog to false and dismissing the dialog behind the user's back.
                        callbackTag.value = "second"
                        waitForIdle()

                        onNodeWithTag(DisruptiveOperationDialogTestTags.DIALOG).assertIsDisplayed()
                    }
                }
            }

            `when`("the confirmation lambda changes and the deletion is confirmed") {
                then("the most recent lambda should be invoked with the pending payload") {
                    val sink = CallbackSink()
                    val callbackTag = mutableStateOf("first")

                    runComposeUiTest {
                        setContent {
                            val currentTag = callbackTag.value
                            val dialogState = rememberDisruptiveOperationDialog<String> { id ->
                                sink.received.add("$currentTag:$id")
                            }

                            DisruptiveOperationDialog(state = dialogState)

                            Button(
                                modifier = Modifier.testTag(SHOW_BUTTON),
                                onClick = { dialogState.show("id-1") },
                            ) {
                                Text(text = "show")
                            }
                        }

                        onNodeWithTag(SHOW_BUTTON).performClick()

                        callbackTag.value = "second"
                        waitForIdle()

                        onNodeWithTag(DisruptiveOperationDialogTestTags.CONFIRM).performClick()
                        waitForIdle()
                    }

                    sink.received shouldContainExactly listOf("second:id-1")
                }
            }

            `when`("the deletion is dismissed") {
                then("the dialog should close without invoking the callback") {
                    val sink = CallbackSink()

                    runComposeUiTest {
                        setContent {
                            val dialogState = rememberDisruptiveOperationDialog<String> { id ->
                                sink.received.add(id)
                            }

                            DisruptiveOperationDialog(state = dialogState)

                            Button(
                                modifier = Modifier.testTag(SHOW_BUTTON),
                                onClick = { dialogState.show("id-1") },
                            ) {
                                Text(text = "show")
                            }
                        }

                        onNodeWithTag(SHOW_BUTTON).performClick()
                        onNodeWithTag(DisruptiveOperationDialogTestTags.DIALOG).assertIsDisplayed()

                        onNodeWithTag(DisruptiveOperationDialogTestTags.CANCEL).performClick()
                        waitForIdle()

                        onNodeWithTag(DisruptiveOperationDialogTestTags.DIALOG)
                            .assertDoesNotExist()
                    }

                    sink.received.shouldBeEmpty()
                }
            }
        }
    }
}
