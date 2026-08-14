package com.takaotech.ktravel.presentation.settings

import androidx.compose.runtime.Stable
import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.takaotech.ktravel.di.AppScope
import com.takaotech.ktravel.domain.repository.AppSettingsRepository
import com.takaotech.navigator.client.NavigatorClient
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metro.Inject
import dev.zacsweers.metrox.viewmodel.ViewModelKey
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@Stable
data class AppSettingsUiState(
    val navigatorBaseUrl: TextFieldValue = TextFieldValue(""),
    val reachability: NavigatorReachability = NavigatorReachability.Unknown,
    val isSaved: Boolean = false,
)

/**
 * Settings of the installation, not of a trip.
 *
 * A plain view model with no assisted travel id, and that is the whole point: it is reached from the
 * trip list, because where this device sends its routing requests has to be configurable before any
 * trip is open, and has to survive every trip being deleted.
 */
@ContributesIntoMap(AppScope::class)
@ViewModelKey
@Inject
class AppSettingsViewModel(
    private val appSettingsRepository: AppSettingsRepository,
    private val navigatorClient: NavigatorClient,
) : ViewModel() {

    private val _uiState = MutableStateFlow(AppSettingsUiState())
    val uiState: StateFlow<AppSettingsUiState> = _uiState.asStateFlow()

    init {
        // Read once rather than collected: the repository emits on every save, and re-seeding the
        // fields from it would wipe whatever the user is in the middle of typing.
        val stored = appSettingsRepository.settings.value
        _uiState.update {
            it.copy(
                navigatorBaseUrl = TextFieldValue(stored.navigatorRemoteBaseUrl),
                reachability = if (stored.hasRemoteNavigator) {
                    NavigatorReachability.Unknown
                } else {
                    NavigatorReachability.NotConfigured
                },
            )
        }
    }

    fun onBaseUrlChanged(value: TextFieldValue) {
        _uiState.update {
            // Any edit invalidates what the last check said: leaving a green badge next to a changed
            // address is the one thing this indicator must never do.
            it.copy(navigatorBaseUrl = value, reachability = NavigatorReachability.Unknown, isSaved = false)
        }
    }

    private var checkJob: Job? = null

    /** Asks the navigator at the address currently typed whether it is there. */
    fun checkReachability() {
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

    fun save() {
        val state = _uiState.value
        viewModelScope.launch {
            appSettingsRepository.updateNavigatorRemote(baseUrl = state.navigatorBaseUrl.text)
            _uiState.update { it.copy(isSaved = true) }
        }
    }

    fun onSavedMessageShown() = _uiState.update { it.copy(isSaved = false) }
}
