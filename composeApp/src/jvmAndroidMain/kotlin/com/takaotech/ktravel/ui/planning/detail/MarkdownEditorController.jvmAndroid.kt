package com.takaotech.ktravel.ui.planning.detail

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import com.denser.hyphen.state.HyphenTextState
import com.denser.hyphen.state.markdownFlow
import com.denser.hyphen.state.rememberHyphenTextState
import kotlinx.coroutines.flow.Flow

/**
 * Actual Android/Desktop: incapsula lo [HyphenTextState] dell'editor WYSIWYG. L'inserimento al
 * cursore usa `HyphenTextState.insertText`, che applica anche i controlli Markdown/trigger.
 */
@Stable
actual class MarkdownEditorController internal constructor(internal val state: HyphenTextState) {
    actual val markdownFlow: Flow<String> = state.markdownFlow

    actual fun insertAtCursor(text: String) {
        state.insertText(text)
    }
}

@Composable
actual fun rememberMarkdownEditorController(initialValue: String): MarkdownEditorController {
    val state = rememberHyphenTextState(initialText = initialValue)
    return remember(state) { MarkdownEditorController(state) }
}
