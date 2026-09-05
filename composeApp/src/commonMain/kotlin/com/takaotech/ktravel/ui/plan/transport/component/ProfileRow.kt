package com.takaotech.ktravel.ui.plan.transport.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import com.takaotech.ktravel.domain.routing.RoutingProfileOption
import com.takaotech.ktravel.ui.shared.format.descriptionOrNull
import com.takaotech.ktravel.ui.shared.format.labelOrNull
import com.takaotech.ktravel.ui.shared.format.reasonOrNull
import com.takaotech.ktravel.ui.theme.KTravelTheme
import kotlinx.collections.immutable.toPersistentList
import org.jetbrains.compose.resources.stringResource

/**
 * One profile in the selector.
 *
 * A row rather than an entry in a dropdown because it has to carry a reason when it cannot be
 * picked: a menu item that is simply missing tells the traveller the profile does not exist, when
 * the truth is usually that this navigator does not serve it or this trip has no key for it.
 */
@Composable
internal fun ProfileRow(
    option: RoutingProfileOption,
    selected: Boolean,
    onSelect: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val enabled = option.isSelectable
    val scheme = MaterialTheme.colorScheme
    val label =
        option.profile.id.labelOrNull()?.let { stringResource(it) } ?: option.profile.displayName
    val description = option.profile.id.descriptionOrNull()?.let { stringResource(it) }
    val reason = option.availability.reasonOrNull()?.let { stringResource(it) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag(
                TransportPlanningTestTags.profileTag(
                    option.profile.id.provider,
                    option.profile.id.profile,
                ),
            )
            .selectable(
                selected = selected,
                enabled = enabled,
                role = Role.RadioButton,
                onClick = onSelect,
            ),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = if (selected) scheme.primaryContainer else scheme.surfaceContainerLow,
            contentColor = if (selected) scheme.onPrimaryContainer else scheme.onSurface,
        ),
        border = if (selected) {
            null
        } else {
            androidx.compose.foundation.BorderStroke(
                1.dp,
                scheme.outlineVariant,
            )
        },
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            // Disabled rows are dimmed rather than hidden: the reason underneath is the whole point.
            val alpha = if (enabled) 1f else DISABLED_ALPHA

            RadioButton(selected = selected, onClick = null, enabled = enabled)

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.titleSmall,
                    color = LocalContentColorOf(enabled),
                )
                if (description != null) {
                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodySmall,
                        color = scheme.onSurfaceVariant.copy(alpha = alpha),
                    )
                }
                if (reason != null) {
                    Text(
                        modifier = Modifier.padding(top = 2.dp),
                        text = reason,
                        style = MaterialTheme.typography.bodySmall,
                        color = scheme.error,
                    )
                }

                val modes = remember {
                    option.profile.options.modes.toPersistentList()
                }

                ModeIconStrip(modes = modes, alpha = alpha)
            }
        }
    }
}

//region Previews
@Preview(showBackground = true)
@Composable
private fun ProfileRowPreview(@PreviewParameter(ProfileRowPreviewParams::class) state: ProfileRowPreviewState) =
    KTravelTheme {
        Surface {
            ProfileRow(
                modifier = Modifier.padding(12.dp),
                option = state.option,
                selected = state.selected,
                onSelect = {},
            )
        }
    }
//endregion Previews
