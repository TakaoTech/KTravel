package com.takaotech.ktravel.ui.planning.transport

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.takaotech.ktravel.domain.routing.RoutingMode
import com.takaotech.ktravel.domain.routing.RoutingProfileOption
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

/** Test tags for the transport screen; the host tests resolve strings in Italian, so nodes are found by tag. */
internal object PlanningTransportTestTags {
    const val NAVIGATOR_EMBEDDED = "transport_navigator_embedded"
    const val NAVIGATOR_REMOTE = "transport_navigator_remote"
    const val REACHABILITY = "transport_reachability"
    const val RETRY_CATALOG = "transport_retry_catalog"
    const val CALCULATE = "transport_calculate"
    const val TIME_NOW = "transport_time_now"
    const val TIME_NOW_INFO = "transport_time_now_info"
    const val TIME_NOW_TOOLTIP = "transport_time_now_tooltip"
    const val TIME_DEPART_AT = "transport_time_depart_at"
    const val TIME_ARRIVE_BY = "transport_time_arrive_by"
    const val TIME_VALUE = "transport_time_value"
    const val SHORTEST = "transport_shortest"
    const val ALTERNATIVES = "transport_alternatives"
    const val CHANGES_LIMIT = "transport_changes_limit"
    const val CHANGES_SLIDER = "transport_changes_slider"
    const val WALK_DISTANCE_LIMIT = "transport_walk_distance_limit"
    const val WALK_DISTANCE_SLIDER = "transport_walk_distance_slider"
    const val FAILURE = "transport_failure"

    fun profileTag(provider: String, profile: String): String = "transport_profile_${provider}_$profile"

    fun modeTag(modeId: String): String = "transport_mode_$modeId"

    fun avoidTag(featureName: String): String = "transport_avoid_$featureName"

    fun walkingPaceTag(paceName: String): String = "transport_walking_pace_$paceName"
}

/** The uppercase eyebrow that opens each block. */
@Composable
internal fun SectionLabel(text: String, modifier: Modifier = Modifier, trailing: String? = null) {
    Row(
        modifier = modifier.fillMaxWidth().padding(bottom = 8.dp, start = 2.dp),
        verticalAlignment = Alignment.Bottom,
    ) {
        Text(
            text = text.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        if (trailing != null) {
            Text(
                modifier = Modifier.weight(1f),
                text = trailing,
                textAlign = TextAlign.End,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

/** Whether a navigator answered, and how quickly. */
@Composable
internal fun ReachabilityPill(
    isChecking: Boolean,
    isReachable: Boolean,
    latencyMillis: Long?,
    checkingText: String,
    reachableText: String,
    unreachableText: String,
    modifier: Modifier = Modifier,
) {
    val scheme = MaterialTheme.colorScheme
    val (container, content, text) = when {
        isChecking -> Triple(scheme.surfaceContainerHighest, scheme.onSurfaceVariant, checkingText)
        isReachable -> Triple(scheme.primaryContainer, scheme.onPrimaryContainer, reachableText)
        else -> Triple(scheme.errorContainer, scheme.onErrorContainer, unreachableText)
    }
    val dot = when {
        isChecking -> scheme.outline
        isReachable -> scheme.primary
        else -> scheme.error
    }

    Card(
        modifier = modifier.testTag(PlanningTransportTestTags.REACHABILITY),
        shape = CircleShape,
        colors = CardDefaults.cardColors(
            containerColor = container,
            contentColor = content,
        ),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 11.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(7.dp),
        ) {
            Box(
                modifier = Modifier.size(8.dp)
                    .background(
                        color = dot,
                        shape = CircleShape,
                    ),
            ) {}
            Text(
                text = if (isReachable && latencyMillis != null) "$text · $latencyMillis ms" else text,
                style = MaterialTheme.typography.labelMedium,
            )
        }
    }
}

/**
 * One profile in the selector.
 *
 * A row rather than an entry in a dropdown because it has to carry a reason when it cannot be picked:
 * a menu item that is simply missing tells the traveller the profile does not exist, when the truth
 * is usually that this navigator does not serve it or this trip has no key for it.
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
                PlanningTransportTestTags.profileTag(
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
                ModeIconStrip(modes = option.profile.options.modes, alpha = alpha)
            }
        }
    }
}

/** The modes a profile can be asked for, as a row of small icons under its name. */
@Composable
private fun ModeIconStrip(modes: List<RoutingMode>, alpha: Float, modifier: Modifier = Modifier) {
    val icons = modes.mapNotNull { it.iconOrNull() }.distinct()
    if (icons.isEmpty()) return

    Row(
        modifier = modifier.padding(top = 7.dp),
        horizontalArrangement = Arrangement.spacedBy(5.dp),
    ) {
        icons.forEach { icon ->
            Icon(
                modifier = Modifier.size(17.dp),
                painter = painterResource(icon),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = alpha * MODE_ICON_ALPHA),
            )
        }
    }
}

/** One mode, selectable on its own or as part of a filter. */
@Composable
internal fun ModeChip(
    mode: RoutingMode,
    selected: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val label = mode.labelOrNull()?.let { stringResource(it) } ?: mode.id
    val icon = mode.iconOrNull()

    FilterChip(
        modifier = modifier.testTag(PlanningTransportTestTags.modeTag(mode.id)),
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

/** A card holding one block's detail, in the container colour the blocks share. */
@Composable
internal fun TransportCard(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, MaterialTheme.shapes.large),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        ),
        content = { Column(modifier = Modifier.padding(14.dp), content = content) },
    )
}

/** One of the two places this leg runs between. */
@Composable
internal fun PlaceEndpoint(label: String, name: String, icon: DrawableResource, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 11.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Icon(
                painter = painterResource(icon),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
            )
            Column {
                Text(
                    text = label.uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(text = name, style = MaterialTheme.typography.bodyLarge, maxLines = 1)
            }
        }
    }
}

/** A switch row with a title and a line explaining what it does, or why it is unavailable. */
@Composable
internal fun ToggleRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    enabled: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = LocalContentColorOf(enabled),
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
                    .copy(alpha = if (enabled) 1f else DISABLED_ALPHA),
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            enabled = enabled,
        )
    }
}

@Composable
private fun LocalContentColorOf(enabled: Boolean): Color =
    MaterialTheme.colorScheme.onSurface.copy(alpha = if (enabled) 1f else DISABLED_ALPHA)

/** Material's own disabled opacity, used for anything the screen dims rather than hides. */
private const val DISABLED_ALPHA = 0.38f

/** The mode strip is a hint, not a control, so it sits below the text it belongs to. */
private const val MODE_ICON_ALPHA = 0.85f
