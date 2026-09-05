package com.takaotech.ktravel.ui.plan.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import com.takaotech.ktravel.domain.navigator.NavigatorKind
import com.takaotech.ktravel.presentation.settings.NavigatorReachability
import com.takaotech.ktravel.presentation.settings.TravelSettingsUiState
import com.takaotech.ktravel.ui.settings.component.ReachabilityBadge
import com.takaotech.ktravel.ui.theme.KTravelTheme
import ktravel.composeapp.generated.resources.Res
import ktravel.composeapp.generated.resources.app_settings_test_connection
import ktravel.composeapp.generated.resources.planning_transport_server_embedded
import ktravel.composeapp.generated.resources.planning_transport_server_remote
import ktravel.composeapp.generated.resources.settings_navigator_base_url
import ktravel.composeapp.generated.resources.settings_navigator_hint
import ktravel.composeapp.generated.resources.settings_navigator_override_hint
import ktravel.composeapp.generated.resources.settings_navigator_section
import org.jetbrains.compose.resources.stringResource

/**
 * Where the routes of this trip are computed.
 *
 * A section of its own rather than more lines in the page: it edits three related settings and owns
 * a reachability check, and folding that into the API key screen made one composable answer two
 * unrelated questions.
 *
 * It takes the three actions rather than the view model that carries them: those are all it ever
 * called, and a section that needs the graph to exist cannot be rendered on its own.
 */
@Composable
internal fun NavigatorSection(
    uiState: TravelSettingsUiState,
    onPreferenceChange: (NavigatorKind) -> Unit,
    onBaseUrlChange: (TextFieldValue) -> Unit,
    onTestConnection: () -> Unit,
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
                    modifier = Modifier.testTag(TravelSettingsTestTags.navigatorPreferenceTag(kind.name)),
                    selected = uiState.navigatorPreference == kind,
                    onClick = { onPreferenceChange(kind) },
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
            onValueChange = onBaseUrlChange,
            label = { Text(stringResource(Res.string.settings_navigator_base_url)) },
            supportingText = { Text(stringResource(Res.string.settings_navigator_override_hint)) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth().testTag(TravelSettingsTestTags.NAVIGATOR_BASE_URL),
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            OutlinedButton(
                onClick = onTestConnection,
                modifier = Modifier.testTag(TravelSettingsTestTags.NAVIGATOR_TEST),
            ) {
                Text(stringResource(Res.string.app_settings_test_connection))
            }
            ReachabilityBadge(
                reachability = uiState.reachability,
                modifier = Modifier.testTag(TravelSettingsTestTags.NAVIGATOR_REACHABILITY),
            )
        }
    }
}

//region Previews

@Preview(showBackground = true)
@Composable
private fun NavigatorSectionPreview(
    @PreviewParameter(NavigatorSectionPreviewParams::class) uiState: TravelSettingsUiState,
) = KTravelTheme {
    Surface {
        NavigatorSection(
            uiState = uiState,
            onPreferenceChange = {},
            onBaseUrlChange = {},
            onTestConnection = {},
            modifier = Modifier
                .width(360.dp)
                .padding(16.dp),
        )
    }
}

/**
 * The trip on the embedded navigator, on a remote one that answers, and on one that does not.
 *
 * The address and the badge are what the choice is worth: on the embedded navigator the field is
 * an override nobody has filled and the check has nothing to say, while a remote one is only as
 * good as its last answer. The three are read together, which is why they are previewed together
 * rather than through the badge alone.
 */
internal class NavigatorSectionPreviewParams : PreviewParameterProvider<TravelSettingsUiState> {
    override val values = sequenceOf(
        TravelSettingsUiState(navigatorPreference = NavigatorKind.EMBEDDED),
        TravelSettingsUiState(
            navigatorPreference = NavigatorKind.REMOTE,
            navigatorBaseUrl = TextFieldValue("https://gunzou.example.org"),
            reachability = NavigatorReachability.Reachable(version = "1.4.0", latencyMillis = 42),
        ),
        TravelSettingsUiState(
            navigatorPreference = NavigatorKind.REMOTE,
            navigatorBaseUrl = TextFieldValue("https://gunzou.example.org"),
            reachability = NavigatorReachability.Unreachable,
        ),
    )
}
//endregion Previews
