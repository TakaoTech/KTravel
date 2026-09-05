package com.takaotech.ktravel.presentation.plan.transport

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
data class TransportPlanningNavState(val eventSink: (TransportPlanningEvent) -> Unit) : CircuitUiState

/** What the leg composer can ask for. */
sealed interface TransportPlanningEvent : CircuitUiEvent {
    /** The traveller left without computing anything. */
    data object Back : TransportPlanningEvent

    /** A calculation succeeded, and its alternatives are what should be on screen. */
    data object ShowPreview : TransportPlanningEvent
}
