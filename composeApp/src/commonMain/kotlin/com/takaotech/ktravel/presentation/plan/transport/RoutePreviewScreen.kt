package com.takaotech.ktravel.presentation.plan.transport

import com.slack.circuit.runtime.CircuitUiEvent
import com.slack.circuit.runtime.CircuitUiState
import com.slack.circuit.runtime.screen.Screen
import com.slack.circuit.serialization.CircuitSerializable
import com.takaotech.ktravel.di.AppScope
import kotlinx.serialization.Serializable

/**
 * The itineraries the composer computed, for the traveller to choose one.
 *
 * Carries the same four identifiers as the composer rather than a handle to its answer: the answer
 * lives in the trip's graph ([RouteAnswerDraft]), and a screen has to be reconstructible from what
 * the back stack saved of it.
 *
 * @property travelId The trip the leg belongs to.
 * @property dayId The day the leg belongs to.
 * @property startPlaceId The place the leg starts from.
 * @property endPlaceId The place the leg ends at.
 */
@Serializable
@CircuitSerializable(AppScope::class)
data class RoutePreviewScreen(
    val travelId: String,
    val dayId: String,
    val startPlaceId: String,
    val endPlaceId: String,
) : Screen

/**
 * Everything the presenter owns for this screen, which for now is only where it can go next.
 *
 * The screen's own state — what it shows and what the user is editing — still lives in its view
 * model, and the UI reads it from there. Once that view model becomes the presenter, its state
 * replaces this type and the two halves are one again.
 *
 * @property eventSink Where the screen sends what the user did.
 */
data class RoutePreviewNavState(val eventSink: (RoutePreviewEvent) -> Unit) : CircuitUiState

/** What the route preview can ask for. */
sealed interface RoutePreviewEvent : CircuitUiEvent {
    /** The traveller left without filing anything. */
    data object Back : RoutePreviewEvent

    /**
     * The chosen alternative was filed, and the leg it produced is what to open.
     *
     * @property stepId The leg that was just filed.
     */
    data class Saved(val stepId: String) : RoutePreviewEvent
}
