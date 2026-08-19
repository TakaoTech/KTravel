package com.takaotech.ktravel.ui.planning.transport.preview

import androidx.compose.runtime.Composable
import io.nacular.measured.units.Length
import io.nacular.measured.units.Measure
import ktravel.composeapp.generated.resources.Res
import ktravel.composeapp.generated.resources.route_preview_distance_km
import ktravel.composeapp.generated.resources.route_preview_distance_m
import ktravel.composeapp.generated.resources.route_preview_duration_hours_minutes
import ktravel.composeapp.generated.resources.route_preview_duration_minutes
import org.jetbrains.compose.resources.stringResource
import kotlin.math.roundToInt
import kotlin.time.Duration

/** Metres below a kilometre, kilometres with one decimal above it. */
@Composable
fun Measure<Length>.formatDistance(): String = ((this `in` Length.meters).roundToInt()).formatDistance()

/** Metres below a kilometre, kilometres with one decimal above it. */
@Composable
fun Int.formatDistance(): String = if (this >= METRES_IN_KILOMETRE) {
    val kilometres = this / METRES_IN_KILOMETRE.toDouble()
    val whole = kilometres.toInt()
    val tenths = ((kilometres - whole) * 10).roundToInt()
    stringResource(Res.string.route_preview_distance_km, "$whole.$tenths")
} else {
    stringResource(Res.string.route_preview_distance_m, this)
}

/**
 * Minutes, or hours and minutes past the hour.
 *
 * Never seconds: no timetable is accurate to one, and showing them would promise a precision the
 * answer does not have.
 */
@Composable
fun Duration.formatDuration(): String {
    val minutes = inWholeMinutes

    return if (minutes >= MINUTES_IN_HOUR) {
        stringResource(
            Res.string.route_preview_duration_hours_minutes,
            minutes / MINUTES_IN_HOUR,
            minutes % MINUTES_IN_HOUR,
        )
    } else {
        stringResource(Res.string.route_preview_duration_minutes, minutes)
    }
}

private const val METRES_IN_KILOMETRE = 1_000
private const val MINUTES_IN_HOUR = 60L
