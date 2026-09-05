package com.takaotech.ktravel.ui.plan.day.placesbacklog

internal object PlacesBacklogTestTags {
    const val LIST = "places_backlog_list"
    const val EMPTY = "places_backlog_empty"
    const val EMPTY_HINT = "places_backlog_empty_hint"
    const val CLOSE_BUTTON = "places_backlog_close"
    const val ADD_PLACE_BUTTON = "places_backlog_add_place"
    fun placeTag(id: String) = "places_backlog_place_$id"
    fun moveToStepsTag(id: String) = "places_backlog_move_to_steps_$id"
    fun noteBadgeTag(id: String) = "places_backlog_note_badge_$id"
    fun attachmentBadgeTag(id: String) = "places_backlog_attachment_badge_$id"
}
