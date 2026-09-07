package com.takaotech.ktravel.ui.plan.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import androidx.compose.ui.unit.dp
import com.takaotech.ktravel.domain.navigator.NavigatorKind
import com.takaotech.ktravel.presentation.settings.NavigatorReachability
import com.takaotech.ktravel.presentation.settings.TravelSettingsUiState
import com.takaotech.ktravel.ui.theme.KTravelTheme
import ktravel.composeapp.generated.resources.Res
import ktravel.composeapp.generated.resources.arrow_back
import ktravel.composeapp.generated.resources.settings_here_api_key
import ktravel.composeapp.generated.resources.settings_here_api_key_hint
import ktravel.composeapp.generated.resources.settings_here_section
import ktravel.composeapp.generated.resources.settings_hide_api_key
import ktravel.composeapp.generated.resources.settings_save
import ktravel.composeapp.generated.resources.settings_show_api_key
import ktravel.composeapp.generated.resources.settings_title
import ktravel.composeapp.generated.resources.travel_settings_diagnostics
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

/**
 * The settings of one trip: the provider key its routing calls carry, and the navigator that
 * computes them.
 *
 * Takes the state and the eight actions rather than the view model holding them, for the reason
 * [NavigatorSection] states about itself: a screen that needs the dependency graph to exist cannot
 * be rendered on its own, neither in a preview nor in a test. [TravelSettingsPage] is the half that
 * keeps the view model, and the confirmation it posts arrives here through [snackbarHostState].
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun TravelSettingsContent(
    uiState: TravelSettingsUiState,
    onNavigationBackClick: () -> Unit,
    onDiagnosticsClick: () -> Unit,
    onHereApiKeyChange: (TextFieldValue) -> Unit,
    onApiKeyVisibilityToggle: () -> Unit,
    onNavigatorPreferenceChange: (NavigatorKind) -> Unit,
    onNavigatorBaseUrlChange: (TextFieldValue) -> Unit,
    onTestConnectionClick: () -> Unit,
    onSaveClick: () -> Unit,
    modifier: Modifier = Modifier,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(Res.string.settings_title)) },
                navigationIcon = {
                    IconButton(onClick = onNavigationBackClick) {
                        Icon(
                            painter = painterResource(Res.drawable.arrow_back),
                            contentDescription = null,
                        )
                    }
                },
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            BottomAppBar {
                Button(
                    onClick = onSaveClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag(TravelSettingsTestTags.SAVE),
                ) {
                    Text(stringResource(Res.string.settings_save))
                }
            }
        },
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = stringResource(Res.string.settings_here_section),
                style = MaterialTheme.typography.titleMedium,
            )

            OutlinedTextField(
                value = uiState.hereApiKey,
                onValueChange = onHereApiKeyChange,
                label = { Text(stringResource(Res.string.settings_here_api_key)) },
                supportingText = { Text(stringResource(Res.string.settings_here_api_key_hint)) },
                // The key is a credential: masked unless the user asks to check what they pasted.
                visualTransformation = if (uiState.isApiKeyVisible) {
                    VisualTransformation.None
                } else {
                    PasswordVisualTransformation()
                },
                trailingIcon = {
                    TextButton(
                        onClick = onApiKeyVisibilityToggle,
                        modifier = Modifier.testTag(TravelSettingsTestTags.TOGGLE_VISIBILITY),
                    ) {
                        Text(
                            stringResource(
                                if (uiState.isApiKeyVisible) {
                                    Res.string.settings_hide_api_key
                                } else {
                                    Res.string.settings_show_api_key
                                },
                            ),
                        )
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag(TravelSettingsTestTags.API_KEY),
                singleLine = true,
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            NavigatorSection(
                uiState = uiState,
                onPreferenceChange = onNavigatorPreferenceChange,
                onBaseUrlChange = onNavigatorBaseUrlChange,
                onTestConnection = onTestConnectionClick,
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            OutlinedButton(
                onClick = onDiagnosticsClick,
                modifier = Modifier.fillMaxWidth().testTag(TravelSettingsTestTags.DIAGNOSTICS),
            ) {
                Text(stringResource(Res.string.travel_settings_diagnostics))
            }
        }
    }
}

//region Previews
@PreviewScreenSizes
@Composable
private fun TravelSettingsPagePreview(
    @PreviewParameter(TravelSettingsPreviewParams::class) uiState: TravelSettingsUiState,
) = KTravelTheme {
    TravelSettingsContent(
        uiState = uiState,
        onNavigationBackClick = {},
        onDiagnosticsClick = {},
        onHereApiKeyChange = {},
        onApiKeyVisibilityToggle = {},
        onNavigatorPreferenceChange = {},
        onNavigatorBaseUrlChange = {},
        onTestConnectionClick = {},
        onSaveClick = {},
    )
}

/**
 * A trip nobody has configured yet, one configured and working, and one whose navigator is silent.
 *
 * The masked key is the state the screen is normally read in, so the revealed one is kept beside
 * it: those are the only two renders where the field's content is legible at all, and the toggle
 * label flips with them. The unreachable navigator carries the failure badge, which is the one
 * piece of this layout that grows when it has something to say — the empty trip shows how much
 * room is left for it.
 */
internal class TravelSettingsPreviewParams : PreviewParameterProvider<TravelSettingsUiState> {
    override val values = sequenceOf(
        TravelSettingsUiState(),
        TravelSettingsUiState(
            hereApiKey = TextFieldValue(PREVIEW_API_KEY),
            navigatorPreference = NavigatorKind.REMOTE,
            navigatorBaseUrl = TextFieldValue("https://gunzou.example.org"),
            reachability = NavigatorReachability.Reachable(version = "1.4.0", latencyMillis = 42),
        ),
        TravelSettingsUiState(
            hereApiKey = TextFieldValue(PREVIEW_API_KEY),
            isApiKeyVisible = true,
            navigatorPreference = NavigatorKind.REMOTE,
            navigatorBaseUrl = TextFieldValue("https://gunzou.example.org"),
            reachability = NavigatorReachability.Unreachable,
        ),
    )
}

/** As long as a real HERE key, and made of nothing that could be one. */
private const val PREVIEW_API_KEY = "not-a-real-here-api-key-0123456789abcd"
//endregion Previews
