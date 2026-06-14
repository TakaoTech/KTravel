package com.takaotech.ktravel.presentation.intro

import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.takaotech.ktravel.core.toLocalDate
import com.takaotech.ktravel.core.ui.FieldValidationState
import com.takaotech.ktravel.core.ui.toTextPayload
import com.takaotech.ktravel.di.AppScope
import com.takaotech.ktravel.di.PlanningGraphStore
import com.takaotech.ktravel.domain.repository.TravelManagerRepository
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ktravel.composeapp.generated.resources.Res
import ktravel.composeapp.generated.resources.travel_creation_name_empty_error
import kotlin.time.Instant

@SingleIn(AppScope::class)
@Inject
class TravelCreationViewModel(
    private val repository: TravelManagerRepository,
    private val planningGraphStore: PlanningGraphStore,
) : ViewModel() {

    private val _uiState = MutableStateFlow(TravelCreationUiState())
    val uiState: StateFlow<TravelCreationUiState> = _uiState.asStateFlow()

    fun onNameChange(name: TextFieldValue) {
        _uiState.update {
            it.copy(
                travelName = it.travelName.copy(
                    value = name,
                    validationState = FieldValidationState.None
                )
            )
        }
    }

    fun onDateRangeChange(start: Long, end: Long) {
        _uiState.update { it.copy(startDateMillis = start, endDateMillis = end) }
    }

    fun createTravelPlan() {
        val currentState = _uiState.value
        val name = currentState.travelName.value.text
        val start = currentState.startDateMillis
        val end = currentState.endDateMillis

        if (name.isBlank()) {
            _uiState.update {
                it.copy(
                    travelName = it.travelName.copy(
                        validationState = FieldValidationState.BaseNotValid(
                            errorText = Res.string.travel_creation_name_empty_error.toTextPayload()
                        )
                    )
                )
            }
            return
        }

        if (start == 0L || end == 0L) {
            _uiState.update { it.copy(error = "Compila tutti i campi") }
            return
        }

        viewModelScope.launch(Dispatchers.Default) {
            _uiState.update { it.copy(isLoading = true, error = null) }
            runCatching {
                repository.createTravelPlan(
                    name = name,
                    periodStart = Instant.fromEpochMilliseconds(start).toLocalDate(),
                    periodEnd = Instant.fromEpochMilliseconds(end).toLocalDate()
                )
            }.onSuccess { id ->
                val planningGraph = planningGraphStore.getOrCreate(id)
                planningGraph.travelPlanRepository.updatePeriod(start, end)

                _uiState.update { it.copy(isLoading = false, createdTravelId = id) }
            }.onFailure { error ->
                _uiState.update { it.copy(isLoading = false, error = error.message) }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}
