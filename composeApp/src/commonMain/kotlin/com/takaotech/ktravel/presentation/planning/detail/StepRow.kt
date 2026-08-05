package com.takaotech.ktravel.presentation.planning.detail

import com.takaotech.ktravel.presentation.planning.StepUi
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toPersistentList

/**
 * Riga renderizzabile della lista steps: il presenter pre-calcola dove inserire gli slot
 * "aggiungi trasporto" così la UI resta un semplice renderer senza logica di adiacenza.
 *
 * [key] è stabile e univoca, pensata per `LazyColumn(items, key = { it.key })`.
 */
sealed interface StepRow {
    val key: String

    data class Step(val step: StepUi) : StepRow {
        override val key: String get() = step.id
    }

    /** Slot per aggiungere un trasporto tra lo step [startPlaceId] e lo step successivo [endPlaceId]. */
    data class AddTransportSlot(val startPlaceId: String, val endPlaceId: String) : StepRow {
        override val key: String get() = "add_transport_${startPlaceId}_$endPlaceId"
    }
}

/**
 * Costruisce le righe della lista steps inserendo uno [StepRow.AddTransportSlot] dopo ogni
 * [StepUi.Place] il cui successore non è un [StepUi.Transport].
 *
 * Nota: uno slot non viene mai inserito dopo un [StepUi.Transport], anche se seguito da un Place —
 * parità con il comportamento storico della UI; eventuale estensione è una scelta di prodotto.
 */
fun buildStepRows(steps: List<StepUi>): ImmutableList<StepRow> = buildList {
    steps.forEachIndexed { index, step ->
        add(StepRow.Step(step))

        val next = steps.getOrNull(index + 1)
        if (step is StepUi.Place && next != null && next !is StepUi.Transport) {
            add(StepRow.AddTransportSlot(startPlaceId = step.id, endPlaceId = next.id))
        }
    }
}.toPersistentList()
