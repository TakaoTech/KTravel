package com.takaotech.navigation.routing.dto.request

import com.takaotech.navigation.routing.model.Units
import com.vanniktech.locale.Locale
import com.vanniktech.locale.Locales

/**
 * Request parameters for the HERE Routing API /routes endpoint.
 *
 * @property origin Origin waypoint with coordinates and optional place options.
 *                  Supports all place options defined in [com.takaotech.navigation.routing.dto.request.PlaceOptions] such as:
 *                  course, sideOfStreetHint, displayLocation, nameHint, radius, etc.
 *                  **Note**: [com.takaotech.navigation.routing.dto.request.WaypointOptions] are NOT supported for origin.
 *
 * @property destination Destination waypoint with coordinates, place options, and waypoint options.
 *                       Supports all place options defined in [com.takaotech.navigation.routing.dto.request.PlaceOptions].
 *                       Additionally supports [com.takaotech.navigation.routing.dto.request.WaypointOptions] including:
 *                       - [com.takaotech.navigation.routing.dto.request.WaypointOptions.stopDuration]: desired duration for the stop in seconds.
 *                         The section arriving at this waypoint will have a `wait` post action
 *                         reflecting the stopping time.
 *                       - [com.takaotech.navigation.routing.dto.request.WaypointOptions.passThrough]: not typically used for destination
 *                       - [com.takaotech.navigation.routing.dto.request.WaypointOptions.charging]: user-planned charging stop for EV vehicles
 *                       - [com.takaotech.navigation.routing.dto.request.WaypointOptions.currentWeightChange]: changes vehicle weight at this waypoint
 *
 * @property transportMode Mode of transport
 * @property via List of intermediate waypoints with full [com.takaotech.navigation.routing.dto.request.Waypoint] support including [com.takaotech.navigation.routing.dto.request.WaypointOptions]
 * @property routingMode Optimization mode (fast or short)
 * @property alternatives Number of alternative routes (0-6)
 * @property departureTime Specifies the time of departure. Can be:
 *                         - [com.takaotech.navigation.routing.dto.request.DepartureTime.Local]: date-time without timezone (assumed local at origin),
 *                           e.g., `2019-06-24T01:23:45`
 *                         - [com.takaotech.navigation.routing.dto.request.DepartureTime.WithOffset]: date-time with UTC offset,
 *                           e.g., `2019-06-24T01:23:45+02:00`
 *                         - [com.takaotech.navigation.routing.dto.request.DepartureTime.Any]: special value indicating time should not be considered.
 *                           Only long-term traffic incidents will be used.
 *                         If neither departureTime nor arrivalTime are specified, current time at
 *                         departure place will be used.
 * @property arrivalTime Arrival time in RFC 3339 format
 * @property units Units of measurement
 * @property lang Language for instructions (BCP47 format)
 * @property returnAttributes List of attributes to include in response. See [com.takaotech.navigation.routing.dto.request.ReturnAttribute] for available options.
 *                            Use [com.takaotech.navigation.routing.dto.request.ReturnAttribute.Companion.Presets] for common combinations.
 *                            **Note**: Certain combinations have restrictions:
 *                            - If [com.takaotech.navigation.routing.dto.request.ReturnAttribute.ACTIONS] is requested, [com.takaotech.navigation.routing.dto.request.ReturnAttribute.POLYLINE] must also be requested.
 *                            - If [com.takaotech.navigation.routing.dto.request.ReturnAttribute.INSTRUCTIONS] is requested, [com.takaotech.navigation.routing.dto.request.ReturnAttribute.ACTIONS] must also be requested.
 *                            - If [com.takaotech.navigation.routing.dto.request.ReturnAttribute.TURN_BY_TURN_ACTIONS] is requested, [com.takaotech.navigation.routing.dto.request.ReturnAttribute.POLYLINE] must also be requested.
 */
data class RoutesRequest(
    val transportMode: com.takaotech.navigation.routing.model.TransportMode,
    val origin: Waypoint,
    val destination: Waypoint,
    val via: List<Waypoint>? = null,
    val routingMode: com.takaotech.navigation.routing.model.RoutingMode? = null,
    val alternatives: Int? = null,
    val departureTime: DepartureTime? = null,
    val arrivalTime: String? = null,
    val units: Units = Units.METRIC,
    val lang: Locale? = Locale.from(Locales.currentLocaleString()),
    val returnAttributes: List<ReturnAttribute>? = null
) {
    init {
        // Validate return attributes combinations
        returnAttributes?.let {
            ReturnAttribute.validate(
                it
            )
        }
    }

    /**
     * Secondary constructor for backward compatibility.
     * Accepts origin and destination as simple coordinate strings "lat,lng".
     */
    constructor(
        origin: String,
        destination: String,
        transportMode: com.takaotech.navigation.routing.model.TransportMode,
        via: List<String>? = null,
        routingMode: com.takaotech.navigation.routing.model.RoutingMode? = null,
        alternatives: Int? = null,
        departureTime: DepartureTime? = null,
        arrivalTime: String? = null,
        units: Units = Units.METRIC,
        lang: Locale? = null,
        returnAttributes: List<ReturnAttribute>? = null
    ) : this(
        origin = Waypoint.fromString(origin),
        destination = Waypoint.fromString(
            destination
        ),
        transportMode = transportMode,
        via = via?.map {
            Waypoint.fromString(
                it
            )
        },
        routingMode = routingMode,
        alternatives = alternatives,
        departureTime = departureTime,
        arrivalTime = arrivalTime,
        units = units,
        lang = lang,
        returnAttributes = returnAttributes
    )

}
