package com.takaotech.ktravel.ui.plan.transport.routepreview

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.takaotech.ktravel.domain.routing.model.TransitJourney
import com.takaotech.ktravel.ui.shared.route.transit.TransitStepBadge
import com.takaotech.ktravel.ui.shared.route.transit.clock
import io.nacular.measured.units.times
import ktravel.composeapp.generated.resources.Res
import ktravel.composeapp.generated.resources.transit_preview_changes
import ktravel.composeapp.generated.resources.transit_preview_no_change
import org.jetbrains.compose.resources.stringResource

/**
 * The journey at a glance: when it leaves, when it lands, how long that is, and how many changes.
 *
 * The duration shown is arrival minus departure and not the sum of the steps. The two differ by
 * every minute spent waiting on a platform, and a traveller deciding whether they have time for
 * this is asking about the first number.
 */
@Composable
internal fun TransitJourneySummaryCard(journey: TransitJourney, modifier: Modifier = Modifier) {
    Card(modifier = modifier.fillMaxWidth().padding(16.dp)) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = listOfNotNull(journey.departure?.clock(), journey.arrival?.clock())
                        .joinToString(" → "),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
                journey.totalDuration?.let {
                    Text(text = it.toString(), style = MaterialTheme.typography.titleMedium)
                }
            }

            Text(
                text = if (journey.changes == 0) {
                    stringResource(Res.string.transit_preview_no_change)
                } else {
                    stringResource(Res.string.transit_preview_changes, journey.changes)
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                journey.steps.forEach { step -> TransitStepBadge(step) }
            }
        }
    }
}
