package com.takaotech.ktravel.ui.planning.detail

import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier

/**
 * Fallback iOS: Hyphen non pubblica artefatti iOS, quindi si modifica il Markdown grezzo con un
 * campo di testo standard. Il contenuto resta comunque renderizzabile da mikepenz in sola lettura.
 */
@Composable
actual fun MarkdownNoteEditor(
    initialValue: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier,
) {
    var value by remember { mutableStateOf(initialValue) }
    OutlinedTextField(
        value = value,
        onValueChange = {
            value = it
            onValueChange(it)
        },
        modifier = modifier,
        label = { Text(label) }
    )
}
