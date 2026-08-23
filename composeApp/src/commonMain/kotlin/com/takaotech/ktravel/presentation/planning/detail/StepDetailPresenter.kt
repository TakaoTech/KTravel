package com.takaotech.ktravel.presentation.planning.detail

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import com.slack.circuit.codegen.annotations.CircuitInject
import com.slack.circuit.runtime.Navigator
import com.takaotech.ktravel.data.datasource.AttachmentDataSource
import com.takaotech.ktravel.di.AppScope
import com.takaotech.ktravel.di.PlanningGraphStore
import com.takaotech.ktravel.presentation.planning.StepUi
import com.takaotech.ktravel.presentation.planning.TravelPlanUiMapper
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

/**
 * Presenter Circuit del dettaglio step (function-based, registrato via Metro `@CircuitInject`).
 *
 * Risolve il repository con `getOrCreate(travelId)` (vincolo V3), osserva il flow del giorno e
 * seleziona lo [StepUi.Place] con id [StepDetailScreen.stepId]. Le note e l'inventario file sono
 * delegati per intero a [rememberStepNotesEditing], condiviso con il dettaglio del trasporto.
 */
@CircuitInject(StepDetailScreen::class, AppScope::class)
@Composable
fun StepDetailPresenter(
    screen: StepDetailScreen,
    navigator: Navigator,
    planningGraphStore: PlanningGraphStore,
    attachmentDataSource: AttachmentDataSource,
): StepDetailUiState {
    val repository = remember(screen.travelId) {
        planningGraphStore.getOrCreate(screen.travelId).travelPlanRepository
    }
    val scope = rememberCoroutineScope()

    val detailFlow = remember(repository, screen.dayId, screen.stepId) {
        repository.getTravelDayFlow(screen.dayId).map { day ->
            with(TravelPlanUiMapper) { day.toUiDay() }.steps
                .filterIsInstance<StepUi.Place>()
                .firstOrNull { it.id == screen.stepId }
        }
    }
    val detail by detailFlow.collectAsState(initial = null)
    val place: StepUi.Place? = detail

    val notes = rememberStepNotesEditing(
        repository = repository,
        resolveFile = attachmentDataSource::resolveFile,
        dayId = screen.dayId,
        stepId = screen.stepId,
        savedNote = place?.note.orEmpty(),
        attachments = place?.attachments ?: persistentListOf(),
    )

    return StepDetailUiState(place = place, notes = notes.state) { event ->
        when (event) {
            StepDetailEvent.NavigateBack -> navigator.pop()

            is StepDetailEvent.SetStartTime -> {
                scope.launch {
                    repository.updatePlaceStartTime(screen.dayId, screen.stepId, event.time)
                }
            }

            is StepDetailEvent.SetEndTime -> {
                scope.launch {
                    repository.updatePlaceEndTime(screen.dayId, screen.stepId, event.time)
                }
            }

            is StepDetailEvent.Notes -> notes(event.event)
        }
    }
}
