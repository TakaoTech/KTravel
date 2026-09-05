package com.takaotech.ktravel.ui.plan.transport.composer.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import com.takaotech.ktravel.domain.navigator.NavigatorKind
import com.takaotech.ktravel.presentation.plan.transport.TransportPlanningUiState
import com.takaotech.ktravel.ui.plan.transport.component.ReachabilityPill
import com.takaotech.ktravel.ui.plan.transport.component.SectionLabel
import com.takaotech.ktravel.ui.plan.transport.component.TransportCard
import com.takaotech.ktravel.ui.plan.transport.component.TransportPlanningTestTags
import com.takaotech.ktravel.ui.plan.transport.composer.NavigatorBlockPreviewParams
import com.takaotech.ktravel.ui.theme.KTravelTheme
import ktravel.composeapp.generated.resources.Res
import ktravel.composeapp.generated.resources.planning_transport_navigator_version
import ktravel.composeapp.generated.resources.planning_transport_profiles_declared
import ktravel.composeapp.generated.resources.planning_transport_retry
import ktravel.composeapp.generated.resources.planning_transport_server_embedded
import ktravel.composeapp.generated.resources.planning_transport_server_embedded_hint
import ktravel.composeapp.generated.resources.planning_transport_server_remote
import ktravel.composeapp.generated.resources.planning_transport_server_section
import ktravel.composeapp.generated.resources.planning_transport_status_checking
import ktravel.composeapp.generated.resources.planning_transport_status_offline
import ktravel.composeapp.generated.resources.planning_transport_status_online
import org.jetbrains.compose.resources.stringResource

/** Which navigator computes this route, and whether it is answering. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun NavigatorBlock(
    uiState: TransportPlanningUiState,
    onNavigatorChange: (NavigatorKind) -> Unit,
    onRetryCatalog: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        SectionLabel(stringResource(Res.string.planning_transport_server_section))

        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
            SegmentedButton(
                modifier = Modifier.testTag(TransportPlanningTestTags.NAVIGATOR_EMBEDDED),
                selected = uiState.navigatorKind == NavigatorKind.EMBEDDED,
                onClick = { onNavigatorChange(NavigatorKind.EMBEDDED) },
                shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2),
            ) {
                Text(stringResource(Res.string.planning_transport_server_embedded))
            }
            SegmentedButton(
                modifier = Modifier.testTag(TransportPlanningTestTags.NAVIGATOR_REMOTE),
                selected = uiState.navigatorKind == NavigatorKind.REMOTE,
                onClick = { onNavigatorChange(NavigatorKind.REMOTE) },
                // Offered only when there is an address to call: switching to a navigator that was
                // never configured would fail in a way this screen cannot help with.
                enabled = uiState.isRemoteConfigured,
                shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2),
            ) {
                Text(stringResource(Res.string.planning_transport_server_remote))
            }
        }

        TransportCard(modifier = Modifier.padding(top = 10.dp)) {
            if (uiState.navigatorKind == NavigatorKind.REMOTE) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    ReachabilityPill(
                        isChecking = uiState.isCatalogLoading,
                        isReachable = uiState.catalog?.isReachable == true,
                        latencyMillis = uiState.catalog?.latencyMillis,
                        checkingText = stringResource(Res.string.planning_transport_status_checking),
                        reachableText = stringResource(Res.string.planning_transport_status_online),
                        unreachableText = stringResource(Res.string.planning_transport_status_offline),
                    )
                    TextButton(
                        modifier = Modifier.testTag(TransportPlanningTestTags.RETRY_CATALOG),
                        onClick = onRetryCatalog,
                    ) {
                        Text(stringResource(Res.string.planning_transport_retry))
                    }
                }
            } else {
                Text(
                    text = stringResource(Res.string.planning_transport_server_embedded_hint),
                    style = MaterialTheme.typography.bodyMedium,
                )
            }

            val version = uiState.catalog?.navigatorVersion
            val declared = uiState.catalog?.options?.size ?: 0
            Text(
                modifier = Modifier.padding(top = 10.dp),
                text = listOfNotNull(
                    version?.let {
                        stringResource(
                            Res.string.planning_transport_navigator_version,
                            it,
                        )
                    },
                    stringResource(Res.string.planning_transport_profiles_declared, declared),
                ).joinToString(" · "),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

//region Previews
@Preview(showBackground = true)
@Composable
private fun NavigatorBlockPreview(
    @PreviewParameter(NavigatorBlockPreviewParams::class) uiState: TransportPlanningUiState,
) = KTravelTheme {
    Surface {
        NavigatorBlock(
            uiState = uiState,
            onNavigatorChange = {},
            onRetryCatalog = {},
            modifier = Modifier
                .width(360.dp)
                .padding(16.dp),
        )
    }
}
//endregion Previews
