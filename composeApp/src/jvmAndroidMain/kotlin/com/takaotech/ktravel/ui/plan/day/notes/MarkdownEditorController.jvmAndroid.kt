package com.takaotech.ktravel.ui.plan.day.notes

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import com.denser.hyphen.state.HyphenTextState
import com.denser.hyphen.state.markdownFlow
import com.denser.hyphen.state.rememberHyphenTextState
import kotlinx.coroutines.flow.Flow

/**
 * On Android and desktop the handle wraps the WYSIWYG editor's own state.
 *
 * Writing at the caret goes through `HyphenTextState.insertText` rather than editing the text
 * directly, so an inserted reference passes the same Markdown handling as anything typed.
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
