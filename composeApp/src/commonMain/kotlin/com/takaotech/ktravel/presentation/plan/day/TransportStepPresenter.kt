@file:OptIn(ExperimentalTime::class)

package com.takaotech.ktravel.presentation.plan.day

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import com.slack.circuit.codegen.annotations.CircuitInject
import com.slack.circuit.runtime.Navigator
import com.takaotech.ktravel.data.datasource.AttachmentDataSource
import com.takaotech.ktravel.di.AppScope
import com.takaotech.ktravel.di.PlanningGraphStore
import com.takaotech.ktravel.presentation.plan.StepUi
import com.takaotech.ktravel.presentation.plan.TravelPlanUiMapper
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.flow.map
import kotlin.time.ExperimentalTime

/**
 * Watches the day and draws out of it both the leg and the names of the two places it joins.
 *
 * A leg does not carry its own ends: its position in the itinerary is what gives it two, and they
 * are the same two the calculation starts again from when the user asks for the leg to be
 * recomputed.
 *
 * The notes and the files attached to them are handed over whole to [rememberStepNotesEditing],
 * which the place screen uses too.
 */
@CircuitInject(TransportStepScreen::class, AppScope::class)
@Composable
fun TransportStepPresenter(
    screen: TransportStepScreen,
    navigator: Navigator,
    planningGraphStore: PlanningGraphStore,
    attachmentDataSource: AttachmentDataSource,
): TransportStepUiState {
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

    return TransportStepUiState(
        transport = transport,
        notes = notes.state,
        canRecalculate = neighbours != null,
    ) { event ->
        when (event) {
            TransportStepEvent.NavigateBack -> navigator.pop()

            TransportStepEvent.Recalculate -> neighbours?.let { (startPlaceId, endPlaceId) ->
                navigator.goTo(AddTransportScreen(screen.travelId, screen.dayId, startPlaceId, endPlaceId))
            }

            is TransportStepEvent.Notes -> notes(event.event)
        }
    }
}

/**
 * The leg, together with the names of the two places it joins.
 *
 * The places are read out of the rows that were already built. When one is missing — a leg left at
 * the end of a day a place was removed from — its name stays empty and the leg is drawn anyway: the
 * route that was filed is still a good route, even if the itinerary around it no longer is.
 */
private fun List<StepRow>.toTransportDetail(
    step: StepUi.Transport,
    neighbours: Pair<String, String>?,
): TransportStepUi {
    val places = filterIsInstance<StepRow.Step>().map(StepRow.Step::step).filterIsInstance<StepUi.Place>()
    fun nameOf(id: String?) = places.firstOrNull { it.id == id }?.name.orEmpty()

    val answer = step.answer
    return TransportStepUi(
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
