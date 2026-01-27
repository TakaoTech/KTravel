package com.takaotech.ktravel.presentation.planner

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.takaotech.ktravel.domain.repository.TravelPlanRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.koin.android.annotation.KoinViewModel
import org.koin.core.annotation.InjectedParam

@OptIn(ExperimentalCoroutinesApi::class)
@KoinViewModel
class PlanningDetailViewModel(
    @InjectedParam private val dayId: String,
    private val repository: TravelPlanRepository
) : ViewModel() {

    /**
     * Stato del giorno corrente, sincronizzato automaticamente con il repository
     */
    val travelDay: StateFlow<TravelDay?> = repository.getTravelDayFlow(dayId)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    fun onStepRemoveRequested(stepId: String) {
        viewModelScope.launch {
            repository.removeStepFromDay(dayId, stepId)
        }
    }
}