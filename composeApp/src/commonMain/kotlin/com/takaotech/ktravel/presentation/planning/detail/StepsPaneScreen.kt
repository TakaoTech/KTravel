package com.takaotech.ktravel.presentation.planning.detail

import com.slack.circuit.runtime.CircuitUiEvent
import com.slack.circuit.runtime.CircuitUiState
import com.slack.circuit.runtime.screen.Screen
import com.takaotech.ktravel.core.annotation.Parcelize
import com.takaotech.ktravel.presentation.planning.StepUi
import kotlinx.collections.immutable.ImmutableList
import kotlinx.datetime.LocalTime

/**
 * Pannello itinerario del dettaglio giorno: lista ordinata di step (posti e trasporti) con slot
 * "aggiungi trasporto" pre-calcolati dal presenter.
 *
 * Il [travelId] determina il [com.takaotech.ktravel.di.PlanningGraph] da cui risolvere il
 * repository (vincolo V3); il [dayId] individua il giorno mostrato.
 */
@Parcelize
data class StepsPaneScreen(val travelId: String, val dayId: String) : Screen

data class StepsPaneUiState(val rows: ImmutableList<StepRow>, val eventSink: (StepsPaneEvent) -> Unit) : CircuitUiState

sealed interface StepsPaneEvent : CircuitUiEvent {
    /** Torna alla pagina di pianificazione del viaggio. */
    data object NavigateBack : StepsPaneEvent

    /** Mostra il pannello backlog dei posti (gestito dal layout della pagina padre). */
    data object OpenBacklog : StepsPaneEvent

    /** Apre la schermata di dettaglio dello step indicato. */
    data class OpenStepDetail(val stepId: String) : StepsPaneEvent

    /** Rimuove uno step (la decisione Place->backlog / Transport->delete è del dominio). */
    data class DeleteStep(val step: StepUi) : StepsPaneEvent

    data class MoveStepUp(val stepId: String) : StepsPaneEvent
    data class MoveStepDown(val stepId: String) : StepsPaneEvent

    /** Avvia il flusso di aggiunta trasporto tra due step consecutivi. */
    data class AddTransport(val startPlaceId: String, val endPlaceId: String) : StepsPaneEvent

    /** Imposta l'orario di inizio dello step luogo indicato. */
    data class SetStartTime(val stepId: String, val time: LocalTime) : StepsPaneEvent

    /** Imposta l'orario di fine dello step luogo indicato. */
    data class SetEndTime(val stepId: String, val time: LocalTime) : StepsPaneEvent
}
