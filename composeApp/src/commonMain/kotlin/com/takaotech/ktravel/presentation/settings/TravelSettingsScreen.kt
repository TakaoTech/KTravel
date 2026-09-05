package com.takaotech.ktravel.presentation.settings

import com.slack.circuit.runtime.CircuitUiEvent
import com.slack.circuit.runtime.CircuitUiState
import com.slack.circuit.runtime.screen.Screen
import com.slack.circuit.serialization.CircuitSerializable
import com.takaotech.ktravel.di.AppScope
import kotlinx.serialization.Serializable

/**
 * The settings of one trip: its routing key, and which navigator it plans with.
 *
 * @property travelId The trip these settings belong to.
 */
@Serializable
@CircuitSerializable(AppScope::class)
data class TravelSettingsScreen(val travelId: String) : Screen

/**
 * Everything the presenter owns for this screen, which for now is only where it can go next.
 *
 * The screen's own state — what it shows and what the user is editing — still lives in its view
 * model, and the UI reads it from there. Once that view model becomes the presenter, its state
 * replaces this type and the two halves are one again.
 *
 * @property eventSink Where the screen sends what the user did.
 */
data class TravelSettingsNavState(val eventSink: (TravelSettingsEvent) -> Unit) : CircuitUiState

/** What the trip settings screen can ask for. */
sealed interface TravelSettingsEvent : CircuitUiEvent {
    /** The user is done. */
    data object Back : TravelSettingsEvent
}
