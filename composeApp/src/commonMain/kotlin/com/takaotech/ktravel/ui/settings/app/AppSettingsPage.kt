package com.takaotech.ktravel.ui.settings.app

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.takaotech.ktravel.presentation.settings.AppSettingsUiState
import com.takaotech.ktravel.presentation.settings.AppSettingsViewModel
import com.takaotech.ktravel.ui.settings.component.ReachabilityBadge
import com.takaotech.ktravel.ui.theme.KTravelTheme
import ktravel.composeapp.generated.resources.Res
import ktravel.composeapp.generated.resources.app_settings_about_section
import ktravel.composeapp.generated.resources.app_settings_diagnostics
import ktravel.composeapp.generated.resources.app_settings_licenses
import ktravel.composeapp.generated.resources.app_settings_navigator_hint
import ktravel.composeapp.generated.resources.app_settings_navigator_section
import ktravel.composeapp.generated.resources.app_settings_saved
import ktravel.composeapp.generated.resources.app_settings_test_connection
import ktravel.composeapp.generated.resources.app_settings_title
import ktravel.composeapp.generated.resources.arrow_back
import ktravel.composeapp.generated.resources.privacy_policy_label
import ktravel.composeapp.generated.resources.settings_navigator_base_url
import ktravel.composeapp.generated.resources.settings_save
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

/**
 * Where this installation sends its routing requests.
 *
 * Reached from the trip list rather than from inside a trip: it has to be usable before any trip
 * exists, and it outlives all of them.
 */
@Composable
fun AppSettingsPage(
    viewModel: AppSettingsViewModel,
    onNavigationBackClick: () -> Unit,
    onLicensesClick: () -> Unit,
    onPrivacyPolicyClick: () -> Unit,
    onDiagnosticsClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val savedMessage = stringResource(Res.string.app_settings_saved)

    LaunchedEffect(uiState.isSaved) {
        if (uiState.isSaved) {
            snackbarHostState.showSnackbar(savedMessage)
            viewModel.onSavedMessageShown()
        }
    }

    AppSettingsContent(
        uiState = uiState,
        snackbarHostState = snackbarHostState,
        onNavigationBackClick = onNavigationBackClick,
        onLicensesClick = onLicensesClick,
        onPrivacyPolicyClick = onPrivacyPolicyClick,
        onDiagnosticsClick = onDiagnosticsClick,
        onBaseUrlChange = viewModel::onBaseUrlChanged,
        onTestConnection = viewModel::checkReachability,
        onSave = viewModel::save,
        modifier = modifier,
    )
}

/**
 * The screen with nothing behind it: everything it draws is an argument.
 *
 * Split from [AppSettingsPage] because the page resolves a view model and a screen that needs the
 * graph to exist can be neither rendered on its own nor previewed.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun AppSettingsContent(
    uiState: AppSettingsUiState,
    snackbarHostState: SnackbarHostState,
    onNavigationBackClick: () -> Unit,
    onLicensesClick: () -> Unit,
    onDiagnosticsClick: () -> Unit,
    onPrivacyPolicyClick: () -> Unit,
    onBaseUrlChange: (TextFieldValue) -> Unit,
    onTestConnection: () -> Unit,
    onSave: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(Res.string.app_settings_title)) },
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
                    onClick = onSave,
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
                        .testTag(AppSettingsTestTags.SAVE),
                ) {
                    Text(stringResource(Res.string.settings_save))
                }
            }
        },
    ) { paddingValues ->
        Column(
            modifier = Modifier.fillMaxSize().padding(paddingValues).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = stringResource(Res.string.app_settings_navigator_section),
                style = MaterialTheme.typography.titleMedium,
            )

            OutlinedTextField(
                value = uiState.navigatorBaseUrl,
                onValueChange = onBaseUrlChange,
                label = { Text(stringResource(Res.string.settings_navigator_base_url)) },
                supportingText = { Text(stringResource(Res.string.app_settings_navigator_hint)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth().testTag(AppSettingsTestTags.BASE_URL),
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                OutlinedButton(
                    onClick = onTestConnection,
                    modifier = Modifier.testTag(AppSettingsTestTags.TEST_CONNECTION),
                ) {
                    Text(stringResource(Res.string.app_settings_test_connection))
                }
                ReachabilityBadge(
                    reachability = uiState.reachability,
                    modifier = Modifier.testTag(AppSettingsTestTags.REACHABILITY),
                )
            }

            Text(
                text = stringResource(Res.string.app_settings_about_section),
                style = MaterialTheme.typography.titleMedium,
            )

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                OutlinedButton(
                    onClick = onLicensesClick,
                    modifier = Modifier.testTag(AppSettingsTestTags.LICENSES),
                ) {
                    Text(stringResource(Res.string.app_settings_licenses))
                }

                OutlinedButton(
                    onClick = onDiagnosticsClick,
                    modifier = Modifier.testTag(AppSettingsTestTags.DIAGNOSTICS),
                ) {
                    Text(stringResource(Res.string.app_settings_diagnostics))
                }

                OutlinedButton(onClick = onPrivacyPolicyClick) {
                    Text(stringResource(Res.string.privacy_policy_label))
                }
            }
        }
    }
}

//region Previews

@PreviewScreenSizes
@Composable
private fun AppSettingsContentPreview(
    @PreviewParameter(AppSettingsContentPreviewParams::class) uiState: AppSettingsUiState,
) = KTravelTheme {
    AppSettingsContent(
        uiState = uiState,
        snackbarHostState = remember { SnackbarHostState() },
        onNavigationBackClick = {},
        onLicensesClick = {},
        onPrivacyPolicyClick = {},
        onDiagnosticsClick = {},
        onBaseUrlChange = {},
        onTestConnection = {},
        onSave = {},
    )
}
//endregion Previews
