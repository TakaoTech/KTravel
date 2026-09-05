package com.takaotech.ktravel.ui.shared.dialog

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.v2.runComposeUiTest
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe

/** Each action must invoke its own callback and no other, and the password must reach it intact. */
@OptIn(ExperimentalTestApi::class)
class ImportSecretsDialogsTest : BehaviorSpec() {

    init {
        given("the dialog asking whether to import the API key") {

            `when`("the import action is clicked") {
                then("only onImport is invoked") {
                    val invoked = mutableListOf<String>()
                    runComposeUiTest {
                        setContent {
                            ImportSecretsChoiceDialog(
                                onImport = { invoked += "import" },
                                onSkip = { invoked += "skip" },
                            )
                        }
                        onNodeWithTag(ImportSecretsDialogTestTags.CHOICE_IMPORT).performClick()
                    }
                    invoked shouldContainExactly listOf("import")
                }
            }

            `when`("the skip action is clicked") {
                then("only onSkip is invoked") {
                    val invoked = mutableListOf<String>()
                    runComposeUiTest {
                        setContent {
                            ImportSecretsChoiceDialog(
                                onImport = { invoked += "import" },
                                onSkip = { invoked += "skip" },
                            )
                        }
                        onNodeWithTag(ImportSecretsDialogTestTags.CHOICE_SKIP).performClick()
                    }
                    invoked shouldContainExactly listOf("skip")
                }
            }
        }

        given("the dialog asking for the password") {

            `when`("no password has been typed") {
                then("the unlock action is disabled, so an empty attempt cannot be made") {
                    runComposeUiTest {
                        setContent {
                            ImportSecretsPasswordDialog(
                                attemptFailed = false,
                                onConfirm = { },
                                onCancel = { },
                            )
                        }
                        onNodeWithTag(ImportSecretsDialogTestTags.PASSWORD_CONFIRM)
                            .assertIsNotEnabled()
                    }
                }
            }

            `when`("a password is typed and confirmed") {
                then("the typed password is handed over unchanged") {
                    var submitted: String? = null
                    runComposeUiTest {
                        setContent {
                            ImportSecretsPasswordDialog(
                                attemptFailed = false,
                                onConfirm = { submitted = it },
                                onCancel = { },
                            )
                        }
                        onNodeWithTag(ImportSecretsDialogTestTags.PASSWORD)
                            .performTextInput("hunter2")
                        onNodeWithTag(ImportSecretsDialogTestTags.PASSWORD_CONFIRM)
                            .assertIsEnabled()
                            .performClick()
                    }
                    submitted shouldBe "hunter2"
                }
            }

            `when`("cancel is clicked") {
                then("only onCancel is invoked") {
                    val invoked = mutableListOf<String>()
                    runComposeUiTest {
                        setContent {
                            ImportSecretsPasswordDialog(
                                attemptFailed = false,
                                onConfirm = { invoked += "confirm" },
                                onCancel = { invoked += "cancel" },
                            )
                        }
                        onNodeWithTag(ImportSecretsDialogTestTags.PASSWORD_CANCEL).performClick()
                    }
                    invoked shouldContainExactly listOf("cancel")
                }
            }

            `when`("a previous attempt failed") {
                then("the field is still usable, so the user can retry") {
                    var submitted: String? = null
                    runComposeUiTest {
                        setContent {
                            ImportSecretsPasswordDialog(
                                attemptFailed = true,
                                onConfirm = { submitted = it },
                                onCancel = { },
                            )
                        }
                        onNodeWithTag(ImportSecretsDialogTestTags.PASSWORD)
                            .performTextInput("second try")
                        onNodeWithTag(ImportSecretsDialogTestTags.PASSWORD_CONFIRM).performClick()
                    }
                    submitted shouldBe "second try"
                }
            }
        }
    }
}
