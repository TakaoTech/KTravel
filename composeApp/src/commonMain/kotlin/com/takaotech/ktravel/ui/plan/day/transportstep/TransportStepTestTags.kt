package com.takaotech.ktravel.ui.plan.day.transportstep

import com.takaotech.ktravel.ui.plan.day.notes.StepNotesTestTags

internal object TransportStepTestTags {
    const val MAP = "transport_detail_map"
    const val BACK_BUTTON = "transport_detail_back"
    const val RECALCULATE = "transport_detail_recalculate"
    const val SEGMENT_HEAD = "transport_detail_segment"
    const val METRICS = "transport_detail_metrics"
    const val CALCULATED_AT = "transport_detail_calculated_at"
    const val STEPS_TOGGLE = "transport_detail_steps_toggle"
    const val MANOEUVRES = "transport_detail_manoeuvres"
    const val TRANSIT_TIMELINE = "transport_detail_transit_timeline"

    val NOTES = StepNotesTestTags(
        noteView = "transport_detail_note_view",
        noteEditor = "transport_detail_note_editor",
        editToggle = "transport_detail_edit_toggle",
        noteAlert = "transport_detail_note_alert",
        missingReferences = "transport_detail_missing_references",
        attachments = "transport_detail_attachments",
        attachmentAdd = "transport_detail_attachment_add",
        attachmentItem = "transport_detail_attachment_item",
    )
}
