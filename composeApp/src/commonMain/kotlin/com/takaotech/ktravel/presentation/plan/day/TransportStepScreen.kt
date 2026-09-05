@file:OptIn(ExperimentalTime::class)

package com.takaotech.ktravel.presentation.plan.day

import androidx.compose.runtime.Immutable
import com.slack.circuit.runtime.CircuitUiEvent
import com.slack.circuit.runtime.CircuitUiState
import com.slack.circuit.runtime.screen.Screen
import com.slack.circuit.serialization.CircuitSerializable
import com.takaotech.ktravel.di.AppScope
import com.takaotech.ktravel.domain.model.TransportType
import com.takaotech.ktravel.domain.routing.model.TransportAnswer
import io.nacular.measured.units.Length
import io.nacular.measured.units.Measure
import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Serializable
import kotlin.time.Duration
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

/**
 * One leg of an itinerary: the route that was computed and filed between two places.
 *
 * [travelId] is what the repository is resolved from, since each trip has an object graph of its
 * own; [dayId] and [stepId] pick the leg within it.
 */
@Serializable
@CircuitSerializable(AppScope::class)
data class TransportStepScreen(val travelId: String, val dayId: String, val stepId: String) : Screen

/**
 * The leg, ready to draw.
 *
 * Its two ends are names rather than identifiers because the header shows names, and the presenter
 * is the only place that can resolve them: a leg has no ends of its own — its position in the
 * itinerary is what gives it two.
 */
@Immutable
data class TransportStepUi(
    val type: TransportType,
    val fromName: String,
    val toName: String,
    /**
     * The answer as it was filed, in the shape it was given in.
     *
     * It is also what decides which list of steps is drawn — manoeuvres or a timeline — and it
     * decides by type: the `when` over it is exhaustive, so a third kind of answer cannot be added
     * without being told how to draw itself.
     */
    val answer: TransportAnswer,
    val totalDuration: Duration,
    val totalDistance: Measure<Length>,
    /** When the leg leaves its first end, `null` when the answer carried no times. */
    val departure: LocalDateTime? = null,
    /** When the leg reaches its second end, `null` when the answer carried no times. */
    val arrival: LocalDateTime? = null,
    /** When the leg was computed, `null` for legs filed before that was recorded. */
    val calculatedAt: Instant? = null,
)

data class TransportStepUiState(
    /** The leg being shown, `null` while the plan has not been read or the step is not one. */
    val transport: TransportStepUi?,
    /** The Markdown notes and the files attached to them, shared with the place screen. */
    val notes: StepNotesUiState,
    /**
     * Whether the leg can be computed again, which it can only while it still has a place before it
     * and one after: without both ends there is no question left to ask.
     */
    val canRecalculate: Boolean,
    val eventSink: (TransportStepEvent) -> Unit,
) : CircuitUiState

sealed interface TransportStepEvent : CircuitUiEvent {
    data object NavigateBack : TransportStepEvent

    /** Asks for the leg again, between the same two places. */
    data object Recalculate : TransportStepEvent

    /** Something happened to the notes or their files; the shared block answers it. */
    data class Notes(val event: StepNotesEvent) : TransportStepEvent
}
