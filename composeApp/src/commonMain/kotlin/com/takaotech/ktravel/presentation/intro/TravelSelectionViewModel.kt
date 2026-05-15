package com.takaotech.ktravel.presentation.intro

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.takaotech.ktravel.domain.repository.TravelManagerRepository
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.core.annotation.KoinViewModel

@KoinViewModel
class TravelSelectionViewModel(
    private val repository: TravelManagerRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(TravelSelectionUiState())
    val uiState: StateFlow<TravelSelectionUiState> = _uiState.asStateFlow()

    init {
        loadTravelPlans()
    }

    fun loadTravelPlans() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            runCatching {
                repository.getAllTravelPlans()
            }.onSuccess { plans ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        travelList = plans.map { plan ->
                            TravelSummaryUiState(
                                id = plan.id,
                                name = plan.name,
                                periodStart = plan.periodStart,
                                periodEnd = plan.periodEnd,
                            )
                        }.toPersistentList()
                    )
                }
            }.onFailure { error ->
                _uiState.update {
                    it.copy(isLoading = false, error = error.message)
                }
            }
        }
    }
}
