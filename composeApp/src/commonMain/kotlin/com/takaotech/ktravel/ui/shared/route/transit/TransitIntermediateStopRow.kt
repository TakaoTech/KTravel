package com.takaotech.ktravel.ui.shared.route.transit

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.takaotech.gunzou.api.geometry.PolylineEncoderDecoder
import com.takaotech.ktravel.domain.routing.model.TransitStep
import com.takaotech.ktravel.domain.routing.model.TransitStop

@Composable
internal fun TransitIntermediateStopRow(
    stop: TransitStop,
    onStopClick: (PolylineEncoderDecoder.LatLngZ) -> Unit,
    onLine: TransitStep.Ride,
    modifier: Modifier = Modifier,
) {
    Text(
        text = listOfNotNull(
            (stop.departure ?: stop.arrival)?.clock(),
            stop.name,
        ).joinToString("  "),
        modifier = modifier
            .fillMaxWidth()
            .clickableStop(stop, onLine, onStopClick)
            .padding(vertical = 4.dp),
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}
