package com.takaotech.ktravel.ui.planning.detail

import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * Fallback iOS: si modifica il Markdown grezzo con un campo di testo standard, pilotato dal
 * [MarkdownEditorController] condiviso (che traccia testo e selezione). Il contenuto resta
 * renderizzabile da mikepenz in sola lettura.
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
