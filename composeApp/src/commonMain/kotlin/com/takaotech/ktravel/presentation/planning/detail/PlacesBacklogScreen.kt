package com.takaotech.ktravel.presentation.planning.detail

import com.slack.circuit.runtime.CircuitUiEvent
import com.slack.circuit.runtime.CircuitUiState
import com.slack.circuit.runtime.screen.Screen
import com.takaotech.ktravel.core.annotation.Parcelize
import com.takaotech.ktravel.presentation.planning.PlaceUi
import kotlinx.collections.immutable.ImmutableList

/**
 * Pannello backlog dei posti del dettaglio giorno: posti non ancora inseriti nell'itinerario, con
 * azioni di aggiunta, spostamento negli step ed eliminazione definitiva (con conferma).
 *
 * Il [travelId] determina il [com.takaotech.ktravel.di.PlanningGraph] da cui risolvere il
 * repository (vincolo V3); il [dayId] individua il giorno mostrato.
 */
@Parcelize
data class PlacesBacklogScreen(val travelId: String, val dayId: String) : Screen

data class PlacesBacklogUiState(
    val places: ImmutableList<PlaceUi>,
    /** Posto in attesa di conferma di eliminazione definitiva; non-null => dialog visibile. */
    val pendingPermanentDelete: PlaceUi?,
    val eventSink: (PlacesBacklogEvent) -> Unit
) : CircuitUiState

sealed interface PlacesBacklogEvent : CircuitUiEvent {
    /** Chiude il pannello backlog (gestito dal layout della pagina padre). */
    data object Close : PlacesBacklogEvent

    /** Avvia il flusso di inserimento di un nuovo posto. */
    data object AddPlace : PlacesBacklogEvent

    /** Sposta un Place del backlog nella lista steps (movePlaceToStep). */
    data class MovePlaceToSteps(val placeId: String) : PlacesBacklogEvent

    /** Riporta un Place del giorno nel backlog generale (movePlaceToGeneral). */
    data class MovePlaceToBacklog(val placeId: String) : PlacesBacklogEvent

    /** Richiede l'eliminazione definitiva: mostra il dialog di conferma. */
    data class PermanentDeleteRequested(val placeId: String) : PlacesBacklogEvent

    /** Conferma l'eliminazione definitiva del posto in attesa (deletePlace). */
    data object PermanentDeleteConfirmed : PlacesBacklogEvent

    /** Annulla la richiesta di eliminazione definitiva. */
    data object PermanentDeleteDismissed : PlacesBacklogEvent
}
