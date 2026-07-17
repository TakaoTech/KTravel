package com.takaotech.ktravel.ui.planning.detail

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

/**
 * Actual iOS: Hyphen non pubblica artefatti iOS, quindi l'editor lavora sul Markdown grezzo con un
 * [TextFieldValue]. La [selection] permette comunque l'inserimento **al cursore**.
 */
@Stable
actual class MarkdownEditorController internal constructor(initialValue: String) {

    internal var fieldValue by mutableStateOf(TextFieldValue(initialValue))
        private set

    private val _markdownFlow = MutableStateFlow(initialValue)
    actual val markdownFlow: Flow<String> = _markdownFlow

    /** Aggiornamento dall'editor (digitazione / spostamento cursore). */
    internal fun onFieldValueChange(new: TextFieldValue) {
        fieldValue = new
        _markdownFlow.value = new.text
    }

    actual fun insertAtCursor(text: String) {
        val current = fieldValue
        val selection = current.selection
        val newText = current.text.replaceRange(selection.min, selection.max, text)
        val cursor = selection.min + text.length
        onFieldValueChange(TextFieldValue(text = newText, selection = TextRange(cursor)))
    }
}

@Composable
actual fun rememberMarkdownEditorController(initialValue: String): MarkdownEditorController =
    remember { MarkdownEditorController(initialValue) }
