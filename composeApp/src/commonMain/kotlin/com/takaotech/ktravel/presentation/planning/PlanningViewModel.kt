package com.takaotech.ktravel.presentation.planning

import androidx.compose.ui.text.input.TextFieldValue
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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@AssistedInject
class PlanningViewModel(
    @Assisted private val travelId: String,
    private val planningGraphStore: PlanningGraphStore,
) : ViewModel() {

    @AssistedFactory
    @ContributesIntoMap(AppScope::class)
    @ManualViewModelAssistedFactoryKey
    fun interface Factory : ManualViewModelAssistedFactory {
        fun create(travelId: String): PlanningViewModel
    }

    private val repository get() = planningGraphStore.getOrCreate(travelId).travelPlanRepository

    private val _uiState = MutableStateFlow(PlanningUiState())
    val uiState: StateFlow<PlanningUiState> = _uiState.asStateFlow()

    init {
        repository.planningState
            .onEach { domainState ->
                val domainName = domainState.name
                _uiState.update { current ->
                    val mappedState = with(TravelPlanUiMapper) { domainState.toUiState() }
                    if (domainName != current.planHeader.name.text) {
                        // External change — safe to replace TextFieldValue
                        mappedState
                    } else {
                        // Same text — preserve existing TextFieldValue (cursor/selection intact)
                        mappedState.copy(
                            planHeader = mappedState.planHeader.copy(name = current.planHeader.name)
                        )
                    }
                }
            }
            .launchIn(viewModelScope)
    }

    fun onPlanNameChanged(name: TextFieldValue) {
        // Immediately update UI state preserving full TextFieldValue (cursor/selection)
        _uiState.update { it.copy(planHeader = it.planHeader.copy(name = name)) }
        // Send only the String to the domain layer
        viewModelScope.launch {
            repository.updatePlanName(name.text)
        }
    }

    fun onPlanDateChanged(start: Long, end: Long) {
        viewModelScope.launch {
            repository.updatePeriod(start, end)
        }
    }

    fun onPlaceMovedToDate(placeId: String, dayId: String) {
        viewModelScope.launch {
            repository.movePlaceToDay(placeId, dayId)
        }
    }

    fun deletePlace(placeId: String) {
        viewModelScope.launch {
            repository.deletePlace(placeId, null)
        }
    }
}