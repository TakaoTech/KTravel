package com.takaotech.navigation.routing.dto.request

/**
 * Defines which attributes are included in the response as part of data representation of a
 * Route or Section in the HERE Routing API.
 *
 * The following restrictions apply when specifying the return parameter:
 * - If [ACTIONS] is requested, then [POLYLINE] must also be requested.
 * - If [INSTRUCTIONS] is requested, then [ACTIONS] must also be requested.
 * - If [TURN_BY_TURN_ACTIONS] is requested, then [POLYLINE] must also be requested.
 * - If at least one attribute is requested within the spans parameter, then [POLYLINE] must be requested as well.
 */
enum class ReturnAttribute(val value: String) {
    /**
     * Polyline for the route in Flexible Polyline Encoding.
     * Either a 2D polyline (without elevation specified), or a 3D polyline with the 3rd dimension
     * type Elevation (with elevation specified), using the WGS84 coordinate system.
     *
     * @see <a href="https://github.com/heremaps/flexible-polyline">Flexible Polyline</a>
     */
    POLYLINE("polyline"),

    /**
     * Actions (such as maneuvers or tasks) that must be taken to complete the section.
     *
     * **Note**: Requires [POLYLINE] to be requested as well.
     */
    ACTIONS("actions"),

    /**
     * Include instructions in returned actions. Instructions are localized to the requested language.
     *
     * **Note**: Requires [ACTIONS] to be requested as well.
     */
    INSTRUCTIONS("instructions"),

    /**
     * Include summary for the section.
     */
    SUMMARY("summary"),

    /**
     * Include summary for the travel portion of the section.
     */
    TRAVEL_SUMMARY("travelSummary"),

    /**
     * Include all information necessary to support turn by turn guidance to complete the section.
     *
     * **Note**: Requires [POLYLINE] to be requested as well.
     */
    TURN_BY_TURN_ACTIONS("turnByTurnActions"),

    /**
     * Use a region-specific machine learning model to calculate route duration.
     *
     * **Disclaimer**: This parameter is currently in beta release, and is therefore subject to breaking changes.
     */
    ML_DURATION("mlDuration"),

    /**
     * Include route duration under typical traffic conditions.
     */
    TYPICAL_DURATION("typicalDuration"),

    /**
     * Include WGS84 elevation information in coordinate and geometry types.
     * See e.g. polyline or location.
     */
    ELEVATION("elevation"),

    /**
     * Encode calculated route and return a handle which can be used with
     * routes/{routeHandle} to decode the route at a later point in time.
     */
    ROUTE_HANDLE("routeHandle"),

    /**
     * Include information on passthrough via waypoints in the section.
     */
    PASSTHROUGH("passthrough"),

    /**
     * Include a list of all incidents applicable to each section.
     * Returned incidents may be referenced by incidents and/or intersectionIncidents span parameters.
     * Incidents are localized to the requested language.
     */
    INCIDENTS("incidents"),

    /**
     * Include information about routing zones each section goes through.
     */
    ROUTING_ZONES("routingZones"),

    /**
     * Include information about road types each section goes through.
     */
    TRUCK_ROAD_TYPES("truckRoadTypes"),

    /**
     * Include information about the tolls to be paid, per section, according to the tolls parameter
     * and other toll-influencing parameters such as vehicle dimensions in the query,
     * e.g. transportMode, vehicle[hovOccupancy], vehicle[height].
     *
     * If tolls cannot be calculated for a section, it will contain the tollsDataUnavailable notice code.
     *
     * **Note**:
     * - Toll requests support car dimensions such as atypical car height and weight, trailers,
     *   axle count and vehicle fuel type. However extreme parameter combinations may result in
     *   tolls being returned for a different vehicle type (for example, a car with 6 axles will
     *   most likely result in truck tolls). Therefore the request will return the most "typical"
     *   toll cost for the parameters provided.
     * - Tolls are not available in route import service.
     */
    TOLLS("tolls"),

    /**
     * Include a list of the most important names and route numbers on this route that
     * differentiate it from other alternatives.
     */
    ROUTE_LABELS("routeLabels"),

    /**
     * Include info notices for potential time-dependent violations in the sections that match
     * the current vehicle profile, but are not violating the restricted times for the calculated route.
     */
    POTENTIAL_TIME_DEPENDENT_VIOLATIONS("potentialTimeDependentViolations"),

    /**
     * Include information about the parts of the route that are not allowed to be driven through,
     * i.e. route can only start, end, or have an intermediate via waypoint here.
     */
    NO_THROUGH_RESTRICTIONS("noThroughRestrictions");

    override fun toString(): String = value

    companion object {
        /**
         * Common combinations of return attributes for typical use cases.
         */
        object Presets {
            /**
             * Basic route information with polyline and summary.
             */
            val BASIC = listOf(POLYLINE, SUMMARY)

            /**
             * Route with navigation instructions.
             * Includes polyline, actions, and instructions.
             */
            val NAVIGATION = listOf(POLYLINE, ACTIONS, INSTRUCTIONS, SUMMARY)

            /**
             * Route with turn-by-turn guidance.
             * Includes polyline, turn-by-turn actions, and summary.
             */
            val TURN_BY_TURN = listOf(POLYLINE, TURN_BY_TURN_ACTIONS, SUMMARY)

            /**
             * Route with toll information.
             * Includes polyline, summary, and tolls.
             */
            val WITH_TOLLS = listOf(POLYLINE, SUMMARY, TOLLS)

            /**
             * Route with elevation data.
             * Includes polyline with elevation and summary.
             */
            val WITH_ELEVATION = listOf(POLYLINE, ELEVATION, SUMMARY)

            /**
             * Full route information for detailed analysis.
             * Includes polyline, actions, instructions, summary, travel summary, and route handle.
             */
            val DETAILED = listOf(
                POLYLINE,
                ACTIONS,
                INSTRUCTIONS,
                SUMMARY,
                TRAVEL_SUMMARY,
                ROUTE_HANDLE
            )
        }

        /**
         * Converts a list of ReturnAttribute to the query string format.
         *
         * @param attributes List of return attributes
         * @return Comma-separated string of attribute values
         */
        fun toQueryString(attributes: List<ReturnAttribute>): String =
            attributes.joinToString(",") { it.value }

        /**
         * Validates that the return attributes follow HERE API restrictions.
         *
         * @param attributes List of return attributes to validate
         * @throws IllegalArgumentException if validation fails
         */
        fun validate(attributes: List<ReturnAttribute>) {
            if (ACTIONS in attributes && POLYLINE !in attributes) {
                throw IllegalArgumentException("If ACTIONS is requested, POLYLINE must also be requested")
            }
            if (INSTRUCTIONS in attributes && ACTIONS !in attributes) {
                throw IllegalArgumentException("If INSTRUCTIONS is requested, ACTIONS must also be requested")
            }
            if (TURN_BY_TURN_ACTIONS in attributes && POLYLINE !in attributes) {
                throw IllegalArgumentException("If TURN_BY_TURN_ACTIONS is requested, POLYLINE must also be requested")
            }
        }
    }
}
