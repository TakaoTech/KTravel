package com.takaotech.ktravel.ui.plan.transport.composer.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import com.takaotech.ktravel.domain.navigator.NavigatorKind
import com.takaotech.ktravel.domain.routing.RoutingProfileId
import com.takaotech.ktravel.presentation.plan.transport.TransportPlanningUiState
import com.takaotech.ktravel.ui.plan.transport.component.ProfileRow
import com.takaotech.ktravel.ui.plan.transport.component.SectionLabel
import com.takaotech.ktravel.ui.plan.transport.composer.ProfileBlockPreviewParams
import com.takaotech.ktravel.ui.theme.KTravelTheme
import ktravel.composeapp.generated.resources.Res
import ktravel.composeapp.generated.resources.planning_transport_catalog_failed
import ktravel.composeapp.generated.resources.planning_transport_catalog_loading
import ktravel.composeapp.generated.resources.planning_transport_profiles_available
import ktravel.composeapp.generated.resources.planning_transport_profiles_none
import ktravel.composeapp.generated.resources.planning_transport_profiles_section
import ktravel.composeapp.generated.resources.planning_transport_use_embedded
import org.jetbrains.compose.resources.stringResource

/** Which profile of which provider, including the ones that cannot be picked and why. */
@Composable
internal fun ProfileBlock(
    uiState: TransportPlanningUiState,
    onProfileChange: (RoutingProfileId) -> Unit,
    onUseEmb: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val catalog = uiState.catalog
    val unreachable = catalog != null && !catalog.isReachable

    Column(modifier = modifier) {
        SectionLabel(
            text = stringResource(Res.string.planning_transport_profiles_section),
            trailing = when {
                catalog == null || unreachable -> stringResource(Res.string.planning_transport_profiles_none)

                else -> stringResource(
                    Res.string.planning_transport_profiles_available,
                    catalog.selectable.size,
                )
            },
        )

        when {
            uiState.isCatalogLoading && catalog == null -> Text(
                text = stringResource(Res.string.planning_transport_catalog_loading),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            unreachable -> Card(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.large,
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.onErrorContainer,
                ),
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                ) {
                    Text(
                        text = stringResource(Res.string.planning_transport_catalog_failed),
                        style = MaterialTheme.typography.bodyMedium,
                    )

                    if (uiState.navigatorKind == NavigatorKind.REMOTE) {
                        TextButton(onClick = onUseEmb) {
                            Text(stringResource(Res.string.planning_transport_use_embedded))
                        }
                    }
                }
            }

            catalog != null -> Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                catalog.options.forEach { option ->
                    ProfileRow(
                        option = option,
                        selected = option.profile.id == uiState.selectedProfileId,
                        onSelect = { onProfileChange(option.profile.id) },
                    )
                }
            }
        }
    }
}

//region Previews
@Preview(showBackground = true)
@Composable
private fun ProfileBlockPreview(
    @PreviewParameter(ProfileBlockPreviewParams::class) uiState: TransportPlanningUiState,
) = KTravelTheme {
    Surface {
        ProfileBlock(
            uiState = uiState,
            onProfileChange = {},
            onUseEmb = {},
            modifier = Modifier
                .width(360.dp)
                .padding(16.dp),
        )
    }
}
//endregion Previews
