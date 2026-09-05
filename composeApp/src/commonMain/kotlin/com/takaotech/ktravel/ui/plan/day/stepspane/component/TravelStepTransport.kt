package com.takaotech.ktravel.ui.plan.day.stepspane.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.takaotech.ktravel.domain.model.TransportType
import com.takaotech.ktravel.domain.routing.model.RouteDeparture
import com.takaotech.ktravel.domain.routing.model.RouteLocation
import com.takaotech.ktravel.domain.routing.model.RouteSummary
import com.takaotech.ktravel.domain.routing.model.RoutingRoute
import com.takaotech.ktravel.domain.routing.model.RoutingSection
import com.takaotech.ktravel.domain.routing.model.TransportAnswer
import com.takaotech.ktravel.presentation.plan.StepUi
import com.takaotech.ktravel.ui.plan.day.stepspane.StepsPaneTestTags
import com.takaotech.ktravel.ui.shared.component.PillChip
import com.takaotech.ktravel.ui.shared.component.PillChipSize
import com.takaotech.ktravel.ui.shared.format.formatClock
import com.takaotech.ktravel.ui.shared.format.formatDistance
import com.takaotech.ktravel.ui.theme.KTravelTheme
import io.nacular.measured.units.Length
import io.nacular.measured.units.times
import kotlinx.datetime.format.DateTimeComponents
import ktravel.composeapp.generated.resources.Res
import ktravel.composeapp.generated.resources.add
import ktravel.composeapp.generated.resources.delete
import ktravel.composeapp.generated.resources.planning_detail_add_transport
import ktravel.composeapp.generated.resources.planning_detail_cd_delete_step
import ktravel.composeapp.generated.resources.planning_detail_cd_transport_times
import ktravel.composeapp.generated.resources.planning_detail_transport_duration
import ktravel.composeapp.generated.resources.planning_detail_transport_summary
import ktravel.composeapp.generated.resources.planning_detail_transport_times
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import kotlin.time.Duration.Companion.minutes

/**
 * Content (right-hand side of the timeline) of a transport: a compact connector between two places.
 * The vehicle icon is rendered in the gutter node by the calling timeline row; the pill here reads
 * when the leg leaves and lands, how long it takes and how far it goes, next to the delete action.
 *
 * The two clocks are only written when the saved answer times both ends: a leg computed for "now"
 * carries no timetable, and the row then reads the way it always did.
 *
 * Clicking it opens the leg, the way clicking a place opens the place: the route saved into the
 * plan is worth reading back, and it is the only place its notes can be written.
 */
@Composable
fun TravelStepTransport(
    step: StepUi.Transport,
    onStepClick: (String) -> Unit,
    onStepDeleteClicked: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .clip(CircleShape)
            .clickable(interactionSource = null, indication = ripple()) { onStepClick(step.id) }
            .heightIn(min = 32.dp)
            .padding(start = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        val duration = stringResource(
            Res.string.planning_detail_transport_duration,
            step.totalDuration.toString(),
        )
        val metrics = stringResource(
            Res.string.planning_detail_transport_summary,
            duration,
            step.answer.summary.distance.formatDistance(),
        )

        // Both ends or neither: a lone clock on the row cannot be told apart from an arrival.
        val timed = step.departure != null && step.arrival != null
        val departureClock = step.departure?.formatClock().orEmpty()
        val arrivalClock = step.arrival?.formatClock().orEmpty()
        val times = stringResource(
            Res.string.planning_detail_transport_times,
            departureClock,
            arrivalClock,
        ).takeIf { timed }

        // The arrow reads as "right arrow" to a screen reader, so the two moments are spelled out.
        val timesDescription = stringResource(
            Res.string.planning_detail_cd_transport_times,
            departureClock,
            arrivalClock,
        ).takeIf { timed }

        Text(
            modifier = Modifier
                .weight(1f, fill = false)
                .testTag(StepsPaneTestTags.TRANSPORT_DURATION)
                .semantics {
                    timesDescription?.let { contentDescription = "$it, $metrics" }
                },
            text = if (times != null) {
                stringResource(Res.string.planning_detail_transport_summary, times, metrics)
            } else {
                metrics
            },
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )

        Spacer(Modifier.width(4.dp))

        IconButton(
            modifier = Modifier.testTag(StepsPaneTestTags.deleteStepTag(step.id)),
            onClick = onStepDeleteClicked,
        ) {
            Icon(
                painter = painterResource(Res.drawable.delete),
                contentDescription = stringResource(Res.string.planning_detail_cd_delete_step),
            )
        }
    }
}

/**
 * Empty slot between two places: a dashed pill invites the transport that is still missing, the way
 * the dashed rail node does on the timeline.
 */
@Composable
fun TravelTransportStepAdd(modifier: Modifier = Modifier, onClick: () -> Unit) {
    PillChip(
        modifier = modifier,
        text = stringResource(Res.string.planning_detail_add_transport),
        icon = Res.drawable.add,
        onClick = onClick,
        size = PillChipSize.Medium,
        contentAlignment = Alignment.CenterStart,
    )
}

//region Previews
@PreviewLightDark
@Composable
private fun TravelStepTransportPreview() = KTravelTheme {
    Surface {
        TravelStepTransport(
            step = timedPreviewStep(),
            onStepClick = {},
            onStepDeleteClicked = {},
        )
    }
}

@PreviewLightDark
@Composable
private fun TravelStepTransportUntimedPreview() = KTravelTheme {
    Surface {
        TravelStepTransport(
            step = StepUi.Transport(
                type = TransportType.TRAIN,
                answer = TransportAnswer.Routing(
                    RoutingRoute(summary = previewSummary(), sections = emptyList()),
                ),
            ),
            onStepClick = {},
            onStepDeleteClicked = {},
        )
    }
}

@PreviewLightDark
@Composable
private fun TravelTransportStepAddPreview() = KTravelTheme {
    Surface {
        TravelTransportStepAdd(onClick = {})
    }
}

internal fun previewSummary() = RouteSummary(
    durationSeconds = 42.minutes,
    distance = 18_500.0 * Length.meters,
)

/** A leg the navigator dated at both ends, which is what the row shows the two clocks for. */
internal fun timedPreviewStep() = StepUi.Transport(
    type = TransportType.TRAIN,
    answer = TransportAnswer.Routing(
        RoutingRoute(
            summary = previewSummary(),
            sections = listOf(
                RoutingSection(
                    summary = previewSummary(),
                    mode = "train",
                    departure = previewWaypoint("2026-05-18T09:30:00+02:00"),
                    arrival = previewWaypoint("2026-05-18T10:12:00+02:00"),
                ),
            ),
        ),
    ),
)

internal fun previewWaypoint(isoWithOffset: String) = RouteDeparture(
    location = RouteLocation(lat = 0.0, lng = 0.0),
    time = DateTimeComponents.Formats.ISO_DATE_TIME_OFFSET.parse(isoWithOffset),
)
//endregion Previews
