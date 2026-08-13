package com.takaotech.navigator.api.response

import com.takaotech.navigator.api.common.GeoPoint
import com.takaotech.navigator.api.common.TransitMode
import com.takaotech.navigator.api.common.ZonedTime
import kotlinx.serialization.Serializable

/**
 * The scheduled service operating a section, present only on
 * [com.takaotech.navigator.api.common.TravelMode.TRANSIT] legs.
 *
 * @property mode The kind of vehicle.
 * @property name The line as the traveller sees it on the vehicle, such as `M1` or `62`.
 * @property category The operator's own wording for [mode], such as `Metro` or `Regional train`,
 *   which is often what the signage says and is worth showing over a translated enum.
 * @property headsign The destination shown on the front of the vehicle, which is how a traveller
 *   tells apart the two directions of the same line.
 * @property agency Who runs the service.
 * @property color Line colour as a `#RRGGBB` string, when the agency publishes one.
 * @property textColor Colour to draw on top of [color], for the same reason.
 * @property intermediateStops The stops between boarding and alighting, when the request asked for
 *   them. Empty means they were not requested, not that the vehicle runs non stop.
 */
@Serializable
data class TransitDetailsDto(
    val mode: TransitMode,
    val name: String? = null,
    val category: String? = null,
    val headsign: String? = null,
    val agency: TransitAgencyDto? = null,
    val color: String? = null,
    val textColor: String? = null,
    val intermediateStops: List<TransitStopDto> = emptyList(),
)

/**
 * The operator of a transit service.
 *
 * @property name Display name, the only field a provider always has.
 * @property id The operator's identifier in the provider's own namespace.
 * @property website Where to check for disruptions.
 */
@Serializable
data class TransitAgencyDto(val name: String, val id: String? = null, val website: String? = null)

/**
 * A stop the vehicle calls at between boarding and alighting.
 *
 * @property place Where it is.
 * @property name The name on the sign.
 * @property arrival When the vehicle gets there.
 * @property departure When it leaves, which differs from [arrival] at stops with a dwell time.
 */
@Serializable
data class TransitStopDto(
    val place: GeoPoint,
    val name: String? = null,
    val arrival: ZonedTime? = null,
    val departure: ZonedTime? = null,
)
