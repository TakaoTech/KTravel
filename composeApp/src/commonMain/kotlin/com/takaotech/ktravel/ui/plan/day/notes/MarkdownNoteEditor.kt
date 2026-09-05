package com.takaotech.ktravel.ui.plan.day.notes

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * Edits the Markdown note of a step, driven by a shared [MarkdownEditorController].
 *
 * Android and desktop get a WYSIWYG editor with a formatting toolbar; iOS, where that editor is not
 * available, gets a text field over the raw Markdown instead. What the user has written is read
 * through [MarkdownEditorController.markdownFlow], and the starting text is given to the controller
 * when it is created rather than to this composable.
 */
@Composable
expect fun MarkdownNoteEditor(controller: MarkdownEditorController, label: String, modifier: Modifier = Modifier)
