package com.takaotech.ktravel.ui.common

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.v2.runComposeUiTest
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe

/**
 * The dialog gates the export on a password worth having: it must refuse to proceed on a weak or
 * mismatched one, and let the user opt out entirely.
 */
@OptIn(ExperimentalTestApi::class)
class ExportSecretsDialogTest : BehaviorSpec() {

    init {
        given("the export secrets dialog") {

            `when`("nothing has been typed yet") {
                then("exporting is blocked, because the key would be sealed under nothing") {
                    runComposeUiTest {
                        setContent {
                            ExportSecretsDialog(onConfirm = { }, onDismiss = { })
                        }
                        onNodeWithTag(ExportSecretsDialogTestTags.CONFIRM).assertIsNotEnabled()
                    }
                }
            }

            `when`("a notoriously weak password is typed twice") {
                then("exporting stays blocked") {
                    runComposeUiTest {
                        setContent {
                            ExportSecretsDialog(onConfirm = { }, onDismiss = { })
                        }
                        onNodeWithTag(ExportSecretsDialogTestTags.PASSWORD)
                            .performTextInput("password")
                        onNodeWithTag(ExportSecretsDialogTestTags.CONFIRM_PASSWORD)
                            .performTextInput("password")
                        waitForIdle()
                        onNodeWithTag(ExportSecretsDialogTestTags.CONFIRM).assertIsNotEnabled()
                    }
                }
            }

            `when`("a strong password is typed but the confirmation differs") {
                then("exporting stays blocked") {
                    runComposeUiTest {
                        setContent {
                            ExportSecretsDialog(onConfirm = { }, onDismiss = { })
                        }
                        onNodeWithTag(ExportSecretsDialogTestTags.PASSWORD)
                            .performTextInput("chair velvet lantern rust")
                        onNodeWithTag(ExportSecretsDialogTestTags.CONFIRM_PASSWORD)
                            .performTextInput("chair velvet lantern rusty")
                        waitForIdle()
                        onNodeWithTag(ExportSecretsDialogTestTags.CONFIRM).assertIsNotEnabled()
                    }
                }
            }

            `when`("the key is excluded") {
                then("exporting is allowed immediately, with no password") {
                    var confirmedWith: String? = "not called"
                    runComposeUiTest {
                        setContent {
                            ExportSecretsDialog(
                                onConfirm = { confirmedWith = it },
                                onDismiss = { },
                            )
                        }
                        // Unticking the checkbox removes the password fields entirely.
                        onNodeWithTag(ExportSecretsDialogTestTags.INCLUDE).performClick()
                        waitForIdle()
                        onNodeWithTag(ExportSecretsDialogTestTags.CONFIRM).performClick()
                    }
                    confirmedWith shouldBe null
                }
            }

            `when`("cancel is clicked") {
                then("only onDismiss is invoked") {
                    var dismissed = false
                    var confirmed = false
                    runComposeUiTest {
                        setContent {
                            ExportSecretsDialog(
                                onConfirm = { confirmed = true },
                                onDismiss = { dismissed = true },
                            )
                        }
                        onNodeWithTag(ExportSecretsDialogTestTags.CANCEL).performClick()
                    }
                    dismissed shouldBe true
                    confirmed shouldBe false
                }
            }
        }
    }
}
