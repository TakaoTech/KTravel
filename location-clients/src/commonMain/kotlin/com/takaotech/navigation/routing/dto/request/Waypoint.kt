package com.takaotech.navigation.routing.dto.request

/**
 * Represents a waypoint location for the HERE Routing API.
 *
 * Format: `{lat},{lng}[PlaceOptions][com.takaotech.routing.dto.request.WaypointOptions]`
 *
 * A waypoint consists of:
 * - Exactly one place (latitude, longitude)
 * - Optional settings for the place (PlaceOptions) - separated by `;`
 * - Optional settings for the waypoint itself (WaypointOptions) - separated by `!`
 *
 * @property lat Latitude in WGS84 format
 * @property lng Longitude in WGS84 format
 * @property placeOptions Optional place options for fine-tuning waypoint matching
 * @property waypointOptions Optional waypoint options (available for via and destination waypoints only).
 *                           **Note**: origin does not support WaypointOptions.
 */
data class Waypoint(
    val lat: Double,
    val lng: Double,
    val placeOptions: PlaceOptions? = null,
    val waypointOptions: WaypointOptions? = null,
) {
    /**
     * Converts the waypoint to the HERE API string format.
     *
     * Format: `{lat},{lng}[;placeOption1=value1;...][!waypointOption1=value1!...]`
     */
    fun toQueryString(): String {
        val base = "$lat,$lng"
        val placeOptionsQ = placeOptions?.toQueryString() ?: ""
        val waypointOptionsQ = waypointOptions?.toQueryString() ?: ""

        return base + placeOptionsQ + waypointOptionsQ
    }

    companion object {
        /**
         * Creates a simple waypoint with just coordinates.
         */
        fun simple(lat: Double, lng: Double): Waypoint =
            Waypoint(lat, lng)

        /**
         * Creates a waypoint from a coordinate string "lat,lng".
         * @throws IllegalArgumentException if the format is invalid
         */
        fun fromString(coordinate: String): Waypoint {
            val parts = coordinate.split(",")
            require(parts.size >= 2) { "Invalid coordinate format. Expected 'lat,lng'" }
            return Waypoint(
                lat = parts[0].toDouble(),
                lng = parts[1].toDouble()
            )
        }
    }
}

/**
 * U-Turn permission mode at a waypoint.
 */
enum class UTurnPermission(val value: String) {
    /** Allow making a U-Turn at this stop-over waypoint */
    ALLOW("allow"),

    /** Avoid making a U-Turn at this stop-over waypoint */
    AVOID("avoid")
}

/**
 * Specifies how the location set by sideOfStreetHint should be handled.
 */
enum class MatchSideOfStreet(val value: String) {
    /** Always prefer the given side of street */
    ALWAYS("always"),

    /** Only prefer using side of street in case the street has dividers (default) */
    ONLY_IF_DIVIDED("onlyIfDivided")
}

/**
 * Place options for fine-tuning waypoint matching in HERE Routing API.
 *
 * These options control how the router matches the waypoint to the road network.
 *
 * @property course Degrees clock-wise from north (0-359). Indicates desired direction at the place.
 *                  Often combined with [radius] and/or [minCourseDistance].
 *                  E.g. 90 indicates east.
 *
 * @property sideOfStreetHint Coordinate indicating the side of the street that should be used.
 *                            If the location is to the left of the street, the router will prefer
 *                            using that side in case the street has dividers.
 *                            Required if [matchSideOfStreet] is set to [com.takaotech.routing.dto.request.MatchSideOfStreet.ALWAYS].
 *                            Cannot be combined with [radius], [radiusPenalty] or [snapRadius].
 *
 * @property displayLocation Coordinate indicating the location on the map where this POI is to be displayed.
 *                           This attribute is passed through to the displayLocation attribute in the
 *                           corresponding Place object in the output.
 *
 * @property uTurnPermission Specifies the U-Turn Permission mode at the stop-over waypoint.
 *                           Not supported for pass-through waypoints.
 *
 * @property matchSideOfStreet Specifies how the location set by [sideOfStreetHint] should be handled.
 *                             Requires [sideOfStreetHint] to be specified as well.
 *
 * @property nameHint Causes the router to look for the place with the most similar name.
 *                    Typical examples: "North" to differentiate between I66 North and I66 South,
 *                    "Downtown Avenue" to correctly select a residential street.
 *                    Empty string values are ignored.
 *
 * @property radius Meters (max 200). Asks the router to consider all places within the given radius
 *                  as potential candidates for route calculation.
 *                  Cannot be combined with [snapRadius].
 *
 * @property radiusPenalty Percentage 0-10000. Used in conjunction with [radius].
 *                         Router will apply a penalty to candidates based on their air distance to the waypoint.
 *                         100 is just the cost of the air distance, 200 is twice the cost.
 *                         The penalty multiplied by radius must be <= 7200.
 *                         Cannot be combined with [snapRadius].
 *                         **Alpha**: This parameter is in development.
 *
 * @property snapRadius Meters. Instructs the router to match the waypoint to the most "significant" road
 *                      within the specified radius. A highway is more significant than a national road, etc.
 *                      Typical use case: selecting a waypoint on a zoomed-out map view.
 *                      Cannot be combined with [radius] or [radiusPenalty].
 *
 * @property minCourseDistance Meters (max 2000). Instructs the routing service to try to find a route
 *                             that avoids actions for the indicated distance.
 *                             Useful when the origin is determined by a moving vehicle and the user
 *                             might not have time to react to early actions.
 *
 * @property customizationIndex Zero-based index into the list of customizations specified in the
 *                              customizations parameter. The customization at that index must be an Extension Map.
 *                              **Alpha**: This parameter is in development.
 *
 * @property segmentIdHint Causes the router to try and match to the specified segment.
 *                         Waypoint coordinates need to be on the segment, otherwise waypoint will be
 *                         matched ignoring the segment hint.
 *                         Useful when the waypoint is too close to more than one segment.
 *
 * @property onRoadThreshold Meters. Allows specifying a distance within which the waypoint could be
 *                           considered as being on a highway/bridge/tunnel/sliproad.
 *                           Within this threshold, the attributes of the segments do not impact the matching.
 *                           Outside the threshold only segments which aren't highway/bridge/tunnel/sliproad can be matched.
 */
