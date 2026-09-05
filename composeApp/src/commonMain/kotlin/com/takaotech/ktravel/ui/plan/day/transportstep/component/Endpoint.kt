package com.takaotech.ktravel.ui.plan.day.transportstep.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import com.takaotech.ktravel.ui.shared.format.formatClock
import com.takaotech.ktravel.ui.theme.KTravelTheme
import io.nacular.measured.units.times
import kotlinx.datetime.LocalDateTime
import ktravel.composeapp.generated.resources.Res
import ktravel.composeapp.generated.resources.flag
import ktravel.composeapp.generated.resources.place
import ktravel.composeapp.generated.resources.transport_detail_arrival_label
import ktravel.composeapp.generated.resources.transport_detail_departure_label
import ktravel.composeapp.generated.resources.transport_detail_time_unknown
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun Endpoint(icon: DrawableResource, tint: Color, label: String, name: String, time: LocalDateTime?) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Icon(
            modifier = Modifier.size(18.dp),
            painter = painterResource(icon),
            contentDescription = null,
            tint = tint,
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = name,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Text(
            text = time?.formatClock() ?: stringResource(Res.string.transport_detail_time_unknown),
            style = MaterialTheme.typography.titleMedium,
            color = if (time != null) {
                MaterialTheme.colorScheme.onSurface
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            },
        )
    }
}

//region Previews

@Preview(showBackground = true)
@Composable
private fun EndpointPreview(@PreviewParameter(EndpointPreviewParams::class) state: EndpointPreviewState) =
    KTravelTheme {
        Surface {
            Column(
                modifier = Modifier
                    .width(320.dp)
                    .padding(12.dp),
            ) {
                val label = if (state.isArrival) {
                    Res.string.transport_detail_arrival_label
                } else {
                    Res.string.transport_detail_departure_label
                }

                Endpoint(
                    icon = if (state.isArrival) Res.drawable.flag else Res.drawable.place,
                    tint = if (state.isArrival) {
                        MaterialTheme.colorScheme.tertiary
                    } else {
                        MaterialTheme.colorScheme.primary
                    },
                    label = stringResource(label),
                    name = state.name,
                    time = state.time,
                )
            }
        }
    }

/** One end of a leg: which end it is, the place it names, and the moment it happens. */
internal data class EndpointPreviewState(val isArrival: Boolean, val name: String, val time: LocalDateTime?)

/**
 * The endpoint as a departure and as an arrival, with the time missing, and with a name too long.
 *
 * The two ends are not interchangeable: icon, tint and label always travel together, so the preview
 * keeps the two combinations [SegmentHead] builds rather than crossing them. The missing time is the only state that
 * changes the colour of the clock, and the long name is where the row has to ellipsize instead of
 * pushing the time off its end.
 */
internal class EndpointPreviewParams : PreviewParameterProvider<EndpointPreviewState> {
    override val values = sequenceOf(
        EndpointPreviewState(isArrival = false, name = "Kyoto Station", time = previewTime(hour = 9, minute = 12)),
        EndpointPreviewState(
            isArrival = true,
            name = "Fushimi Inari Taisha",
            time = previewTime(hour = 9, minute = 37),
        ),
        EndpointPreviewState(isArrival = true, name = "Fushimi Inari Taisha", time = null),
        EndpointPreviewState(
            isArrival = false,
            name = "Kyoto Station Karasuma central exit, bus terminal D2",
            time = previewTime(hour = 9, minute = 12),
        ),
    )
}
//endregion Previews
