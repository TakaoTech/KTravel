package com.takaotech.ktravel.presentation.settings

import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.takaotech.ktravel.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.android.annotation.KoinViewModel

@KoinViewModel
class SettingsViewModel(
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            settingsRepository.hereApiKey.collect { apiKey ->
                _uiState.update {
                    it.copy(
                        hereApiKey = TextFieldValue(apiKey),
                    )
                }
            }
        }
    }

    fun onHereApiKeyChanged(apiKey: TextFieldValue) {
        _uiState.update {
            it.copy(
                hereApiKey = apiKey,
            )
        }
    }

    fun saveSettings() {
        settingsRepository.updateHereApiKey(_uiState.value.hereApiKey.text)
    }
}
