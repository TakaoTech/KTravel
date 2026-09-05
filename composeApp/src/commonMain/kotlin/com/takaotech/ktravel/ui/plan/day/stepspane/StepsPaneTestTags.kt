package com.takaotech.ktravel.ui.plan.day.stepspane

internal object StepsPaneTestTags {
    const val LIST = "steps_pane_list"
    const val EMPTY = "steps_pane_empty"
    const val BACK_BUTTON = "steps_pane_back"
    const val TITLE = "steps_pane_title"
    const val OPEN_BACKLOG_BUTTON = "steps_pane_open_backlog"
    const val TRANSPORT_DURATION = "steps_pane_transport_duration"
    fun addTransportTag(startPlaceId: String, endPlaceId: String) =
        "steps_pane_add_transport_${startPlaceId}_$endPlaceId"

    fun deleteStepTag(stepId: String) = "steps_pane_delete_step_$stepId"
    fun arrivalChipTag(stepId: String) = "steps_pane_arrival_chip_$stepId"
    fun departureChipTag(stepId: String) = "steps_pane_departure_chip_$stepId"
    fun moveStepUpTag(stepId: String) = "steps_pane_move_step_up_$stepId"
    fun moveStepDownTag(stepId: String) = "steps_pane_move_step_down_$stepId"
}
