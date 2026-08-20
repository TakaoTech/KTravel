package com.takaotech.ktravel.ui.planning.transport.preview

import androidx.compose.runtime.Composable
import io.nacular.measured.units.Length
import io.nacular.measured.units.Measure
import org.maplibre.spatialk.units.extensions.kilometers
import org.maplibre.spatialk.units.extensions.meters

/** Metres below a kilometre, kilometres with one decimal above it. */
@Composable
fun Measure<Length>.formatDistance(): String = (this `in` Length.kilometers).let { kmDistance ->
    if (kmDistance > 1) {
        kmDistance.kilometers.toString()
    } else {
        kmDistance.meters.toString()
    }
}
