package com.takaotech.ktravel.presentation.plan.day

import com.slack.circuit.foundation.NavEvent
import com.slack.circuit.runtime.CircuitUiEvent
import com.slack.circuit.runtime.CircuitUiState
import com.slack.circuit.runtime.screen.Screen
import com.slack.circuit.serialization.CircuitSerializable
import com.takaotech.ktravel.di.AppScope
import kotlinx.serialization.Serializable

/**
 * One day of a trip: the itinerary and the places not yet placed in it.
 *
 * Coordinates two panes rather than drawing anything itself — [StepsPaneScreen] and
 * [PlacesBacklogScreen], mounted as nested `CircuitContent` by the adaptive layout, which decides
 * whether they sit side by side or one at a time.
 *
 * [travelId] is what the repository is resolved from, since each trip has an object graph of its
 * own; [dayId] picks the day within it.
 */
@Serializable
@CircuitSerializable(AppScope::class)
data class DayDetailScreen(val travelId: String, val dayId: String) : Screen

/** Adding a place, either to a day or to the trip's backlog when no day is given. */
@Serializable
@CircuitSerializable(AppScope::class)
data class AddPlaceScreen(val travelId: String, val dayId: String? = null) : Screen

@Serializable
@CircuitSerializable(AppScope::class)
data class AddTransportScreen(
    val travelId: String,
    val dayId: String,
    val startPlaceId: String,
    val endPlaceId: String,
) : Screen

data class DayDetailUiState(
    val stepsPaneScreen: StepsPaneScreen,
    val placesBacklogScreen: PlacesBacklogScreen,
    val eventSink: (DayDetailEvent) -> Unit,
) : CircuitUiState

sealed interface DayDetailEvent : CircuitUiEvent {
    /**
     * A navigation event a child pane raised and the layout did not consume.
     *
     * The layout answers the events that only move between panes; anything that leaves the day
     * travels up here, so a child never needs a navigator of its own.
     */
    data class ChildNav(val navEvent: NavEvent) : DayDetailEvent
}
