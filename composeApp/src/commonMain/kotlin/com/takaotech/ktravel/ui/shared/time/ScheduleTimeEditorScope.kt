package com.takaotech.ktravel.ui.shared.time

import androidx.compose.runtime.Stable

/**
 * Presentation-agnostic handle exposed by [ScheduleTimeEditor] to its trigger [content]: the
 * display strings (formatted time or `--:--` placeholder), the accessibility descriptions, and the
 * actions that open each Material3 time picker. Callers decide how the two triggers look.
 */
@Stable
internal class ScheduleTimeEditorScope(
    val startDisplay: String,
    val endDisplay: String,
    val startContentDescription: String,
    val endContentDescription: String,
    val openStartPicker: () -> Unit,
    val openEndPicker: () -> Unit,
)
