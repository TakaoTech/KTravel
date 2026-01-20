package com.takaotech.navigation.publictransit.dto.response

import kotlinx.serialization.Serializable

/**
 * A section of a transit route. Can be either a transit section or a pedestrian section.
 * The type field indicates which kind of section this is ("transit" or "pedestrian").
 */
@Serializable
data class TransitRouteSection(
    val id: String,
    val type: String,
    val departure: TransitDeparture? = null,
    val arrival: TransitDeparture? = null,
    val travelSummary: TravelSummary? = null,
    val polyline: String? = null,
    val transport: TransitTransport? = null,
    val notices: List<Notice>? = null,
    val intermediateStops: List<TransitStop>? = null
)

/**
 * Represents an intermediate stop in a transit section.
 */
@Serializable
data class TransitStop(
    val departure: TransitDeparture? = null,
    val arrival: TransitDeparture? = null,
    val place: TransitPlace? = null
)
