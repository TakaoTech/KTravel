package com.takaotech.ktravel.presentation.planning

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.takaotech.ktravel.di.PlanningScope
import com.takaotech.ktravel.domain.repository.TravelPlanRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.koin.core.annotation.InjectedParam
import org.koin.core.annotation.KoinViewModel
import org.koin.core.annotation.Scope
import org.koin.core.annotation.Scoped

@OptIn(ExperimentalCoroutinesApi::class)
@KoinViewModel
@Scope(PlanningScope::class)
@Scoped
class PlanningDetailViewModel(
    @InjectedParam private val dayId: String,
    private val repository: TravelPlanRepository
) : ViewModel() {

    /**
     * Stato del giorno corrente, sincronizzato automaticamente con il repository
     */
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