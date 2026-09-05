package com.takaotech.ktravel.presentation.plan.transport

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.takaotech.ktravel.di.AppScope
import com.takaotech.ktravel.di.PlanningGraphStore
import com.takaotech.ktravel.domain.routing.model.RouteResult
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metrox.viewmodel.ManualViewModelAssistedFactory
import dev.zacsweers.metrox.viewmodel.ManualViewModelAssistedFactoryKey
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * The alternatives the composer computed, and the one the traveller files.
 *
 * Owns no answer of its own: it reads the plan's [RouteAnswerDraft], which is why this screen no
 * longer has to share a view model with the composer — the two were previously kept together by a
 * nested navigation graph, a shape Circuit does not have.
 */
@AssistedInject
class RoutePreviewViewModel(
    @Assisted private val travelId: String,
    @Assisted private val dayId: String,
    @Assisted private val startPlaceId: String,
    private val planningGraphStore: PlanningGraphStore,
) : ViewModel() {

    /** Builds the view model for one leg being previewed. */
    @AssistedFactory
    @ContributesIntoMap(AppScope::class)
    @ManualViewModelAssistedFactoryKey
    fun interface Factory : ManualViewModelAssistedFactory {
        /**
         * @param travelId The trip whose graph holds the answer.
         * @param dayId The day the leg belongs to.
         * @param startPlaceId The place the leg starts from, which is where it is filed.
         */
        fun create(travelId: String, dayId: String, startPlaceId: String): RoutePreviewViewModel
    }

    private val planningGraph get() = planningGraphStore.getOrCreate(travelId)
    private val routeAnswerDraft get() = planningGraph.routeAnswerDraft

    private val savedStepId = MutableStateFlow<String?>(null)

    /** What to draw, and — once the traveller confirms — which leg to open. */
    val uiState: StateFlow<RoutePreviewUiState> =
        combine(routeAnswerDraft.answer, savedStepId) { answer, stepId ->
            RoutePreviewUiState(
                result = answer.result,
                selectedIndex = answer.selectedIndex,
                savedStepId = stepId,
            )
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), RoutePreviewUiState())

    /** Moves to another alternative of the same answer. */
    fun selectRoute(index: Int) = routeAnswerDraft.select(index)

    /**
     * Files the alternative the traveller confirmed into the plan.
     *
     * Exhaustive on the kind of answer rather than on an index into one list, because the two are
     * saved from different models — and a journey has to be filed under the vehicle it starts with
     * rather than under the walk to the stop.
     */
    fun saveSelectedRoute() {
        viewModelScope.launch(Dispatchers.Default) {
            val answer = routeAnswerDraft.answer.value
            val request = answer.requestUsed ?: return@launch
            val index = answer.selectedIndex

            savedStepId.value = when (val result = answer.result) {
                null -> return@launch

                is RouteResult.Routing -> result.routes.routes.getOrNull(index)?.let {
                    planningGraph.saveTransportStepUseCase(dayId, startPlaceId, it, request)
                }

                is RouteResult.Transit -> result.journeys.journeys.getOrNull(index)?.let {
                    planningGraph.saveTransportStepUseCase(dayId, startPlaceId, it, request)
                }
            } ?: return@launch
        }
    }

    /**
     * Acknowledges that the host has opened the filed leg, so coming back here does not reopen it.
     */
    fun onTransportStepOpened() {
        savedStepId.value = null
    }
}

/**
 * @property result The alternatives to draw, or null before anything has been computed.
 * @property selectedIndex Which alternative is being shown.
 * @property savedStepId The leg the confirmed alternative produced, until the host has opened it.
 */
data class RoutePreviewUiState(
    val result: RouteResult? = null,
    val selectedIndex: Int = 0,
    val savedStepId: String? = null,
)
