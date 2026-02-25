package com.takaotech.navigation.routing.dto.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * A notice contains important notifications.
 *
 * @property title Human-readable notice description.
 * @property code Machine-readable code
 * @property severity Severity level (critical, info, warning)
 */
@Serializable
data class Notice(
    val title: String,
    val code: NoticeCode? = null,
    val severity: String? = null
)

@Serializable
enum class NoticeCode {
    @SerialName("noRouteFound")
    NO_ROUTE_FOUND,

    @SerialName("failedRouteHandleCreation")
    FAILED_ROUTE_HANDLE_CREATION,

    @SerialName("cancelled")
    CANCELLED,

    @SerialName("routeCalculationFailed")
    ROUTE_CALCULATION_FAILED,

    @SerialName("couldNotMatchOrigin")
    COULD_NOT_MATCH_ORIGIN,

    @SerialName("couldNotMatchDestination")
    COULD_NOT_MATCH_DESTINATION,

    @SerialName("noReachableChargingStationsFound")
    NO_REACHABLE_CHARGING_STATIONS_FOUND,

    @SerialName("violatedTransportModeInRouteHandleDecoding")
    VIOLATED_TRANSPORT_MODE_IN_ROUTE_HANDLE_DECODING,

    @SerialName("unknownError")
    UNKNOWN_ERROR,

    @SerialName("routeLengthLimitExceeded")
    ROUTE_LENGTH_LIMIT_EXCEEDED,

    @SerialName("avoidSegmentsInvalidId")
    AVOID_SEGMENTS_INVALID_ID,

    @SerialName("avoidZonesInvalidId")
    AVOID_ZONES_INVALID_ID,

    @SerialName("avoidTruckRoadTypesInvalidId")
    AVOID_TRUCK_ROAD_TYPES_INVALID_ID,

    @SerialName("returnToRoute")
    RETURN_TO_ROUTE,

    @SerialName("importFailed")
    IMPORT_FAILED,

    @SerialName("importSplitRoute")
    IMPORT_SPLIT_ROUTE,

    @SerialName("brandDoesNotExist")
    BRAND_DOES_NOT_EXIST,

    @SerialName("unknownParameter")
    UNKNOWN_PARAMETER,

    @SerialName("mainLanguageNotFound")
    MAIN_LANGUAGE_NOT_FOUND,

    @SerialName("avoidOptionsWithMakeReachableLimitation")
    AVOID_OPTIONS_WITH_MAKE_REACHABLE_LIMITATION,

    @SerialName("currentWeightChangeNoCurrentWeight")
    CURRENT_WEIGHT_CHANGE_NO_CURRENT_WEIGHT,

    @SerialName("currentWeightChangeNoGrossWeight")
    CURRENT_WEIGHT_CHANGE_NO_GROSS_WEIGHT
}
