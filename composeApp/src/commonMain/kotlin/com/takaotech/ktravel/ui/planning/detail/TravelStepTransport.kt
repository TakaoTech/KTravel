package com.takaotech.ktravel.ui.planning.detail

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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.takaotech.ktravel.core.ui.component.PillChip
import com.takaotech.ktravel.core.ui.component.PillChipSize
import com.takaotech.ktravel.domain.model.TransportType
import com.takaotech.ktravel.domain.routing.model.RouteSummary
import com.takaotech.ktravel.domain.routing.model.RoutingRoute
import com.takaotech.ktravel.domain.routing.model.TransportAnswer
import com.takaotech.ktravel.presentation.planning.StepUi
import com.takaotech.ktravel.ui.planning.transport.preview.formatDistance
import com.takaotech.ktravel.ui.theme.KTravelTheme
import io.nacular.measured.units.Length
import io.nacular.measured.units.times
import ktravel.composeapp.generated.resources.Res
import ktravel.composeapp.generated.resources.add
import ktravel.composeapp.generated.resources.delete
import ktravel.composeapp.generated.resources.directions_bus
import ktravel.composeapp.generated.resources.directions_car
import ktravel.composeapp.generated.resources.flight
import ktravel.composeapp.generated.resources.planning_detail_add_transport
import ktravel.composeapp.generated.resources.planning_detail_cd_delete_step
import ktravel.composeapp.generated.resources.planning_detail_transport_duration
import ktravel.composeapp.generated.resources.planning_detail_transport_summary
import ktravel.composeapp.generated.resources.train
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import kotlin.time.Duration.Companion.minutes

fun TransportType.toIcon(): DrawableResource = when (this) {
    TransportType.TRAIN -> Res.drawable.train
    TransportType.BUS -> Res.drawable.directions_bus
    TransportType.CAR -> Res.drawable.directions_car
    TransportType.FLIGHT -> Res.drawable.flight
}

/**
 * Content (right-hand side of the timeline) of a transport: a compact connector between two places.
 * The vehicle icon is rendered in the gutter node by the calling timeline row; the pill here reads
 * how long the leg takes and how far it goes, next to the delete action.
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

        // TODO Add Dep and Arr time

        Text(
            modifier = Modifier
                .weight(1f, fill = false)
                .testTag(StepsPaneTestTags.TRANSPORT_DURATION),
            text = stringResource(
                Res.string.planning_detail_transport_summary,
                duration,
                step.answer.summary.distance.formatDistance(),
            ),
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

@PreviewLightDark
@Composable
private fun TravelStepTransportPreview() = KTravelTheme {
    Surface {
        val summary = RouteSummary(
            durationSeconds = 42.minutes,
            distance = 18_500.0 * Length.meters,
        )
        TravelStepTransport(
            step = StepUi.Transport(
                type = TransportType.TRAIN,
                answer = TransportAnswer.Routing(
                    RoutingRoute(summary = summary, sections = emptyList()),
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
