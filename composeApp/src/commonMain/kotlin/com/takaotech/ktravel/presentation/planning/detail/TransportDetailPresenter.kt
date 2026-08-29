@file:OptIn(ExperimentalTime::class)

package com.takaotech.ktravel.presentation.planning.detail

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import com.slack.circuit.codegen.annotations.CircuitInject
import com.slack.circuit.runtime.Navigator
import com.takaotech.ktravel.data.datasource.AttachmentDataSource
import com.takaotech.ktravel.di.AppScope
import com.takaotech.ktravel.di.PlanningGraphStore
import com.takaotech.ktravel.presentation.planning.StepUi
import com.takaotech.ktravel.presentation.planning.TravelPlanUiMapper
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.flow.map
import kotlin.time.ExperimentalTime

/**
 * Presenter Circuit del dettaglio trasporto (function-based, registrato via Metro `@CircuitInject`).
 *
 * Osserva il flow del giorno e ne ricava sia il trasporto sia i nomi delle due tappe che collega:
 * un trasporto non porta i propri estremi, è la posizione nell'itinerario a definirli, e sono le
 * stesse due tappe su cui il flusso di calcolo riparte quando si chiede un ricalcolo.
 *
 * Note e inventario file sono delegati per intero a [rememberStepNotesEditing], condiviso con il
 * dettaglio della tappa.
 */
@CircuitInject(TransportDetailScreen::class, AppScope::class)
@Composable
fun TransportDetailPresenter(
    screen: TransportDetailScreen,
    navigator: Navigator,
    planningGraphStore: PlanningGraphStore,
    attachmentDataSource: AttachmentDataSource,
): TransportDetailUiState {
    val repository = remember(screen.travelId) {
        planningGraphStore.getOrCreate(screen.travelId).travelPlanRepository
    }

    val rowsFlow = remember(repository, screen.dayId) {
        repository.getTravelDayFlow(screen.dayId)
            .map { day -> buildStepRows(with(TravelPlanUiMapper) { day.toUiDay() }.steps) }
    }
    val rows by rowsFlow.collectAsState(initial = persistentListOf())

    val step = remember(rows, screen.stepId) {
        rows.filterIsInstance<StepRow.Step>()
            .map(StepRow.Step::step)
            .filterIsInstance<StepUi.Transport>()
            .firstOrNull { it.id == screen.stepId }
    }
    val neighbours = remember(rows, screen.stepId) { rows.transportNeighbours(screen.stepId) }
    val transport = remember(rows, step, neighbours) {
        step?.let { rows.toTransportDetail(it, neighbours) }
    }

    val notes = rememberStepNotesEditing(
        repository = repository,
        resolveFile = attachmentDataSource::resolveFile,
        dayId = screen.dayId,
        stepId = screen.stepId,
        savedNote = step?.note.orEmpty(),
        attachments = step?.attachments ?: persistentListOf(),
    )

    return TransportDetailUiState(
        transport = transport,
        notes = notes.state,
        canRecalculate = neighbours != null,
    ) { event ->
        when (event) {
            TransportDetailEvent.NavigateBack -> navigator.pop()

            TransportDetailEvent.Recalculate -> neighbours?.let { (startPlaceId, endPlaceId) ->
                navigator.goTo(AddTransportScreen(screen.dayId, startPlaceId, endPlaceId))
            }

            is TransportDetailEvent.Notes -> notes(event.event)
        }
    }
}

/**
 * Il trasporto con i nomi dei suoi estremi.
 *
 * Le tappe si leggono dalle righe già costruite: quando mancano — un trasporto in coda a un giorno
 * a cui è stata tolta una tappa — il nome resta vuoto e la schermata mostra la tratta comunque,
 * perché il percorso salvato è ancora buono anche se l'itinerario intorno non lo è più.
 */
private fun List<StepRow>.toTransportDetail(
    step: StepUi.Transport,
    neighbours: Pair<String, String>?,
): TransportDetailUi {
    val places = filterIsInstance<StepRow.Step>().map(StepRow.Step::step).filterIsInstance<StepUi.Place>()
    fun nameOf(id: String?) = places.firstOrNull { it.id == id }?.name.orEmpty()

    val answer = step.answer
    return TransportDetailUi(
        type = step.type,
        fromName = nameOf(neighbours?.first),
        toName = nameOf(neighbours?.second),
        answer = answer,
        totalDuration = step.totalDuration,
        totalDistance = answer.summary.distance,
        departure = answer.departureTime,
        arrival = answer.arrivalTime,
        calculatedAt = step.calculatedAt,
    )
}
