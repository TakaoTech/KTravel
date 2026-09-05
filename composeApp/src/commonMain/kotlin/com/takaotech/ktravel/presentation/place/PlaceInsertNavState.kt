package com.takaotech.ktravel.presentation.place

import com.slack.circuit.runtime.CircuitUiEvent
import com.slack.circuit.runtime.CircuitUiState

/**
 * Everything the presenter owns for this screen, which for now is only where it can go next.
 *
 * The screen's own state — what it shows and what the user is editing — still lives in its view
 * model, and the UI reads it from there. Once that view model becomes the presenter, its state
 * replaces this type and the two halves are one again.
 *
 * @property eventSink Where the screen sends what the user did.
 */
data class PlaceInsertNavState(val eventSink: (PlaceInsertEvent) -> Unit) : CircuitUiState

/** What the place form can ask for. */
sealed interface PlaceInsertEvent : CircuitUiEvent {
    /** The traveller left without adding a place. */
    data object Exit : PlaceInsertEvent

    /** The place was added. */
    data object Saved : PlaceInsertEvent
}
