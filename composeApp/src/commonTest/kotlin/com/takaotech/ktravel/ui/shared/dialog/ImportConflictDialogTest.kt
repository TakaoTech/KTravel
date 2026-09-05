package com.takaotech.ktravel.ui.shared.dialog

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.v2.runComposeUiTest
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.collections.shouldContainExactly

/** Ogni azione del dialog deve invocare la propria callback e nessun'altra. */
@OptIn(ExperimentalTestApi::class)
class ImportConflictDialogTest : BehaviorSpec() {

    init {
        given("an import conflict dialog") {

            `when`("the duplicate action is clicked") {
                then("only onDuplicate is invoked") {
                    val invoked = clickAndCollect(ImportConflictDialogTestTags.DUPLICATE)
                    invoked shouldContainExactly listOf("duplicate")
                }
            }

            `when`("the replace action is clicked") {
                then("only onReplace is invoked") {
                    val invoked = clickAndCollect(ImportConflictDialogTestTags.REPLACE)
                    invoked shouldContainExactly listOf("replace")
                }
            }

            `when`("the cancel action is clicked") {
                then("only onDismiss is invoked") {
                    val invoked = clickAndCollect(ImportConflictDialogTestTags.CANCEL)
                    invoked shouldContainExactly listOf("dismiss")
                }
            }
        }
    }
}

@OptIn(ExperimentalTestApi::class)
private fun clickAndCollect(tag: String): List<String> {
    val invoked = mutableListOf<String>()
    runComposeUiTest {
        setContent {
            ImportConflictDialog(
                existingName = "Tokyo",
                onDuplicate = { invoked += "duplicate" },
                onReplace = { invoked += "replace" },
                onDismiss = { invoked += "dismiss" },
            )
        }
        onNodeWithTag(tag).performClick()
    }
    return invoked
}
