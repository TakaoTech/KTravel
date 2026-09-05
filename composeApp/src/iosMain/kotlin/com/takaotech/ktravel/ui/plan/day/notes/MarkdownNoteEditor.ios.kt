package com.takaotech.ktravel.ui.plan.day.notes

import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * On iOS the note is edited as raw Markdown in a plain text field, since the WYSIWYG editor the
 * other platforms use is not available here.
 *
 * What is written is still ordinary Markdown, so the read-only rendering is the same everywhere.
 */
@Composable
actual fun MarkdownNoteEditor(controller: MarkdownEditorController, label: String, modifier: Modifier) {
    OutlinedTextField(
        value = controller.fieldValue,
        onValueChange = { controller.onFieldValueChange(it) },
        modifier = modifier,
        label = { Text(label) },
    )
}
