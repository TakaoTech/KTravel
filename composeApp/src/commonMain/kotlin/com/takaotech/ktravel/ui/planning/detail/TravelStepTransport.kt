package com.takaotech.ktravel.ui.planning.detail

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.takaotech.ktravel.domain.model.TransportType
import com.takaotech.ktravel.presentation.planning.StepUi
import ktravel.composeapp.generated.resources.Res
import ktravel.composeapp.generated.resources.delete
import ktravel.composeapp.generated.resources.directions_bus
import ktravel.composeapp.generated.resources.directions_car
import ktravel.composeapp.generated.resources.flight
import ktravel.composeapp.generated.resources.planning_detail_cd_delete_step
import ktravel.composeapp.generated.resources.planning_detail_transport_duration
import ktravel.composeapp.generated.resources.train
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

fun TransportType.toIcon(): DrawableResource = when (this) {
    TransportType.TRAIN -> Res.drawable.train
    TransportType.BUS -> Res.drawable.directions_bus
    TransportType.CAR -> Res.drawable.directions_car
    TransportType.FLIGHT -> Res.drawable.flight
}

/**
 * Content (right-hand side of the timeline) of a transport: a compact connector between two places.
 * The vehicle icon is rendered in the gutter node by the calling timeline row; only the aggregated
 * duration and the delete action remain here.
 */
@Composable
fun TravelStepTransport(
    step: StepUi.Transport,
    onStepDeleteClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            modifier = Modifier.testTag(StepsPaneTestTags.TRANSPORT_DURATION),
            text = stringResource(
                Res.string.planning_detail_transport_duration,
                step.totalDuration.toString(),
            ),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Spacer(Modifier.weight(1f))

        IconButton(onClick = onStepDeleteClicked) {
            Icon(
                painter = painterResource(Res.drawable.delete),
                contentDescription = stringResource(Res.string.planning_detail_cd_delete_step),
            )
        }
    }
}
