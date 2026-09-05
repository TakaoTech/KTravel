package com.takaotech.ktravel.ui.plan.transport.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import com.takaotech.ktravel.ui.theme.KTravelTheme
import ktravel.composeapp.generated.resources.Res
import ktravel.composeapp.generated.resources.planning_transport_status_checking
import ktravel.composeapp.generated.resources.planning_transport_status_offline
import ktravel.composeapp.generated.resources.planning_transport_status_online
import org.jetbrains.compose.resources.stringResource

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
        modifier = modifier.testTag(TransportPlanningTestTags.REACHABILITY),
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

//region Previews
@Preview(showBackground = true)
@Composable
private fun ReachabilityPillPreview(
    @PreviewParameter(ReachabilityPillPreviewParams::class) state: ReachabilityPillPreviewState,
) = KTravelTheme {
    Surface {
        ReachabilityPill(
            modifier = Modifier.padding(12.dp),
            isChecking = state.isChecking,
            isReachable = state.isReachable,
            latencyMillis = state.latencyMillis,
            checkingText = stringResource(Res.string.planning_transport_status_checking),
            reachableText = stringResource(Res.string.planning_transport_status_online),
            unreachableText = stringResource(Res.string.planning_transport_status_offline),
        )
    }
}
//endregion Previews
