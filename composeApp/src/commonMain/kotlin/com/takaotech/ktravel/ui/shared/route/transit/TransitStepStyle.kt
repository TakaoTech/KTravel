package com.takaotech.ktravel.ui.shared.route.transit

import androidx.compose.foundation.clickable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.takaotech.gunzou.api.geometry.PolylineEncoderDecoder
import com.takaotech.ktravel.domain.routing.model.TransitStep
import com.takaotech.ktravel.domain.routing.model.TransitStop
import com.takaotech.ktravel.domain.routing.model.TransitTime
import com.takaotech.ktravel.ui.shared.format.formatClock
import com.takaotech.ktravel.ui.shared.format.toColorOrNull
import io.nacular.measured.units.times

/**
 * Makes a stop move the camera, when there is enough to place it with.
 *
 * The offset on the ride's own geometry is preferred over the stop's coordinates because it is what
 * the navigator carried it for, and it lands the marker on the drawn line rather than beside it.
 */
internal fun Modifier.clickableStop(
    stop: TransitStop,
    onLine: TransitStep.Ride,
    onStopClick: (PolylineEncoderDecoder.LatLngZ) -> Unit,
): Modifier = clickable {
    val polyline = onLine.polyline
    val offset = stop.offset

    val point = if (polyline != null && offset != null) {
        runCatching { PolylineEncoderDecoder.getCoordinateAtOffset(polyline, offset) }.getOrNull()
    } else {
        null
    } ?: PolylineEncoderDecoder.LatLngZ(stop.location.lat, stop.location.lng)

    onStopClick(point)
}

/** The wall clock at the stop, which is the number printed on the departure board. */
internal fun TransitTime.clock(): String = atStop().formatClock()

internal fun TransitStep.lineColor(): Color? = (this as? TransitStep.Ride)?.line?.color?.toColorOrNull()

// FIXME Check background color luminance for accessibility
internal fun TransitStep.textColor(): Color? = (this as? TransitStep.Ride)?.line?.textColor?.toColorOrNull()
