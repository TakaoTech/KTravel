@file:OptIn(ExperimentalTime::class)

package com.takaotech.ktravel.presentation.planning.detail

import androidx.compose.runtime.Immutable
import com.slack.circuit.runtime.CircuitUiEvent
import com.slack.circuit.runtime.CircuitUiState
import com.slack.circuit.runtime.screen.Screen
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
 * Schermata Circuit di dettaglio di un trasporto: la tratta calcolata fra due tappe.
 *
 * Il [travelId] determina il [com.takaotech.ktravel.di.PlanningGraph] da cui risolvere il
 * repository (vincolo V3); [dayId] individua il giorno e [stepId] il trasporto mostrato.
 */
@Serializable
data class TransportDetailScreen(val travelId: String, val dayId: String, val stepId: String) : Screen

/**
 * La tratta, pronta da disegnare.
 *
 * Le due tappe sono nomi e non id perché la testata mostra quelli, e il presenter è l'unico punto
 * che sa risolverli: la posizione nell'itinerario *è* l'identità di un trasporto.
 */
@Immutable
data class TransportDetailUi(
    val type: TransportType,
    val fromName: String,
    val toName: String,
    /**
     * La risposta salvata, nella forma in cui è stata data.
     *
     * È anche il discriminante fra le due liste di passi — manovre o timeline — e lo è per tipo:
     * il `when` è esaustivo e il compilatore non lascia aggiungere una terza forma senza dirle come
     * disegnarsi.
     */
    val answer: TransportAnswer,
    val totalDuration: Duration,
    val totalDistance: Measure<Length>,
    /** Partenza dalla prima tappa, null quando il calcolo non ha prodotto orari. */
    val departure: LocalDateTime? = null,
    /** Arrivo alla seconda tappa, null quando il calcolo non ha prodotto orari. */
    val arrival: LocalDateTime? = null,
    /** Quando la tratta è stata calcolata, null per i trasporti salvati prima che venisse annotato. */
    val calculatedAt: Instant? = null,
)

data class TransportDetailUiState(
    /** Trasporto mostrato; `null` finché il flow non emette o se lo step non è un trasporto. */
    val transport: TransportDetailUi?,
    /** Note in Markdown e inventario file della tratta, gestiti dal blocco condiviso. */
    val notes: StepNotesUiState,
    /**
     * True quando il ricalcolo è possibile, cioè quando il trasporto ha ancora una tappa prima e
     * una dopo: senza le due estremità non c'è nulla da ricalcolare.
     */
    val canRecalculate: Boolean,
    val eventSink: (TransportDetailEvent) -> Unit,
) : CircuitUiState

sealed interface TransportDetailEvent : CircuitUiEvent {
    data object NavigateBack : TransportDetailEvent

    /** Riapre il flusso di calcolo sulle due tappe che la tratta collega. */
    data object Recalculate : TransportDetailEvent

    /** Azione sulle note o sull'inventario, inoltrata al blocco condiviso. */
    data class Notes(val event: StepNotesEvent) : TransportDetailEvent
}
