package com.takaotech.ktravel.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.takaotech.ktravel.presentation.settings.NavigatorReachability
import ktravel.composeapp.generated.resources.Res
import ktravel.composeapp.generated.resources.planning_transport_status_checking
import ktravel.composeapp.generated.resources.planning_transport_status_offline
import ktravel.composeapp.generated.resources.planning_transport_status_online
import ktravel.composeapp.generated.resources.settings_navigator_not_checked
import ktravel.composeapp.generated.resources.settings_navigator_not_configured
import org.jetbrains.compose.resources.stringResource

/**
 * Whether the navigator at the configured address answers.
 *
 * Four states rather than two, because "not checked yet" and "no address configured" are things the
 * traveller can act on and a plain red dot for either would be a lie: nothing is wrong in the first
 * case, and nothing has been asked in the second.
 */
@Composable
fun ReachabilityBadge(reachability: NavigatorReachability, modifier: Modifier = Modifier) {
    val scheme = MaterialTheme.colorScheme

    val (container, content, dot) = when (reachability) {
        is NavigatorReachability.Reachable -> Triple(scheme.primaryContainer, scheme.onPrimaryContainer, scheme.primary)
        NavigatorReachability.Unreachable -> Triple(scheme.errorContainer, scheme.onErrorContainer, scheme.error)
        else -> Triple(scheme.surfaceContainerHighest, scheme.onSurfaceVariant, scheme.outline)
    }

    val text = when (reachability) {
        NavigatorReachability.Unknown -> stringResource(Res.string.settings_navigator_not_checked)

        NavigatorReachability.Checking -> stringResource(Res.string.planning_transport_status_checking)

        NavigatorReachability.NotConfigured -> stringResource(Res.string.settings_navigator_not_configured)

        NavigatorReachability.Unreachable -> stringResource(Res.string.planning_transport_status_offline)

        is NavigatorReachability.Reachable ->
            "${stringResource(Res.string.planning_transport_status_online)} · ${reachability.latencyMillis} ms"
    }

    Surface(modifier = modifier, shape = CircleShape, color = container, contentColor = content) {
        Row(
            modifier = Modifier.padding(horizontal = 11.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(7.dp),
        ) {
            Dot(color = dot)
            Text(text = text, style = MaterialTheme.typography.labelMedium)
        }
    }
}

@Composable
private fun Dot(color: Color, modifier: Modifier = Modifier) {
    Surface(modifier = modifier.size(8.dp), shape = CircleShape, color = color) {}
}
