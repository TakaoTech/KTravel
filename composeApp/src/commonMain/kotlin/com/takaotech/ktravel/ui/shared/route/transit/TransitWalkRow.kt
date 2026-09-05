package com.takaotech.ktravel.ui.shared.route.transit

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.takaotech.ktravel.domain.routing.model.TransitStep
import com.takaotech.ktravel.ui.shared.format.formatDistance
import io.nacular.measured.units.times
import ktravel.composeapp.generated.resources.Res
import ktravel.composeapp.generated.resources.transit_preview_walk
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun TransitWalkRow(step: TransitStep.Walk, modifier: Modifier = Modifier) {
    // TODO Add support

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        TransitTimelineRail(color = MaterialTheme.colorScheme.outlineVariant, width = 3.dp)
        Text(
            text = stringResource(
                Res.string.transit_preview_walk,
                step.summary.durationSeconds.toString(),
            ),
            modifier = Modifier.padding(start = 12.dp),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = " · ${step.summary.distance.formatDistance()}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
