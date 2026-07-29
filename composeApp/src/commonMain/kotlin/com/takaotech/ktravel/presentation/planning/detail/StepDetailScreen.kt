package com.takaotech.ktravel.presentation.planning.detail

import com.slack.circuit.runtime.CircuitUiEvent
import com.slack.circuit.runtime.CircuitUiState
import com.slack.circuit.runtime.screen.Screen
import com.takaotech.ktravel.core.annotation.Parcelize
import com.takaotech.ktravel.presentation.planning.StepUi
import io.github.vinceglb.filekit.PlatformFile
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
data class StepDetailScreen(
    val travelId: String,
    val dayId: String,
    val stepId: String
) : Screen

data class StepDetailUiState(
    /** Step Place mostrato; `null` finché il flow non emette o se lo step non è un Place. */
    val place: StepUi.Place?,
    /** True quando è attivo l'editor Markdown (Hyphen); false in sola lettura (rendering mikepenz). */
    val isEditing: Boolean,
    /** True quando l'ultimo tentativo di salvataggio è fallito perché il Markdown non è valido. */
    val noteInvalid: Boolean,
    /**
     * Path relativi referenziati nel Markdown ma non presenti nell'inventario dello step
     * (riferimenti "dangling"): vuoto = coerente. Da segnalare come errore.
     */
    val missingReferences: List<String>,
    /** Risolve un path relativo dell'inventario nel file assoluto, per rendering/apertura. */
    val resolveFile: (String) -> PlatformFile,
    val eventSink: (StepDetailEvent) -> Unit
) : CircuitUiState

sealed interface StepDetailEvent : CircuitUiEvent {
    data object NavigateBack : StepDetailEvent

    /** Passa da/verso la modalità modifica delle note. */
    data class ToggleEdit(val editing: Boolean) : StepDetailEvent

    /** Nuovo contenuto Markdown delle note, salvato automaticamente se valido. */
    data class NoteChanged(val note: String) : StepDetailEvent

    /** Imposta l'orario di inizio dello step. */
    data class SetStartTime(val time: LocalTime) : StepDetailEvent

    /** Imposta l'orario di fine dello step. */
    data class SetEndTime(val time: LocalTime) : StepDetailEvent

    /** Carica un file nell'inventario dello step. */
    data class AddAttachment(val file: PlatformFile) : StepDetailEvent

    /** Rimuove un file dall'inventario dello step (azione dedicata dell'inventario). */
    data class RemoveAttachment(val attachmentId: String) : StepDetailEvent
}
