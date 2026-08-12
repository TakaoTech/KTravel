package com.takaotech.navigation.routing.dto.request

import com.takaotech.navigation.common.model.Units
import com.takaotech.navigation.routing.model.ReturnAttribute
import com.takaotech.navigation.routing.model.RoutingMode
import com.takaotech.navigation.routing.model.TransportMode
import com.vanniktech.locale.Locale
import com.vanniktech.locale.Locales

/**
 * Request parameters for the HERE Routing API /routes endpoint.
 *
 * @property origin Origin waypoint with coordinates and optional place options.
 *                  Supports all place options defined in [PlaceOptions] such as:
 *                  course, sideOfStreetHint, displayLocation, nameHint, radius, etc.
 *                  **Note**: [WaypointOptions] are NOT supported for origin.
 *
 * @property destination Destination waypoint with coordinates, place options, and waypoint options.
 *                       Supports all place options defined in [PlaceOptions].
 *                       Additionally supports [WaypointOptions] including:
 *                       - [WaypointOptions.stopDuration]: desired duration for the stop in seconds.
 *                         The section arriving at this waypoint will have a `wait` post action
 *                         reflecting the stopping time.
 *                       - [WaypointOptions.passThrough]: not typically used for destination
 *                       - [WaypointOptions.charging]: user-planned charging stop for EV vehicles
 *                       - [WaypointOptions.currentWeightChange]: changes vehicle weight at this waypoint
 *
 * @property transportMode Mode of transport
 * @property via List of intermediate waypoints with full [Waypoint] support including [WaypointOptions]
 * @property routingMode Optimization mode (fast or short)
 * @property alternatives Number of alternative routes (0-6)
 * @property departureTime Specifies the time of departure. Can be:
 *                         - [DepartureTime.Local]: date-time without timezone (assumed local at origin),
 *                           e.g., `2019-06-24T01:23:45`
 *                         - [DepartureTime.WithOffset]: date-time with UTC offset,
 *                           e.g., `2019-06-24T01:23:45+02:00`
 *                         - [DepartureTime.Any]: special value indicating time should not be considered.
 *                           Only long-term traffic incidents will be used.
 *                         If neither departureTime nor arrivalTime are specified, current time at
 *                         departure place will be used.
 * @property arrivalTime Arrival time in RFC 3339 format
 * @property units Units of measurement
 * @property lang Language for instructions (BCP47 format)
 * @property returnAttributes List of attributes to include in response. See [ReturnAttribute] for available options.
 *                            Use [ReturnAttribute.Companion.Presets] for common combinations.
 *                            **Note**: Certain combinations have restrictions:
 *                            - If [ReturnAttribute.ACTIONS] is requested, [ReturnAttribute.POLYLINE] must also be requested.
 *                            - If [ReturnAttribute.INSTRUCTIONS] is requested, [ReturnAttribute.ACTIONS] must also be requested.
 *                            - If [ReturnAttribute.TURN_BY_TURN_ACTIONS] is requested, [ReturnAttribute.POLYLINE] must also be requested.
 */
data class RoutesRequest(
    val transportMode: TransportMode,
    val origin: Waypoint,
    val destination: Waypoint,
    val via: List<Waypoint>? = null,
    val routingMode: RoutingMode? = null,
    val alternatives: Int? = null,
    val departureTime: DepartureTime? = DepartureTime.any(),
    val arrivalTime: String? = null,
    val units: Units = Units.METRIC,
    val lang: Locale? = Locale.from(Locales.currentLocaleString()),
    val returnAttributes: List<ReturnAttribute>? = null,
) {
    init {
        // Validate return attributes combinations
        returnAttributes?.let { ReturnAttribute.validate(it) }
    }

    /**
     * Secondary constructor for backward compatibility.
     * Accepts origin and destination as simple coordinate strings "lat,lng".
     */
    constructor(
        origin: String,
        destination: String,
        transportMode: TransportMode,
        via: List<String>? = null,
        routingMode: RoutingMode? = null,
        alternatives: Int? = null,
        departureTime: DepartureTime? = null,
        arrivalTime: String? = null,
        units: Units = Units.METRIC,
        lang: Locale? = null,
        returnAttributes: List<ReturnAttribute>? = null,
    ) : this(
        origin = Waypoint.fromString(origin),
        destination = Waypoint.fromString(destination),
        transportMode = transportMode,
        via = via?.map { Waypoint.fromString(it) },
        routingMode = routingMode,
        alternatives = alternatives,
        departureTime = departureTime,
        arrivalTime = arrivalTime,
        units = units,
        lang = lang,
        returnAttributes = returnAttributes,
    )
}
