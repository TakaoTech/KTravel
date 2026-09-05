package com.takaotech.ktravel.presentation.settings

import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.takaotech.gunzou.client.NavigatorClient
import com.takaotech.ktravel.di.AppScope
import com.takaotech.ktravel.di.PlanningGraphStore
import com.takaotech.ktravel.domain.navigator.NavigatorKind
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metrox.viewmodel.ManualViewModelAssistedFactory
import dev.zacsweers.metrox.viewmodel.ManualViewModelAssistedFactoryKey
import kotlinx.coroutines.Job
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
class TravelSettingsViewModel(
    @Assisted private val travelId: String,
    private val planningGraphStore: PlanningGraphStore,
    private val navigatorClient: NavigatorClient,
) : ViewModel() {

    @AssistedFactory
    @ContributesIntoMap(AppScope::class)
    @ManualViewModelAssistedFactoryKey
    fun interface Factory : ManualViewModelAssistedFactory {
        fun create(travelId: String): TravelSettingsViewModel
    }

    private val settingsRepository get() = planningGraphStore.getOrCreate(travelId).settingsRepository

    private val _uiState = MutableStateFlow(TravelSettingsUiState())
    val uiState: StateFlow<TravelSettingsUiState> = _uiState.asStateFlow()

    init {
        // Read once instead of collecting: the plan emits on every edit, and re-seeding the field
        // from it would wipe whatever the user is typing.
        val stored = settingsRepository.settings
        _uiState.update {
            it.copy(
                hereApiKey = TextFieldValue(stored.hereApiKey),
                navigatorPreference = stored.navigatorPreference,
                navigatorBaseUrl = TextFieldValue(stored.navigatorRemoteBaseUrl),
                reachability = if (stored.overridesNavigatorRemote) {
                    NavigatorReachability.Unknown
                } else {
                    NavigatorReachability.NotConfigured
                },
            )
        }
    }

    fun onHereApiKeyChanged(apiKey: TextFieldValue) {
        _uiState.update { it.copy(hereApiKey = apiKey) }
    }

    fun onApiKeyVisibilityToggled() {
        _uiState.update { it.copy(isApiKeyVisible = !it.isApiKeyVisible) }
    }

    fun onNavigatorPreferenceChanged(preference: NavigatorKind) {
        _uiState.update { it.copy(navigatorPreference = preference) }
    }

    fun onNavigatorBaseUrlChanged(value: TextFieldValue) {
        // An edited address invalidates whatever the last check said about the previous one.
        _uiState.update { it.copy(navigatorBaseUrl = value, reachability = NavigatorReachability.Unknown) }
    }

    private var checkJob: Job? = null

    fun checkNavigatorReachability() {
        checkJob?.cancel()
        val state = _uiState.value
        if (state.navigatorBaseUrl.text.isBlank()) {
            _uiState.update { it.copy(reachability = NavigatorReachability.NotConfigured) }
            return
        }

        _uiState.update { it.copy(reachability = NavigatorReachability.Checking) }
        checkJob = viewModelScope.launch {
            val result = navigatorClient.checkReachability(baseUrl = state.navigatorBaseUrl.text)
            _uiState.update { it.copy(reachability = result) }
        }
    }

    fun saveSettings() {
        val state = _uiState.value
        viewModelScope.launch {
            settingsRepository.updateHereApiKey(state.hereApiKey.text)
            settingsRepository.updateNavigatorSettings(
                preference = state.navigatorPreference,
                remoteBaseUrl = state.navigatorBaseUrl.text,
            )
            _uiState.update { it.copy(isSaved = true) }
        }
    }

    fun onSavedMessageShown() {
        _uiState.update { it.copy(isSaved = false) }
    }
}
