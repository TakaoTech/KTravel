package com.takaotech.ktravel.ui.plan.day.transportstep.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import com.takaotech.ktravel.ui.plan.day.transportstep.TransportStepTestTags
import com.takaotech.ktravel.ui.theme.KTravelTheme
import io.nacular.measured.units.times
import ktravel.composeapp.generated.resources.Res
import ktravel.composeapp.generated.resources.flag
import ktravel.composeapp.generated.resources.place
import ktravel.composeapp.generated.resources.transport_detail_arrival_label
import ktravel.composeapp.generated.resources.transport_detail_departure_label
import org.jetbrains.compose.resources.stringResource
import com.takaotech.ktravel.presentation.plan.day.TransportStepUi as TransportStepUiModel

/**
 * Where the leg starts and ends, and when.
 *
 * Two rows rather than one line: the times belong to the endpoints and reading them off a single
 * `A → B` row means guessing which one each belongs to. Between them, the vehicle that covers the
 * distance.
 */
@Composable
internal fun SegmentHead(transport: TransportStepUiModel, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.testTag(TransportStepTestTags.SEGMENT_HEAD),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Endpoint(
            icon = Res.drawable.place,
            tint = MaterialTheme.colorScheme.primary,
            label = stringResource(Res.string.transport_detail_departure_label),
            name = transport.fromName,
            time = transport.departure,
        )

        Row(
            modifier = Modifier.padding(start = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Box(
                modifier = Modifier
                    .width(2.dp)
                    .height(16.dp)
                    .background(MaterialTheme.colorScheme.outlineVariant),
            )
            ModeChip(transport)
        }

        Endpoint(
            icon = Res.drawable.flag,
            tint = MaterialTheme.colorScheme.tertiary,
            label = stringResource(Res.string.transport_detail_arrival_label),
            name = transport.toName,
            time = transport.arrival,
        )
    }
}

//region Previews

@Preview(showBackground = true)
@Composable
private fun SegmentHeadPreview(@PreviewParameter(SegmentHeadPreviewParams::class) transport: TransportStepUiModel) =
    KTravelTheme {
        Surface {
            SegmentHead(transport = transport, modifier = Modifier.padding(12.dp))
        }
    }

/**
 * The head of a road leg, of a journey, and of a leg the navigator returned no times for.
 *
 * The untimed one is the state the two rows were chosen for: with `--:--` at both ends the times
 * carry no meaning, and what has to stay readable is which name is the departure and which the
 * arrival.
 */
internal class SegmentHeadPreviewParams : PreviewParameterProvider<TransportStepUiModel> {
    override val values = sequenceOf(
        previewRoadStep(),
        previewTransitStep(),
        previewRoadStep(departure = null, arrival = null),
    )
}
//endregion Previews
