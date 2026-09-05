package com.takaotech.ktravel.ui.plan.day.placestep

import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import com.takaotech.ktravel.ui.plan.day.notes.StepNotesTestTags

internal object PlaceStepTestTags {
    const val MAP = "step_detail_map"
    const val BACK_BUTTON = "step_detail_back"
    const val START_TIME_FIELD = "step_detail_start_time"
    const val END_TIME_FIELD = "step_detail_end_time"

    val NOTES = StepNotesTestTags(
        noteView = "step_detail_note_view",
        noteEditor = "step_detail_note_editor",
        editToggle = "step_detail_edit_toggle",
        noteAlert = "step_detail_note_alert",
        missingReferences = "step_detail_missing_references",
        attachments = "step_detail_attachments",
        attachmentAdd = "step_detail_attachment_add",
        attachmentItem = "step_detail_attachment_item",
    )
}
