package com.takaotech.ktravel.ui.settings.component

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewFontScale
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import com.takaotech.ktravel.presentation.settings.NavigatorReachability
import com.takaotech.ktravel.ui.theme.KTravelTheme

@PreviewFontScale
@Composable
private fun ReachabilityBadgePreview(
    @PreviewParameter(ReachabilityBadgePreviewParams::class) reachability: NavigatorReachability,
) = KTravelTheme {
    Surface {
        ReachabilityBadge(reachability = reachability, modifier = Modifier.padding(12.dp))
    }
}

/**
 * Every state the badge draws, including the two that share the neutral colours.
 *
 * Unknown and NotConfigured look alike apart from their label, which is the whole point of keeping
 * them apart: the preview is where that difference has to stay readable.
 */
internal class ReachabilityBadgePreviewParams : PreviewParameterProvider<NavigatorReachability> {
    override val values = sequenceOf(
        NavigatorReachability.Unknown,
        NavigatorReachability.Checking,
        NavigatorReachability.NotConfigured,
        NavigatorReachability.Unreachable,
        NavigatorReachability.Reachable(version = "1.4.0", latencyMillis = 42),
        // A slow answer is still an answer: the latency is the only thing that grows the pill.
        NavigatorReachability.Reachable(version = "1.4.0", latencyMillis = 2_431),
    )
}
