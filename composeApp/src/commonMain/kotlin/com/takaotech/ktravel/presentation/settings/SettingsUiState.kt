package com.takaotech.ktravel.presentation.settings

import androidx.compose.runtime.Stable
import androidx.compose.ui.text.input.TextFieldValue

@Stable
data class SettingsUiState(
    val hereApiKey: TextFieldValue = TextFieldValue(""),
    /** The key is masked by default; the user can reveal it to check what they pasted. */
    val isApiKeyVisible: Boolean = false,
    /** Set once the key has been written to the plan, cleared when the confirmation is shown. */
    val isSaved: Boolean = false,
)
