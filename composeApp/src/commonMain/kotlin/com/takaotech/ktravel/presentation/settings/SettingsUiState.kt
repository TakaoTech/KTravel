package com.takaotech.ktravel.presentation.settings

import androidx.compose.ui.text.input.TextFieldValue

data class SettingsUiState(
    val hereApiKey: TextFieldValue = TextFieldValue(""),
)
