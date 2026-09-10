package com.takaotech.ktravel.presentation.intro

import com.slack.circuit.runtime.CircuitUiEvent
import com.slack.circuit.runtime.CircuitUiState
import com.slack.circuit.runtime.screen.Screen
import com.slack.circuit.serialization.CircuitSerializable
import com.takaotech.ktravel.core.telemetry.TelemetryConsent
import com.takaotech.ktravel.di.AppScope
import com.takaotech.ktravel.domain.staticflows.IntroRequirement
import com.takaotech.ktravel.domain.staticflows.IntroStep
import kotlinx.collections.immutable.ImmutableList
import kotlinx.serialization.Serializable

/**
 * The introduction to the application, shown before anything else when it is due.
 *
 * Navigation decides that before it picks the first screen — see `KTravelNavigation` — and says how
 * much of it is due in [requirement].
 *
 * @property requirement How much of the introduction to show: everything on a first run, only the
 *   privacy page and the question when it is the policy that changed or the answer that expired.
 * @property fromStart True when the introduction is the first screen of the application, which is
 *   what says where answering leads: onto the trip list, with nothing behind it. False when it was
 *   opened again from elsewhere, where answering goes back to where the user was.
 */
@Serializable
@CircuitSerializable(AppScope::class)
data class IntroFlowScreen(val requirement: IntroRequirement = IntroRequirement.Full, val fromStart: Boolean = true) :
    Screen

/**
 * What the introduction draws.
 *
 * @property steps The steps to go through, in order; empty while the packaged files are still being
 *   read. Already filtered for [IntroFlowScreen.requirement].
 * @property eventSink Where what the user does goes.
 */
data class IntroUiState(val steps: ImmutableList<IntroStep>, val eventSink: (IntroEvent) -> Unit) : CircuitUiState

/** What the introduction can report. */
sealed interface IntroEvent : CircuitUiEvent {

    /**
     * The user answered the telemetry question.
     *
     * @property consent What they chose.
     */
    data class Answered(val consent: TelemetryConsent) : IntroEvent

    /**
     * The user asked to read the policy: about one of the points, or in full.
     *
     * @property policyRef The path of the section the document opens on, e.g. `collection.provided`;
     *   null opens it at the top, which is what the link under the acknowledgement asks for.
     */
    data class PolicyOpened(val policyRef: String? = null) : IntroEvent
}
