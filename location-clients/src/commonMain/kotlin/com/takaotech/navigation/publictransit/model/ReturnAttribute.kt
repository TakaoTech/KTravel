package com.takaotech.navigation.publictransit.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Defines which section attributes are included in the response.
 */
@Serializable
enum class ReturnAttribute {
    @SerialName("intermediate")
    INTERMEDIATE,

    @SerialName("fares")
    FARES,

    @SerialName("polyline")
    POLYLINE,

    @SerialName("actions")
    ACTIONS,

    @SerialName("travelSummary")
    TRAVEL_SUMMARY,

    @SerialName("incidents")
    INCIDENTS,

    @SerialName("bookingLinks")
    BOOKING_LINKS,

    @SerialName("nextDepartures")
    NEXT_DEPARTURES,

    @SerialName("sourceFeedMapping")
    SOURCE_FEED_MAPPING,

    @SerialName("serviceTimes")
    SERVICE_TIMES;

    fun toQueryString(): String = when (this) {
        INTERMEDIATE -> "intermediate"
        FARES -> "fares"
        POLYLINE -> "polyline"
        ACTIONS -> "actions"
        TRAVEL_SUMMARY -> "travelSummary"
        INCIDENTS -> "incidents"
        BOOKING_LINKS -> "bookingLinks"
        NEXT_DEPARTURES -> "nextDepartures"
        SOURCE_FEED_MAPPING -> "sourceFeedMapping"
        SERVICE_TIMES -> "serviceTimes"
    }

    companion object {
        fun toQueryString(attributes: List<ReturnAttribute>): String =
            attributes.joinToString(",") { it.toQueryString() }
    }
}
