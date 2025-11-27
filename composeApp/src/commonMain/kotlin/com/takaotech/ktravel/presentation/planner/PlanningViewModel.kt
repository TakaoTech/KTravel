package com.takaotech.ktravel.presentation.planner

import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.takaotech.ktravel.domain.repository.TravelPlanRepository
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import org.koin.android.annotation.KoinViewModel

@KoinViewModel
class PlanningViewModel(
    private val repository: TravelPlanRepository
) : ViewModel() {

    val uiState: StateFlow<PlanningUiState> = repository.planningState

    fun onPlanNameChanged(name: TextFieldValue) {
        // TODO: Implementa l'aggiornamento del nome nel repository se necessario
    }

    fun onPlanDateChanged(start: Long, end: Long) {
        viewModelScope.launch {
            repository.updatePeriod(start, end)
        }
    }

    fun onTStepCreateRequested(day: LocalDate, name: String) {
        viewModelScope.launch {
            val dayId = uiState.value.days.firstOrNull { it.date == day }?.id ?: return@launch
            repository.addStepToDay(
                dayId = dayId,
                step = TravelDay.Step.Place(location = name)
            )
        }
    }
}