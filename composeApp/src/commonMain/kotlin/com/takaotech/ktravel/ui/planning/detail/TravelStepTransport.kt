package com.takaotech.ktravel.ui.planning.detail

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.takaotech.ktravel.domain.model.TransportType
import com.takaotech.ktravel.presentation.planning.TravelDay
import ktravel.composeapp.generated.resources.Res
import ktravel.composeapp.generated.resources.delete
import ktravel.composeapp.generated.resources.directions_bus
import ktravel.composeapp.generated.resources.directions_car
import ktravel.composeapp.generated.resources.flight
import ktravel.composeapp.generated.resources.train
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import kotlin.time.Duration.Companion.seconds

fun TransportType.toIcon(): DrawableResource = when (this) {
    TransportType.TRAIN -> Res.drawable.train
    TransportType.BUS -> Res.drawable.directions_bus
    TransportType.CAR -> Res.drawable.directions_car
    TransportType.FLIGHT -> Res.drawable.flight
}

@Composable
fun TravelStepTransport(
    step: TravelDay.Step.Transport,
    onStepDeleteClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(painter = painterResource(step.type.toIcon()), contentDescription = null)
        val totalDuration by remember(step.route) {
            derivedStateOf {
                var wholeDuration = 0.seconds

                for (section in step.route.sections) {
                    wholeDuration += section.summary.durationSeconds
                }

                wholeDuration.toString()
            }
        }

        Text("Duration $totalDuration")

        Spacer(Modifier.weight(1f))

        IconButton(
            modifier = Modifier.padding(top = 8.dp),
            onClick = onStepDeleteClicked
        ) {
            Icon(
                painter = painterResource(Res.drawable.delete),
                contentDescription = null,
            )
        }
    }
}