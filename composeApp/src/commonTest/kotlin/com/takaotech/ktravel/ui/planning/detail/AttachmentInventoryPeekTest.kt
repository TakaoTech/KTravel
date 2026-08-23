@file:OptIn(ExperimentalTestApi::class, ExperimentalMaterial3Api::class)

package com.takaotech.ktravel.ui.planning.detail

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.v2.runComposeUiTest
import androidx.compose.ui.unit.Dp
import io.github.vinceglb.filekit.PlatformFile
import io.kotest.core.spec.style.BehaviorSpec
import kotlinx.io.files.Path

/**
 * A closed sheet has to say what it holds.
 *
 * `BottomSheetDefaults.SheetPeekHeight` is barely taller than the drag handle, so hosting the
 * inventory in a sheet at the default left a nameless strip at the bottom of the screen: the title
 * and the add button next to it were both below the fold, and nothing invited the traveller to pull
 * it up. Both cases are pinned here — the default is what the screens used to do.
 */
class AttachmentInventoryPeekTest : BehaviorSpec() {

    init {
        given("the attachment inventory in a closed bottom sheet") {
            `when`("it peeks at the height the inventory asks for") {
                then("its header should be on screen") {
                    runComposeUiTest {
                        setContent { SheetPeeking(at = ATTACHMENT_INVENTORY_PEEK_HEIGHT) }

                        onNodeWithTag(StepDetailTestTags.NOTES.attachmentAdd).assertIsDisplayed()
                    }
                }
            }

            `when`("it peeks at the Material default instead") {
                then("the header should be cut off, which is the regression this guards") {
                    runComposeUiTest {
                        setContent { SheetPeeking(at = BottomSheetDefaults.SheetPeekHeight) }

                        onNodeWithTag(StepDetailTestTags.NOTES.attachmentAdd).assertIsNotDisplayed()
                    }
                }
            }
        }
    }

    @Composable
    private fun SheetPeeking(at: Dp) = BottomSheetScaffold(
        modifier = Modifier.fillMaxSize(),
        sheetPeekHeight = at,
        sheetContent = {
            AttachmentInventorySection(
                attachments = emptyList(),
                resolveFile = { PlatformFile(Path(it)) },
                isEditing = false,
                testTags = StepDetailTestTags.NOTES,
                onAdd = {},
                onInsert = {},
                onOpen = {},
                onRemove = {},
            )
        },
    ) {
        Text("body")
    }
}
