package com.takaotech.ktravel.presentation.settings

import com.slack.circuit.runtime.CircuitUiEvent
import com.slack.circuit.runtime.CircuitUiState
import com.slack.circuit.runtime.screen.Screen
import com.slack.circuit.serialization.CircuitSerializable
import com.takaotech.ktravel.di.AppScope
import kotlinx.serialization.Serializable

/** The licenses of the dependencies, reachable from the app settings. */
@Serializable
@CircuitSerializable(AppScope::class)
data object LicensesScreen : Screen

/**
 * Everything the presenter owns for this screen, which for now is only where it can go next.
 *
 * The screen's own state — what it shows and what the user is editing — still lives in its view
 * model, and the UI reads it from there. Once that view model becomes the presenter, its state
 * replaces this type and the two halves are one again.
 *
 * @property eventSink Where the screen sends what the user did.
 */
data class LicensesNavState(val eventSink: (LicensesEvent) -> Unit) : CircuitUiState

/** What the licenses screen can ask for. */
sealed interface LicensesEvent : CircuitUiEvent {
    /** The user is done reading. */
    data object Back : LicensesEvent
}
