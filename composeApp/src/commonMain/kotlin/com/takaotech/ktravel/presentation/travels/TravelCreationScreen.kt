package com.takaotech.ktravel.presentation.travels

import com.slack.circuit.runtime.CircuitUiEvent
import com.slack.circuit.runtime.CircuitUiState
import com.slack.circuit.runtime.screen.Screen
import com.slack.circuit.serialization.CircuitSerializable
import com.takaotech.ktravel.di.AppScope
import kotlinx.serialization.Serializable

/** The form that makes a new trip. */
@Serializable
@CircuitSerializable(AppScope::class)
data object TravelCreationScreen : Screen

/**
 * Everything the presenter owns for this screen, which for now is only where it can go next.
 *
 * The screen's own state — what it shows and what the user is editing — still lives in its view
 * model, and the UI reads it from there. Once that view model becomes the presenter, its state
 * replaces this type and the two halves are one again.
 *
 * @property eventSink Where the screen sends what the user did.
 */
data class TravelCreationNavState(val eventSink: (TravelCreationEvent) -> Unit) : CircuitUiState

/** What the creation form can ask for. */
sealed interface TravelCreationEvent : CircuitUiEvent {
    /** The traveller gave up on creating a trip. */
    data object Back : TravelCreationEvent

    /**
     * The trip was created and is the one to open.
     *
     * @property travelId The trip that was just created.
     */
    data class Created(val travelId: String) : TravelCreationEvent
}
