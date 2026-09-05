package com.takaotech.ktravel.presentation.plan.day

import com.slack.circuit.runtime.CircuitUiEvent
import com.slack.circuit.runtime.CircuitUiState
import com.slack.circuit.runtime.screen.Screen
import com.slack.circuit.serialization.CircuitSerializable
import com.takaotech.ktravel.di.AppScope
import com.takaotech.ktravel.presentation.plan.PlaceUi
import kotlinx.collections.immutable.ImmutableList
import kotlinx.serialization.Serializable

/**
 * The places kept aside for a trip, waiting to be given a day.
 *
 * A place lives here until it is put into an itinerary, and comes back here rather than being lost
 * when it is taken out of one — deleting it for good is a separate action, and asks first.
 *
 * [travelId] is what the repository is resolved from, since each trip has an object graph of its
 * own; [dayId] picks the day places are moved into.
 */
@Serializable
@CircuitSerializable(AppScope::class)
data class PlacesBacklogScreen(val travelId: String, val dayId: String) : Screen

data class PlacesBacklogUiState(
    val places: ImmutableList<PlaceUi>,
    /** The place whose permanent deletion is waiting to be confirmed, or `null` while none is. */
    val pendingPermanentDelete: PlaceUi?,
    val eventSink: (PlacesBacklogEvent) -> Unit,
) : CircuitUiState

sealed interface PlacesBacklogEvent : CircuitUiEvent {
    /** Dismisses the backlog, which the day's layout decides how to honour. */
    data object Close : PlacesBacklogEvent

    /** Starts adding a place that is not in the trip yet. */
    data object AddPlace : PlacesBacklogEvent

    /** Moves a place out of the backlog and into the day's itinerary. */
    data class MovePlaceToSteps(val placeId: String) : PlacesBacklogEvent

    /** Takes a place back out of the itinerary, returning it to the trip's backlog. */
    data class MovePlaceToBacklog(val placeId: String) : PlacesBacklogEvent

    /** Asks to delete a place for good, which is confirmed before it happens. */
    data class PermanentDeleteRequested(val placeId: String) : PlacesBacklogEvent

    /** Confirms the deletion that was asked for. */
    data object PermanentDeleteConfirmed : PlacesBacklogEvent

    /** Abandons the deletion that was asked for. */
    data object PermanentDeleteDismissed : PlacesBacklogEvent
}
