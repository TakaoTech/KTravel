package com.takaotech.ktravel.presentation.plan.day

import com.slack.circuit.runtime.CircuitUiEvent
import com.slack.circuit.runtime.CircuitUiState
import com.slack.circuit.runtime.screen.Screen
import com.slack.circuit.serialization.CircuitSerializable
import com.takaotech.ktravel.di.AppScope
import com.takaotech.ktravel.presentation.plan.StepUi
import kotlinx.collections.immutable.ImmutableList
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.serialization.Serializable

/**
 * The itinerary of one day: its places and legs in order, with the gaps between them.
 *
 * The gaps are rows in their own right rather than a decoration of the row above, because each one
 * is somewhere a leg can be added and has to be reachable on its own.
 *
 * [travelId] is what the repository is resolved from, since each trip has an object graph of its
 * own; [dayId] picks the day within it.
 */
@Serializable
@CircuitSerializable(AppScope::class)
data class StepsPaneScreen(val travelId: String, val dayId: String) : Screen

/**
 * State of the itinerary pane: the rows and the [date] of the day being shown, `null` until the day
 * has been loaded (the toolbar stays without a title in that case).
 */
data class StepsPaneUiState(
    val rows: ImmutableList<StepRow>,
    val date: LocalDate? = null,
    val eventSink: (StepsPaneEvent) -> Unit,
) : CircuitUiState

sealed interface StepsPaneEvent : CircuitUiEvent {
    /** Leaves the day for the trip it belongs to. */
    data object NavigateBack : StepsPaneEvent

    /** Asks for the backlog pane, which the day's layout decides how to show. */
    data object OpenBacklog : StepsPaneEvent

    /** Opens the place the user tapped. */
    data class OpenStepDetail(val stepId: String) : StepsPaneEvent

    /** Opens the leg the user tapped. */
    data class OpenTransportDetail(val stepId: String) : StepsPaneEvent

    /** Removes a step. Whether that means returning a place to the backlog or deleting a leg is
     * the domain's decision, not the pane's. */
    data class DeleteStep(val step: StepUi) : StepsPaneEvent

    data class MoveStepUp(val stepId: String) : StepsPaneEvent
    data class MoveStepDown(val stepId: String) : StepsPaneEvent

    /** Starts composing the leg between two consecutive places. */
    data class AddTransport(val startPlaceId: String, val endPlaceId: String) : StepsPaneEvent

    /** Sets when the visit to a place begins. */
    data class SetStartTime(val stepId: String, val time: LocalTime) : StepsPaneEvent

    /** Sets when the visit to a place ends. */
    data class SetEndTime(val stepId: String, val time: LocalTime) : StepsPaneEvent
}
