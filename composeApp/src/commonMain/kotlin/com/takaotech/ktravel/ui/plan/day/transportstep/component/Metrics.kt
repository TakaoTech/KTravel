package com.takaotech.ktravel.ui.plan.day.transportstep.component

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import com.takaotech.ktravel.ui.plan.day.transportstep.TransportStepTestTags
import com.takaotech.ktravel.ui.shared.format.formatDayMonthYearClockHere
import com.takaotech.ktravel.ui.shared.format.formatDistance
import com.takaotech.ktravel.ui.theme.KTravelTheme
import ktravel.composeapp.generated.resources.Res
import ktravel.composeapp.generated.resources.map
import ktravel.composeapp.generated.resources.schedule
import ktravel.composeapp.generated.resources.transport_detail_calculated_at
import ktravel.composeapp.generated.resources.transport_detail_distance_label
import ktravel.composeapp.generated.resources.transport_detail_duration_label
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import com.takaotech.ktravel.presentation.plan.day.TransportStepUi as TransportStepUiModel

/** How long and how far, and — when the plan knows it — when the answer was computed. */
@Composable
internal fun Metrics(transport: TransportStepUiModel, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(16.dp))
            .testTag(TransportStepTestTags.METRICS),
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            Metric(
                modifier = Modifier.weight(1f),
                icon = Res.drawable.schedule,
                value = transport.totalDuration.toString(),
                label = stringResource(Res.string.transport_detail_duration_label),
            )
            Metric(
                modifier = Modifier.weight(1f),
                icon = Res.drawable.map,
                value = transport.totalDistance.formatDistance(),
                label = stringResource(Res.string.transport_detail_distance_label),
            )
        }

        transport.calculatedAt?.let { calculatedAt ->
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Text(
                modifier = Modifier
                    .padding(horizontal = 14.dp, vertical = 9.dp)
                    .testTag(TransportStepTestTags.CALCULATED_AT),
                text = stringResource(
                    Res.string.transport_detail_calculated_at,
                    calculatedAt.formatDayMonthYearClockHere(),
                ),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
internal fun Metric(icon: DrawableResource, value: String, label: String, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Icon(
            modifier = Modifier.size(20.dp),
            painter = painterResource(icon),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
        )
        Column {
            Text(text = value, style = MaterialTheme.typography.titleMedium)
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

//region Previews

@Preview(showBackground = true)
@Composable
private fun MetricsPreview(@PreviewParameter(MetricsPreviewParams::class) transport: TransportStepUiModel) =
    KTravelTheme {
        Surface {
            Metrics(transport = transport, modifier = Modifier.padding(12.dp))
        }
    }

/**
 * A leg whose computation is dated, and one filed before that was recorded.
 *
 * The date is a whole second row, divider included, so its absence is not a missing line of text
 * but a different block — and it is the shape every leg in an older plan still has.
 */
internal class MetricsPreviewParams : PreviewParameterProvider<TransportStepUiModel> {
    override val values = sequenceOf(
        previewRoadStep(),
        previewRoadStep(calculatedAt = null),
    )
}

@Preview(showBackground = true)
@Composable
private fun MetricPreview() = KTravelTheme {
    Surface {
        Metric(
            modifier = Modifier.padding(12.dp),
            icon = Res.drawable.schedule,
            value = "25m",
            label = stringResource(Res.string.transport_detail_duration_label),
        )
    }
}
//endregion Previews
