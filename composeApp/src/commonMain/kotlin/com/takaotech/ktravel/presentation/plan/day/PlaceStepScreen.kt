package com.takaotech.ktravel.presentation.plan.day

import com.slack.circuit.runtime.CircuitUiEvent
import com.slack.circuit.runtime.CircuitUiState
import com.slack.circuit.runtime.screen.Screen
import com.slack.circuit.serialization.CircuitSerializable
import com.takaotech.ktravel.di.AppScope
import com.takaotech.ktravel.presentation.plan.StepUi
import kotlinx.datetime.LocalTime
import kotlinx.serialization.Serializable

/**
 * One place of an itinerary: where it is, when it is visited, and what is written about it.
 *
 * Only the [StepUi.Place] form is drawn here; a leg is a different enough thing to show that it has
 * a screen of its own.
 *
 * [travelId] is what the repository is resolved from, since each trip has an object graph of its
 * own; [dayId] and [stepId] pick the step within it.
 */
@Serializable
@CircuitSerializable(AppScope::class)
data class PlaceStepScreen(val travelId: String, val dayId: String, val stepId: String) : Screen

data class PlaceStepUiState(
    /** The place being shown, `null` while the plan has not been read or the step is not one. */
    val place: StepUi.Place?,
    /** The Markdown notes and the files attached to them, shared with the leg screen. */
    val notes: StepNotesUiState,
    val eventSink: (PlaceStepEvent) -> Unit,
) : CircuitUiState

sealed interface PlaceStepEvent : CircuitUiEvent {
    data object NavigateBack : PlaceStepEvent

    /** Sets when the visit begins. */
    data class SetStartTime(val time: LocalTime) : PlaceStepEvent

    /** Sets when the visit ends. */
    data class SetEndTime(val time: LocalTime) : PlaceStepEvent

    /** Something happened to the notes or their files; the shared block answers it. */
    data class Notes(val event: StepNotesEvent) : PlaceStepEvent
}
