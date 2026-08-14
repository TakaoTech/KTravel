package com.takaotech.ktravel.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.takaotech.ktravel.domain.navigator.NavigatorKind
import com.takaotech.ktravel.presentation.settings.SettingsViewModel
import kotlinx.serialization.Serializable
import ktravel.composeapp.generated.resources.Res
import ktravel.composeapp.generated.resources.app_settings_test_connection
import ktravel.composeapp.generated.resources.arrow_back
import ktravel.composeapp.generated.resources.planning_transport_server_embedded
import ktravel.composeapp.generated.resources.planning_transport_server_remote
import ktravel.composeapp.generated.resources.settings_here_api_key
import ktravel.composeapp.generated.resources.settings_here_api_key_hint
import ktravel.composeapp.generated.resources.settings_here_section
import ktravel.composeapp.generated.resources.settings_hide_api_key
import ktravel.composeapp.generated.resources.settings_navigator_base_url
import ktravel.composeapp.generated.resources.settings_navigator_hint
import ktravel.composeapp.generated.resources.settings_navigator_override_hint
import ktravel.composeapp.generated.resources.settings_navigator_section
import ktravel.composeapp.generated.resources.settings_save
import ktravel.composeapp.generated.resources.settings_saved
import ktravel.composeapp.generated.resources.settings_show_api_key
import ktravel.composeapp.generated.resources.settings_title
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Serializable
object SettingsNavigation

internal object SettingsTestTags {
    const val API_KEY = "settings_api_key"
    const val TOGGLE_VISIBILITY = "settings_toggle_api_key_visibility"
    const val SAVE = "settings_save"
    const val NAVIGATOR_BASE_URL = "settings_navigator_base_url"
    const val NAVIGATOR_TEST = "settings_navigator_test"
    const val NAVIGATOR_REACHABILITY = "settings_navigator_reachability"

    fun navigatorPreferenceTag(kind: String): String = "settings_navigator_preference_$kind"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsPage(viewModel: SettingsViewModel, onNavigationBackClick: () -> Unit, modifier: Modifier = Modifier) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val savedMessage = stringResource(Res.string.settings_saved)

    LaunchedEffect(uiState.isSaved) {
        if (uiState.isSaved) {
            snackbarHostState.showSnackbar(savedMessage)
            viewModel.onSavedMessageShown()
        }
    }

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
                    onClick = { viewModel.saveSettings() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag(SettingsTestTags.SAVE),
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
                onValueChange = { viewModel.onHereApiKeyChanged(it) },
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
                        onClick = { viewModel.onApiKeyVisibilityToggled() },
                        modifier = Modifier.testTag(SettingsTestTags.TOGGLE_VISIBILITY),
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
                    .testTag(SettingsTestTags.API_KEY),
                singleLine = true,
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            NavigatorSection(uiState = uiState, viewModel = viewModel)
        }
    }
}

/**
 * Where the routes of this trip are computed.
 *
 * A section of its own rather than more lines in the page: it edits three related settings and owns
 * a reachability check, and folding that into the API key screen made one composable answer two
 * unrelated questions.
 */
@Composable
private fun NavigatorSection(
    uiState: com.takaotech.ktravel.presentation.settings.SettingsUiState,
    viewModel: SettingsViewModel,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(
            text = stringResource(Res.string.settings_navigator_section),
            style = MaterialTheme.typography.titleMedium,
        )
        Text(
            text = stringResource(Res.string.settings_navigator_hint),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
            NavigatorKind.entries.forEachIndexed { index, kind ->
                SegmentedButton(
                    modifier = Modifier.testTag(SettingsTestTags.navigatorPreferenceTag(kind.name)),
                    selected = uiState.navigatorPreference == kind,
                    onClick = { viewModel.onNavigatorPreferenceChanged(kind) },
                    shape = SegmentedButtonDefaults.itemShape(index = index, count = NavigatorKind.entries.size),
                ) {
                    Text(
                        stringResource(
                            when (kind) {
                                NavigatorKind.EMBEDDED -> Res.string.planning_transport_server_embedded
                                NavigatorKind.REMOTE -> Res.string.planning_transport_server_remote
                            },
                        ),
                    )
                }
            }
        }

        OutlinedTextField(
            value = uiState.navigatorBaseUrl,
            onValueChange = viewModel::onNavigatorBaseUrlChanged,
            label = { Text(stringResource(Res.string.settings_navigator_base_url)) },
            supportingText = { Text(stringResource(Res.string.settings_navigator_override_hint)) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth().testTag(SettingsTestTags.NAVIGATOR_BASE_URL),
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            OutlinedButton(
                onClick = viewModel::checkNavigatorReachability,
                modifier = Modifier.testTag(SettingsTestTags.NAVIGATOR_TEST),
            ) {
                Text(stringResource(Res.string.app_settings_test_connection))
            }
            ReachabilityBadge(
                reachability = uiState.reachability,
                modifier = Modifier.testTag(SettingsTestTags.NAVIGATOR_REACHABILITY),
            )
        }
    }
}
