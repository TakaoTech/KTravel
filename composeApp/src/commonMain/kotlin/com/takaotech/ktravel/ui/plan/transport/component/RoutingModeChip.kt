package com.takaotech.ktravel.ui.plan.transport.component

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import com.takaotech.ktravel.domain.routing.RoutingMode
import com.takaotech.ktravel.ui.shared.format.iconOrNull
import com.takaotech.ktravel.ui.shared.format.labelOrNull
import com.takaotech.ktravel.ui.theme.KTravelTheme
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

/** One mode, selectable on its own or as part of a filter. */
@Composable
internal fun RoutingModeChip(
    mode: RoutingMode,
    selected: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val label = mode.labelOrNull()?.let { stringResource(it) } ?: mode.id
    val icon = mode.iconOrNull()

    FilterChip(
        modifier = modifier.testTag(TransportPlanningTestTags.modeTag(mode.id)),
        selected = selected,
        enabled = enabled,
        onClick = onClick,
        label = { Text(label) },
        leadingIcon = icon?.let {
            {
                Icon(
                    modifier = Modifier.size(FilterChipDefaults.IconSize),
                    painter = painterResource(it),
                    contentDescription = null,
                )
            }
        },
    )
}

//region Previews
@Preview(showBackground = true)
@Composable
private fun RoutingModeChipPreview(
    @PreviewParameter(
        RoutingModeChipPreviewParams::class,
    ) state: RoutingModeChipPreviewState,
) = KTravelTheme {
    Surface {
        RoutingModeChip(
            modifier = Modifier.padding(12.dp),
            mode = state.mode,
            selected = state.selected,
            enabled = state.enabled,
            onClick = {},
        )
    }
}
//endregion Previews
