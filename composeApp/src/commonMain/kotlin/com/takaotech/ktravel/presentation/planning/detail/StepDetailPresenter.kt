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
import com.takaotech.ktravel.domain.model.StepDomain
import com.takaotech.ktravel.domain.model.TravelPlanDomain
import com.takaotech.ktravel.presentation.planning.StepUi
import com.takaotech.ktravel.presentation.planning.TravelPlanUiMapper
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

/**
 * Presenter Circuit del dettaglio step (function-based, registrato via Metro `@CircuitInject`).
 *
 * Risolve il repository con `getOrCreate(travelId)` (vincolo V3), osserva il flow del piano e
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

    // The plan is already in memory (`planningState` is a StateFlow), so the step is read
    // synchronously for the first frame: collecting from `null` would render the loading screen
    // once and then rebuild the whole content — map included — one frame later.
    val initialPlace = remember(repository, screen.dayId, screen.stepId) {
        repository.planningState.value.selectPlaceStep(screen.dayId, screen.stepId)
    }
    val detailFlow = remember(repository, screen.dayId, screen.stepId) {
        repository.planningState
            .map { plan -> plan.selectPlaceStep(screen.dayId, screen.stepId) }
            .distinctUntilChanged()
            .flowOn(Dispatchers.Default)
    }
    val place: StepUi.Place? by detailFlow.collectAsState(initial = initialPlace)

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

/**
 * The place step this screen shows, picked straight out of the plan.
 *
 * Selecting in the domain and mapping the single step keeps the whole day out of it: mapping the day
 * would rebuild every step and every place of it — and [com.takaotech.ktravel.presentation.planning.TravelDayUi]
 * recomputes its place steps on construction — for one step that is wanted.
 */
private fun TravelPlanDomain.selectPlaceStep(dayId: String, stepId: String): StepUi.Place? =
    days.firstOrNull { it.id == dayId }
        ?.steps
        ?.firstOrNull { it.id == stepId }
        ?.let { it as? StepDomain.Place }
        ?.let { with(TravelPlanUiMapper) { it.toUiStepPlace() } }
