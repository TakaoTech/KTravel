package com.takaotech.ktravel.presentation.settings

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
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Settings of one travel plan.
 *
 * Assisted with the travel id because the API key lives in the plan, not in the app: the settings
 * page is only ever reached from a trip, and each trip carries its own key.
 */
@AssistedInject
class SettingsViewModel(@Assisted private val travelId: String, private val planningGraphStore: PlanningGraphStore) :
    ViewModel() {

    @AssistedFactory
    @ContributesIntoMap(AppScope::class)
    @ManualViewModelAssistedFactoryKey
    fun interface Factory : ManualViewModelAssistedFactory {
        fun create(travelId: String): SettingsViewModel
    }

    private val travelPlanRepository get() = planningGraphStore.getOrCreate(travelId).travelPlanRepository

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        // Read once instead of collecting: the plan emits on every edit, and re-seeding the field
        // from it would wipe whatever the user is typing.
        _uiState.update {
            it.copy(hereApiKey = TextFieldValue(travelPlanRepository.planningState.value.hereApiKey))
        }
    }

    fun onHereApiKeyChanged(apiKey: TextFieldValue) {
        _uiState.update { it.copy(hereApiKey = apiKey) }
    }

    fun onApiKeyVisibilityToggled() {
        _uiState.update { it.copy(isApiKeyVisible = !it.isApiKeyVisible) }
    }

    fun saveSettings() {
        val apiKey = _uiState.value.hereApiKey.text
        viewModelScope.launch {
            travelPlanRepository.updateHereApiKey(apiKey)
            _uiState.update { it.copy(isSaved = true) }
        }
    }

    fun onSavedMessageShown() {
        _uiState.update { it.copy(isSaved = false) }
    }
}
