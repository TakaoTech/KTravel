package com.takaotech.ktravel.presentation.travels

import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.takaotech.ktravel.core.logging.AppLogger
import com.takaotech.ktravel.core.toLocalDate
import com.takaotech.ktravel.di.AppScope
import com.takaotech.ktravel.di.PlanningGraphStore
import com.takaotech.ktravel.domain.repository.TravelManagerRepository
import com.takaotech.ktravel.presentation.field.FieldValidationState
import com.takaotech.ktravel.presentation.field.toTextPayload
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metro.Inject
import dev.zacsweers.metrox.viewmodel.ViewModelKey
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ktravel.composeapp.generated.resources.Res
import ktravel.composeapp.generated.resources.travel_creation_name_empty_error
import kotlin.time.Instant

@ContributesIntoMap(AppScope::class)
@ViewModelKey
@Inject
class TravelCreationViewModel(
    private val repository: TravelManagerRepository,
    private val planningGraphStore: PlanningGraphStore,
    appLogger: AppLogger,
) : ViewModel() {

    private val _uiState = MutableStateFlow(TravelCreationUiState())
    val uiState: StateFlow<TravelCreationUiState> = _uiState.asStateFlow()

    private val logger = appLogger.withTag("TravelCreationViewModel")

    fun onNameChange(name: TextFieldValue) {
        _uiState.update {
            it.copy(
                travelName = it.travelName.copy(
                    value = name,
                    validationState = FieldValidationState.None,
                ),
            )
        }
    }

    fun onDateRangeChange(start: Long, end: Long) {
        logger.d { "Date range picked: $start - $end" }
        _uiState.update { it.copy(startDateMillis = start, endDateMillis = end) }
    }

    fun createTravelPlan() {
        val currentState = _uiState.value
        val name = currentState.travelName.value.text
        val start = currentState.startDateMillis
        val end = currentState.endDateMillis

        if (name.isBlank()) {
            logger.w { "Creation rejected: the travel name is blank" }
            _uiState.update {
                it.copy(
                    travelName = it.travelName.copy(
                        validationState = FieldValidationState.BaseNotValid(
                            errorText = Res.string.travel_creation_name_empty_error.toTextPayload(),
                        ),
                    ),
                )
            }
            return
        }

        if (start == 0L || end == 0L) {
            logger.w { "Creation rejected: incomplete date range ($start - $end)" }
            // TODO Move to strings
            _uiState.update { it.copy(error = "Compila tutti i campi") }
            return
        }

        viewModelScope.launch(Dispatchers.Default) {
            logger.d { "Creating travel plan '$name' over $start - $end" }
            _uiState.update { it.copy(isLoading = true, error = null) }
            runCatching {
                val id = repository.createTravelPlan(
                    name = name,
                    periodStart = Instant.fromEpochMilliseconds(start).toLocalDate(),
                    periodEnd = Instant.fromEpochMilliseconds(end).toLocalDate(),
                )
                planningGraphStore.getOrCreate(id).travelPlanRepository.updatePeriod(start, end)
                id
            }.onSuccess { id ->
                logger.i { "Travel plan $id created" }
                _uiState.update { it.copy(isLoading = false, createdTravelId = id) }
            }.onFailure { error ->
                logger.e(error) { "Failed to create the travel plan" }
                _uiState.update { it.copy(isLoading = false, error = error.message) }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}
