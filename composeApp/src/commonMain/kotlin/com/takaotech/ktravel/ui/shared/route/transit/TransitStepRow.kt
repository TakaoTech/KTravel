package com.takaotech.ktravel.ui.shared.route.transit

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.takaotech.gunzou.api.geometry.PolylineEncoderDecoder
import com.takaotech.ktravel.domain.routing.model.TransitStep
import io.nacular.measured.units.times

/**
 * One step on the timeline.
 *
 * The `when` is exhaustive over the sealed step, which is the point of it being sealed: a kind of
 * step added to the model does not compile until it has been given a row.
 */
@Composable
internal fun TransitStepRow(
    step: TransitStep,
    onStopClick: (PolylineEncoderDecoder.LatLngZ) -> Unit,
    modifier: Modifier = Modifier,
) {
    when (step) {
        is TransitStep.Walk -> TransitWalkRow(
            step = step,
            modifier = modifier,
        )

        is TransitStep.Ride -> TransitRideRow(
            step = step,
            onStopClick = onStopClick,
            modifier = modifier,
        )
    }
}
