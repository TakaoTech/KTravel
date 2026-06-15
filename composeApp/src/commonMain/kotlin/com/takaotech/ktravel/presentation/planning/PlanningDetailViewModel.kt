package com.takaotech.ktravel.presentation.planning

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.takaotech.ktravel.di.AppScope
import com.takaotech.ktravel.di.PlanningGraphStore
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metrox.viewmodel.ManualViewModelAssistedFactory
import dev.zacsweers.metrox.viewmodel.ManualViewModelAssistedFactoryKey
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
@AssistedInject
class PlanningDetailViewModel(
    @Assisted private val travelId: String,
    @Assisted private val dayId: String,
    private val planningGraphStore: PlanningGraphStore,
) : ViewModel() {

    @AssistedFactory
    @ContributesIntoMap(AppScope::class)
    @ManualViewModelAssistedFactoryKey
    fun interface Factory : ManualViewModelAssistedFactory {
        fun create(travelId: String, dayId: String): PlanningDetailViewModel
    }

    private val repository get() = planningGraphStore.getOrCreate(travelId).travelPlanRepository

    val travelDay: StateFlow<TravelDay> = repository.getTravelDayFlow(dayId)
        .map { with(TravelPlanUiMapper) { it.toUiDay() } }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = TravelDay.EMPTY
        )

    fun movePlaceToGeneral(placeId: String) {
        viewModelScope.launch {
            repository.movePlaceToGeneral(placeId, dayId)
        }
    }

    fun deletePlace(placeId: String) {
        viewModelScope.launch {
            repository.deletePlace(placeId, dayId)
        }
    }

    fun movePlaceToStep(placeId: String) {
        viewModelScope.launch {
            repository.movePlaceToStep(placeId, dayId)
        }
    }

    fun moveStepToPlace(stepId: String) {
        viewModelScope.launch {
            repository.moveStepToPlace(stepId, dayId)
        }
    }

    fun moveStepUp(stepId: String) {
        viewModelScope.launch {
            repository.moveTravelStepUp(stepId, dayId)
        }
    }

    fun moveStepDown(stepId: String) {
        viewModelScope.launch {
            repository.moveTravelStepDown(stepId, dayId)
        }
    }
}
