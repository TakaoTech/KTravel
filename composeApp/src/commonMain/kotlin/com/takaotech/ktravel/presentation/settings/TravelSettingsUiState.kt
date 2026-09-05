package com.takaotech.ktravel.presentation.settings

import androidx.compose.runtime.Stable
import androidx.compose.ui.text.input.TextFieldValue
import com.takaotech.ktravel.domain.navigator.NavigatorKind

@Stable
data class TravelSettingsUiState(
    val hereApiKey: TextFieldValue = TextFieldValue(""),
    /** The key is masked by default; the user can reveal it to check what they pasted. */
    val isApiKeyVisible: Boolean = false,
    /** Which navigator the transport screen of this trip opens on. */
    val navigatorPreference: NavigatorKind = NavigatorKind.EMBEDDED,
    /**
     * A navigator for this trip alone, empty when it uses the one the installation is configured
     * with.
     */
    val navigatorBaseUrl: TextFieldValue = TextFieldValue(""),
    val reachability: NavigatorReachability = NavigatorReachability.Unknown,
    /**
     * Set once the settings have been written to the plan, cleared when the confirmation is shown.
     */
    val isSaved: Boolean = false,
)
