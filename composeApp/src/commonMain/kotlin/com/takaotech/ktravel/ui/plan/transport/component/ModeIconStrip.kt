package com.takaotech.ktravel.ui.plan.transport.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.takaotech.ktravel.domain.routing.RoutingMode
import com.takaotech.ktravel.ui.shared.format.iconOrNull
import kotlinx.collections.immutable.ImmutableList
import org.jetbrains.compose.resources.painterResource

/** The modes a profile can be asked for, as a row of small icons under its name. */
@Composable
internal fun ModeIconStrip(modes: ImmutableList<RoutingMode>, alpha: Float, modifier: Modifier = Modifier) {
    val icons = remember(modes) { modes.mapNotNull { it.iconOrNull() }.distinct() }
    if (icons.isNotEmpty()) {
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
}
