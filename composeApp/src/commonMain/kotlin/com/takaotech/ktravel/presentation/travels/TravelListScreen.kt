package com.takaotech.ktravel.presentation.travels

import com.slack.circuit.runtime.CircuitUiEvent
import com.slack.circuit.runtime.CircuitUiState
import com.slack.circuit.runtime.screen.Screen
import com.slack.circuit.serialization.CircuitSerializable
import com.takaotech.ktravel.di.AppScope
import kotlinx.serialization.Serializable

/** The trips the traveller has, and the way into any of them. */
@Serializable
@CircuitSerializable(AppScope::class)
data object TravelListScreen : Screen

/**
 * Everything the presenter owns for this screen, which for now is only where it can go next.
 *
 * The screen's own state — what it shows and what the user is editing — still lives in its view
 * model, and the UI reads it from there. Once that view model becomes the presenter, its state
 * replaces this type and the two halves are one again.
 *
 * @property eventSink Where the screen sends what the user did.
 */
data class TravelListNavState(val eventSink: (TravelListEvent) -> Unit) : CircuitUiState

/** What the travel list can ask for. */
sealed interface TravelListEvent : CircuitUiEvent {
    /**
     * A trip was opened.
     *
     * @property travelId The trip to open.
     */
    data class OpenTravel(val travelId: String) : TravelListEvent

    /** The traveller wants a new trip. */
    data object NewTravel : TravelListEvent

    /** The traveller wants the settings of the installation. */
    data object OpenAppSettings : TravelListEvent
}