data class PlaceOptions(
    val course: Int? = null,
    val sideOfStreetHint: Coordinate? = null,
    val displayLocation: Coordinate? = null,
    val uTurnPermission: UTurnPermission? = null,
    val matchSideOfStreet: MatchSideOfStreet? = null,
    val nameHint: String? = null,
    val radius: Int? = null,
    val radiusPenalty: Int? = null,
    val snapRadius: Int? = null,
    val minCourseDistance: Int? = null,
    val customizationIndex: Int? = null,
    val segmentIdHint: String? = null,
    val onRoadThreshold: Int? = null
) {
    init {
        // Validation based on HERE API constraints
        course?.let {
            require(it in 0..359) { "course must be between 0 and 359 degrees" }
        }
        radius?.let {
            require(it in 1..200) { "radius must be between 1 and 200 meters" }
        }
        radiusPenalty?.let {
            require(it in 0..10000) { "radiusPenalty must be between 0 and 10000" }
        }
        minCourseDistance?.let {
            require(it in 1..2000) { "minCourseDistance must be between 1 and 2000 meters" }
        }
        // Validate that radius * radiusPenalty <= 7200 * 100 (since penalty is percentage)
        if (radius != null && radiusPenalty != null) {
            require(radius * radiusPenalty <= 720000) {
                "radius * radiusPenalty must be <= 720000 (7200 * 100)"
            }
        }
        // Validate mutually exclusive options
        if (snapRadius != null) {
            require(radius == null && radiusPenalty == null) {
                "snapRadius cannot be combined with radius or radiusPenalty"
            }
        }
        if (sideOfStreetHint != null) {
            require(radius == null && radiusPenalty == null && snapRadius == null) {
                "sideOfStreetHint cannot be combined with radius, radiusPenalty or snapRadius"
            }
        }
        if (matchSideOfStreet == MatchSideOfStreet.ALWAYS) {
            require(sideOfStreetHint != null) {
                "sideOfStreetHint is required when matchSideOfStreet is ALWAYS"
            }
        }
    }

    /**
     * Converts place options to the HERE API query string format.
     *
     * Format: `;option1=value1;option2=value2...`
     */
    fun toQueryString(): String {
        val options = mutableListOf<String>()

        course?.let { options.add("course=$it") }
        sideOfStreetHint?.let { options.add("sideOfStreetHint=${it.lat},${it.lng}") }
        displayLocation?.let { options.add("displayLocation=${it.lat},${it.lng}") }
        uTurnPermission?.let { options.add("uTurnPermission=${it.value}") }
        matchSideOfStreet?.let { options.add("matchSideOfStreet=${it.value}") }
        nameHint?.takeIf { it.isNotEmpty() }?.let { options.add("nameHint=$it") }
        radius?.let { options.add("radius=$it") }
        radiusPenalty?.let { options.add("radiusPenalty=$it") }
        snapRadius?.let { options.add("snapRadius=$it") }
        minCourseDistance?.let { options.add("minCourseDistance=$it") }
        customizationIndex?.let { options.add("customizationIndex=$it") }
        segmentIdHint?.let { options.add("segmentIdHint=$it") }
        onRoadThreshold?.let { options.add("onRoadThreshold=$it") }

        return if (options.isEmpty()) "" else options.joinToString(separator = ";", prefix = ";")
    }
}

/**
 * Represents a simple coordinate (latitude, longitude).
 */
data class Coordinate(
    val lat: Double,
    val lng: Double
) {
    override fun toString(): String = "$lat,$lng"
}

/**
 * Supply type for EV charging connectors.
 */
enum class ChargingSupplyType(val value: String) {
    /** Single-phase AC */
    AC_SINGLE("acSingle"),

    /** Three-phase AC */
    AC_THREE("acThree"),

    /** DC */
    DC("dc")
}

