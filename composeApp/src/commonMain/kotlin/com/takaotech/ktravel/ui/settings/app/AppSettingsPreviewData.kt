package com.takaotech.ktravel.ui.settings.app

import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import com.takaotech.ktravel.presentation.settings.AppSettingsUiState
import com.takaotech.ktravel.presentation.settings.NavigatorReachability

/**
 * A fresh installation, a navigator that answers, one that does not, and an address too long to read.
 *
 * The empty one is what every installation starts as, and it is the only state where the badge says
 * the navigator was never configured rather than never reached. The unreachable one is the reason
 * the check exists at all. The long address goes into a `singleLine` field, which scrolls its
 * content rather than wrapping it, so what has to stay usable there is the field and not the text.
 */
internal class AppSettingsContentPreviewParams : PreviewParameterProvider<AppSettingsUiState> {
    override val values = sequenceOf(
        AppSettingsUiState(reachability = NavigatorReachability.NotConfigured),
        AppSettingsUiState(
            navigatorBaseUrl = TextFieldValue("https://gunzou.example.org"),
            reachability = NavigatorReachability.Reachable(version = "1.4.0", latencyMillis = 42),
        ),
        AppSettingsUiState(
            navigatorBaseUrl = TextFieldValue("https://gunzou.example.org"),
            reachability = NavigatorReachability.Unreachable,
        ),
        AppSettingsUiState(
            navigatorBaseUrl = TextFieldValue("https://routing.internal.example.org:8443/gunzou/api/v1"),
            reachability = NavigatorReachability.Checking,
        ),
    )
}
