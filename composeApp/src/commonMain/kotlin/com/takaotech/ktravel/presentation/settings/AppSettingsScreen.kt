package com.takaotech.ktravel.presentation.settings

import com.slack.circuit.runtime.CircuitUiEvent
import com.slack.circuit.runtime.CircuitUiState
import com.slack.circuit.runtime.screen.Screen
import com.slack.circuit.serialization.CircuitSerializable
import com.takaotech.ktravel.di.AppScope
import kotlinx.serialization.Serializable

/** The settings of the installation, which outlive every trip. */
@Serializable
@CircuitSerializable(AppScope::class)
data object AppSettingsScreen : Screen

/**
 * Everything the presenter owns for this screen, which for now is only where it can go next.
 *
 * The screen's own state — what it shows and what the user is editing — still lives in its view
 * model, and the UI reads it from there. Once that view model becomes the presenter, its state
 * replaces this type and the two halves are one again.
 *
 * @property eventSink Where the screen sends what the user did.
 */
data class AppSettingsNavState(val eventSink: (AppSettingsEvent) -> Unit) : CircuitUiState

/** What the app settings screen can ask for. */
sealed interface AppSettingsEvent : CircuitUiEvent {
    /** The user is done. */
    data object Back : AppSettingsEvent

    /** The user asked to read the dependency licenses. */
    data object OpenLicenses : AppSettingsEvent

    /** The user asked to read the log this installation kept. */
    data object OpenDiagnostics : AppSettingsEvent

    /** Read the privacy notice again, and answer it again. */
    data object PrivacyPolicy : AppSettingsEvent
}
