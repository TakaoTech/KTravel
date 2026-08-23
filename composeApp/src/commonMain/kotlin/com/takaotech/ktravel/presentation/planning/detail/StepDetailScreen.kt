package com.takaotech.ktravel.presentation.planning.detail

import com.slack.circuit.runtime.CircuitUiEvent
import com.slack.circuit.runtime.CircuitUiState
import com.slack.circuit.runtime.screen.Screen
import com.takaotech.ktravel.core.annotation.Parcelize
import com.takaotech.ktravel.presentation.planning.StepUi
import kotlinx.datetime.LocalTime

/**
 * Schermata Circuit di dettaglio di un singolo step.
 *
 * In questa iterazione è sviluppata solo la variante [StepUi.Place] (nome, mappa con marker e note
 * in Markdown). La grafica potrà differire per gli step di trasporto in futuro.
 *
 * Il [travelId] determina il [com.takaotech.ktravel.di.PlanningGraph] da cui risolvere il
 * repository (vincolo V3); [dayId] individua il giorno e [stepId] lo step mostrato.
 */
@Parcelize
data class StepDetailScreen(val travelId: String, val dayId: String, val stepId: String) : Screen

data class StepDetailUiState(
    /** Step Place mostrato; `null` finché il flow non emette o se lo step non è un Place. */
    val place: StepUi.Place?,
    /** Note in Markdown e inventario file dello step, gestiti dal blocco condiviso. */
    val notes: StepNotesUiState,
    val eventSink: (StepDetailEvent) -> Unit,
) : CircuitUiState

sealed interface StepDetailEvent : CircuitUiEvent {
    data object NavigateBack : StepDetailEvent

    /** Imposta l'orario di inizio dello step. */
    data class SetStartTime(val time: LocalTime) : StepDetailEvent

    /** Imposta l'orario di fine dello step. */
    data class SetEndTime(val time: LocalTime) : StepDetailEvent

    /** Azione sulle note o sull'inventario, inoltrata al blocco condiviso. */
    data class Notes(val event: StepNotesEvent) : StepDetailEvent
}
