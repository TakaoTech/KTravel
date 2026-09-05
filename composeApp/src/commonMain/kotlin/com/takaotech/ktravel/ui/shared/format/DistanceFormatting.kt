package com.takaotech.ktravel.ui.shared.format

import io.nacular.measured.units.Length
import io.nacular.measured.units.Measure
import org.maplibre.spatialk.units.International
import org.maplibre.spatialk.units.extensions.meters

/** Below this, a distance reads better in metres than in fractions of a kilometre. */
private const val KILOMETRE_IN_METRES = 1000.0

/**
 * Metres below a kilometre, kilometres with one decimal above it.
 *
 * The unit has to be named on the way out: `Length.toString()` always prints metres, whatever the
 * length was built from, so converting first and printing after produced "99417.00 m" for 99 km and
 * "0.24 m" for 240 metres — the number of one unit under the name of another.
 */
fun Measure<Length>.formatDistance(): String {
    val metres = this `in` Length.meters
    return if (metres >= KILOMETRE_IN_METRES) {
        metres.meters.toString(International.Kilometers, decimalPlaces = 1)
    } else {
        metres.meters.toString(International.Meters, decimalPlaces = 0)
    }
}
