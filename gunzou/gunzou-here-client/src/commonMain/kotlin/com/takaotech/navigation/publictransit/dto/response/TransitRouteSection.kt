package com.takaotech.navigation.publictransit.dto.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * A section of a transit route. Can be either a transit section or a pedestrian section.
 * The type field indicates which kind of section this is ("transit" or "pedestrian").
 *
 * @property agency The operator of the service. Absent on a pedestrian section, and on the feeds
 *   that do not publish one.
 */
@Serializable
data class TransitRouteSection(
    @SerialName("id") val id: String,
    @SerialName("type") val type: String,
    @SerialName("departure") val departure: TransitDeparture? = null,
    @SerialName("arrival") val arrival: TransitDeparture? = null,
    @SerialName("travelSummary") val travelSummary: TravelSummary? = null,
    @SerialName("polyline") val polyline: String? = null,
    @SerialName("transport") val transport: TransitTransport? = null,
    @SerialName("notices") val notices: List<Notice>? = null,
    @SerialName("intermediateStops") val intermediateStops: List<TransitStop>? = null,
    @SerialName("agency") val agency: TransitAgency? = null,
)

/**
 * Represents an intermediate stop in a transit section.
 *
 * The place is looked for in [place] and then inside [departure] and [arrival], because the API
 * only ever fills one of the three: on the payloads it actually returns the stop itself carries no
 * place and the station is nested in the departure.
 *
 * @property duration How long the vehicle stands at the stop, in seconds.
 * @property offset Index into the section's polyline where this stop falls, which is what puts a
 *   marker on the map without geocoding the stop again.
 */
@Serializable
data class TransitStop(
    @SerialName("departure") val departure: TransitDeparture? = null,
    @SerialName("arrival") val arrival: TransitDeparture? = null,
    @SerialName("place") val place: TransitPlace? = null,
    @SerialName("duration") val duration: Int? = null,
    @SerialName("offset") val offset: Int? = null,
)
