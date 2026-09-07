package com.takaotech.ktravel.presentation.consent

import com.slack.circuit.runtime.CircuitUiEvent
import com.slack.circuit.runtime.CircuitUiState
import com.slack.circuit.runtime.screen.Screen
import com.slack.circuit.serialization.CircuitSerializable
import com.takaotech.ktravel.core.telemetry.TelemetryConsent
import com.takaotech.ktravel.di.AppScope
import com.takaotech.ktravel.domain.staticflows.ConsentCard
import kotlinx.collections.immutable.ImmutableList
import kotlinx.serialization.Serializable

/**
 * The privacy notice, shown before anything else when it is due.
 *
 * Due means: never answered, answered to an older version of the notice, or answered more than a
 * year ago. Navigation decides that before it picks the first screen — see `KTravelNavigation`.
 *
 * @property fromStart True when the notice is the first screen of the application, which is what says
 *   where answering it leads: onto the trip list, with nothing behind it. False when it was opened
 *   again from the diagnostics screen, where answering it goes back to where the user was.
 */
@Serializable
@CircuitSerializable(AppScope::class)
data class PrivacyPolicyScreen(val fromStart: Boolean = true) : Screen

/**
 * What the notice draws.
 *
 * @property cards The cards to read, in order; empty while the packaged file is still being read.
 * @property eventSink Where the answer goes.
 */
data class ConsentUiState(val cards: ImmutableList<ConsentCard>, val eventSink: (ConsentEvent) -> Unit) :
    CircuitUiState

/** What the notice can report. */
sealed interface ConsentEvent : CircuitUiEvent {

    /**
     * The user answered.
     *
     * @property consent What they chose.
     */
    data class Answered(val consent: TelemetryConsent) : ConsentEvent
}
