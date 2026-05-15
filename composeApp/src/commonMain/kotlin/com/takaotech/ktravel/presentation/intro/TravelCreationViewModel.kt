package com.takaotech.ktravel.presentation.intro

import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.takaotech.ktravel.core.toLocalDate
import com.takaotech.ktravel.core.ui.FieldValidationState
import com.takaotech.ktravel.core.ui.toTextPayload
import com.takaotech.ktravel.domain.repository.TravelManagerRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ktravel.composeapp.generated.resources.Res
import ktravel.composeapp.generated.resources.travel_creation_name_empty_error
import org.koin.core.annotation.KoinViewModel
import kotlin.time.Instant

@KoinViewModel
class TravelCreationViewModel(
    private val repository: TravelManagerRepository
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

    fun onDateRangeChange(start: Long?, end: Long?) {
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

        if (start == null || end == null) {
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
