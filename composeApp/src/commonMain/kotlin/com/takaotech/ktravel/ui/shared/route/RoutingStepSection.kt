package com.takaotech.ktravel.ui.shared.route

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.takaotech.gunzou.api.geometry.PolylineEncoderDecoder
import com.takaotech.ktravel.domain.routing.model.RouteAction
import com.takaotech.ktravel.ui.shared.format.formatDistance

/** Room for "12,3 km" over "1h 3m", which is the widest a manoeuvre's figures ever get. */
private val HOW_FAR_COLUMN_WIDTH = 80.dp

/**
 * The manoeuvres of one section.
 *
 * Takes the [actions] and the [polyline] rather than a section, because the two things that carry
 * manoeuvres — the live answer and the route as the plan saved it — are different types holding the
 * same two fields, and this list has no reason to know which one it is drawing.
 *
 * A manoeuvre is only clickable when both halves of the answer are there: the geometry to look the
 * point up in, and the offset saying where in it. Without either, tapping the row would move the
 * camera somewhere arbitrary, so it does not react at all.
 */
@Composable
fun RoutingStepSection(
    actions: List<RouteAction>,
    polyline: String?,
    onActionClick: (PolylineEncoderDecoder.LatLngZ) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        for (action in actions) {
            val offset = action.offset

            RoutingStep(
                action = action,
                onActionClick = if (polyline == null || offset == null) {
                    null
                } else {
                    {
                        runCatching { PolylineEncoderDecoder.getCoordinateAtOffset(polyline, offset) }
                            .onSuccess(onActionClick)
                    }
                },
            )
        }
    }
}

/** One instruction, with how far and how long it runs. */
@Composable
fun RoutingStep(action: RouteAction, onActionClick: (() -> Unit)? = null, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.then(
            if (onActionClick != null) Modifier.clickable(onClick = onActionClick) else Modifier,
        ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = action.action.replaceFirstChar { it.uppercaseChar() },
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                )
                if (!action.instruction.isNullOrBlank()) {
                    Text(text = action.instruction, style = MaterialTheme.typography.bodyMedium)
                }
                if (!action.direction.isNullOrBlank()) {
                    Text(
                        text = action.direction,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            // Fixed width, not wrap-content: an instruction is a sentence and a distance is never
            // more than a few characters, so letting this column grow is what used to squeeze the
            // instruction beside it down to one letter per line.
            Column(
                modifier = Modifier.width(HOW_FAR_COLUMN_WIDTH),
                horizontalAlignment = Alignment.End,
            ) {
                Text(
                    text = action.distanceMeters.formatDistance(),
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = action.durationSeconds.toString(),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
    }
}