/**
 * Charging options for a waypoint with EV charging station.
 *
 * Format: `charging=(power=<value>;current=<value>;voltage=<value>;supplyType=<value>;minDuration=<value>;maxDuration=<value>)`
 *
 * @property power Rated power of the connector in kW. Required.
 * @property current Rated current of the connector in A. Required.
 * @property voltage Rated voltage of the connector in V. Required.
 * @property supplyType Supply type of the connector. Required.
 * @property minDuration Minimum time the user expects to charge at the station in seconds, including chargingSetupDuration.
 *                       At least one of minDuration or maxDuration is required.
 * @property maxDuration Maximum time the user plans to charge at the station in seconds, including chargingSetupDuration.
 *                       At least one of minDuration or maxDuration is required.
 */
data class ChargingOptions(
    val power: Double,
    val current: Double,
    val voltage: Double,
    val supplyType: ChargingSupplyType,
    val minDuration: Int? = null,
    val maxDuration: Int? = null
) {
    init {
        require(minDuration != null || maxDuration != null) {
            "At least one of minDuration or maxDuration is required"
        }
        minDuration?.let {
            require(it >= 0) { "minDuration must be non-negative" }
        }
        maxDuration?.let {
            require(it >= 0) { "maxDuration must be non-negative" }
        }
        if (minDuration != null && maxDuration != null) {
            require(minDuration <= maxDuration) {
                "minDuration must be less than or equal to maxDuration"
            }
        }
    }

    /**
     * Converts charging options to the HERE API query string format.
     *
     * Format: `(power=<value>;current=<value>;voltage=<value>;supplyType=<value>;minDuration=<value>;maxDuration=<value>)`
     */
    fun toQueryString(): String {
        val options = mutableListOf<String>()
        options.add("power=$power")
        options.add("current=$current")
        options.add("voltage=$voltage")
        options.add("supplyType=${supplyType.value}")
        minDuration?.let { options.add("minDuration=$it") }
        maxDuration?.let { options.add("maxDuration=$it") }
        return "(${options.joinToString(";")})"
    }
}

/**
 * Waypoint options for via and destination waypoints in HERE Routing API.
 *
 * These options control waypoint-specific behavior during route calculation.
 * **Note**: origin does not support WaypointOptions. They are available only for via and destination waypoints.
 *
 * @property stopDuration Desired duration for the stop in seconds. Must be less than 50000.
 *                        The section arriving at this waypoint will have a `wait` post action
 *                        reflecting the stopping time. The subsequent section will start at
 *                        the arrival time of the former section + stop duration.
 *
 * @property passThrough Boolean. Default value is false.
 *                       Setting to true asks the router to avoid:
 *                       - Introducing a stop at the waypoint
 *                       - Splitting the route into sections
 *                       - Changing the direction of travel
 *                       **Note**: Cannot be combined with stopDuration > 0.
 *                       **Note**: Not supported for destination waypoint.
 *
 * @property charging Structured options denoting a user-planned charging stop.
 *                    Requires EV parameters to be set (initialCharge, maxCharge, chargingCurve).
 *                    **Note**: Not supported for pass-through waypoints.
 *
 * @property currentWeightChange Changes the value of vehicle[currentWeight] by this value in kilograms.
 *                               Enables support of scenarios where the vehicle takes additional cargo
 *                               or unloads its cargo along the route.
 *                               Relative value beginning with either + or -.
 *                               Available range: from -40000 to +40000 (inclusive).
 *                               When this parameter is provided, both vehicle[currentWeight] and
 *                               vehicle[grossWeight] must also be provided.
 */
data class WaypointOptions(
    val stopDuration: Int? = null,
    val passThrough: Boolean? = null,
    val charging: ChargingOptions? = null,
    val currentWeightChange: Int? = null
) {
    init {
        stopDuration?.let {
            require(it in 0 until 50000) { "stopDuration must be between 0 and 49999 seconds" }
        }
        currentWeightChange?.let {
            require(it in -40000..40000) { "currentWeightChange must be between -40000 and +40000 kg" }
        }
        // Validate that passThrough=true cannot be combined with stopDuration > 0
        if (passThrough == true && stopDuration != null && stopDuration > 0) {
            throw IllegalArgumentException("passThrough=true cannot be combined with stopDuration > 0")
        }
        // Validate that charging is not supported for pass-through waypoints
        if (passThrough == true && charging != null) {
            throw IllegalArgumentException("charging is not supported for pass-through waypoints")
        }
    }

    /**
     * Converts waypoint options to the HERE API query string format.
     *
     * Format: `!option1=value1!option2=value2...`
     */
    fun toQueryString(): String {
        val options = mutableListOf<String>()

        stopDuration?.let { options.add("stopDuration=$it") }
        passThrough?.let { options.add("passThrough=$it") }
        charging?.let { options.add("charging=${it.toQueryString()}") }
        currentWeightChange?.let {
            val sign = if (it >= 0) "+" else ""
            options.add("currentWeightChange=$sign$it")
        }

        return if (options.isEmpty()) "" else options.joinToString(separator = "!", prefix = "!")
    }
}
