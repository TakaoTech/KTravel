package com.takaotech.ktravel.ui.shared.route.transit

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.takaotech.gunzou.api.geometry.PolylineEncoderDecoder
import com.takaotech.ktravel.domain.routing.model.TransitStep
import com.takaotech.ktravel.domain.routing.model.TransitStop
import io.nacular.measured.units.times

/** A stop the traveller acts at: the time on the board, and the name on the sign. */
@Composable
internal fun TransitStopMarker(
    stop: TransitStop,
    colour: Color,
    onStopClick: (PolylineEncoderDecoder.LatLngZ) -> Unit,
    onLine: TransitStep.Ride,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickableStop(stop, onLine, onStopClick)
            .padding(horizontal = 16.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .width(6.dp)
                .size(6.dp, 12.dp),
            contentAlignment = Alignment.Center,
        ) {
            Box(modifier = Modifier.size(10.dp).background(colour, CircleShape))
        }
        Text(
            text = listOfNotNull((stop.departure ?: stop.arrival)?.clock(), stop.name).joinToString(
                "  ",
            ),
            modifier = Modifier.padding(start = 12.dp),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
        )
    }
}
