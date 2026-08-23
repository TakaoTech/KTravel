package com.takaotech.ktravel.domain.routing.model

import io.nacular.measured.units.Length
import io.nacular.measured.units.Measure
import kotlinx.datetime.format.DateTimeComponents
import kotlin.time.Duration

/**
 * Summary information for a route.
 *
 * @property durationSeconds Total estimated duration in seconds
 * @property distance Total distance in meters
 */
data class RouteSummary(val durationSeconds: Duration, val distance: Measure<Length>)

/**
 * Basic information about a toll system authority.
 */
data class RouteTollSystem(val id: String, val name: String? = null)

/**
 * Toll payment details for a section.
 *
 * @property tollSystem The name of the toll system collecting the toll. Deprecated: use [tollSystems].
 * @property tollSystemRef Reference index of the affected toll system. Deprecated: use [tollSystems].
 * @property tollSystems Reference indices of the associated toll system(s) in the section's tollSystems array.
 * @property countryCode ISO-3166-1 alpha-3 country code.
 */
data class RouteTollCost(
    val tollSystem: String,
    val tollSystemRef: Int,
    val tollSystems: List<Int>? = null,
    val countryCode: String? = null,
)

/**
 * A navigation action within a route section.
 */
data class RouteAction(
    val action: String,
    val durationSeconds: Duration,
    val distanceMeters: Measure<Length>,
    val instruction: String? = null,
    val offset: Int? = null,
    val direction: String? = null,
    val severity: String? = null,
)

/**
 * Departure or arrival details for a route section.
 */
data class RouteDeparture(val location: RouteLocation, val time: DateTimeComponents? = null)

/**
 * Geographic coordinates.
 */
data class RouteLocation(val lat: Double, val lng: Double)
