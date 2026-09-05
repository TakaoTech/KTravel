package com.takaotech.ktravel.ui.plan.day.notes

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import kotlinx.coroutines.flow.Flow

/**
 * A handle on the Markdown editor, held by whoever needs to write into it from outside.
 *
 * The file inventory uses it to drop a reference where the caret is, which it can do without
 * knowing which editor is underneath — the two platforms do not share one.
 */
@Stable
expect class MarkdownEditorController {
    /** The Markdown as it stands, emitted again on every edit. */
    val markdownFlow: Flow<String>

    /** Writes [text] where the caret is. */
    fun insertAtCursor(text: String)
}

/** Remembers a controller for an editor starting from [initialValue]. */
@Composable
expect fun rememberMarkdownEditorController(initialValue: String): MarkdownEditorController
