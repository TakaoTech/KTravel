package com.takaotech.navigator.api.response

import com.takaotech.navigator.api.common.ProviderId
import com.takaotech.navigator.api.common.ProviderProfile
import com.takaotech.navigator.api.common.TravelMode
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * The answer of a road profile, whichever engine computed it.
 *
 * One shape per family of API rather than per provider: a second road engine answers with this same
 * type, because a car route from Valhalla and one from HERE describe the same thing. What does not
 * fit here is a journey on scheduled services, which is [TransitJourneyResponse] — see the note
 * there for why the two were split.
 *
 * [provider] and [profile] are echoed back so a client that fans out to several endpoints can tell
 * the answers apart without tracking which call each one came from.
 *
 * @property provider The engine that computed these routes.
 * @property profile The profile of that engine, that is which of its APIs was called.
 * @property routes The alternatives, best first. Empty is not an answer: a provider that finds
 *   nothing produces [com.takaotech.navigator.api.error.ErrorCode.NO_ROUTE_FOUND] instead.
 * @property notices Advisories about the whole answer, such as an option that was accepted but
 *   could not be honoured. Never a substitute for an error.
 */
@Serializable
data class RoutingRouteResponse(
    @SerialName("provider") val provider: ProviderId,
    @SerialName("profile") val profile: ProviderProfile,
    @SerialName("routes") val routes: List<RoutingRouteDto> = emptyList(),
    @SerialName("notices") val notices: List<NoticeDto> = emptyList(),
)

/**
 * One way of driving, walking or cycling from origin to destination.
 *
 * @property summary Totals for the whole alternative, aggregated by the server over [sections]. The
 *   provider is not asked for it: not all of them return one, and a client that had to sum the
 *   sections itself would be reimplementing the same loop on every platform.
 * @property sections The legs, in travel order — usually one per via waypoint.
 */
@Serializable
data class RoutingRouteDto(
    @SerialName("summary") val summary: RouteSummaryDto,
    @SerialName("sections") val sections: List<RoutingSectionDto> = emptyList(),
)

/**
 * One leg of a road route, travelled in a single [mode].
 *
 * @property summary Totals for this leg.
 * @property mode The vehicle, or the traveller's own power.
 * @property actions The manoeuvres to perform, when the request asked for them.
 * @property departure Where and when this leg starts.
 * @property arrival Where and when this leg ends.
 * @property geometry The drawable shape of this leg.
 * @property tollSystems The authorities collecting on this leg, referenced by index from [tolls].
 * @property tolls What has to be paid on this leg.
 */
@Serializable
data class RoutingSectionDto(
    @SerialName("summary") val summary: RouteSummaryDto,
    @SerialName("mode") val mode: TravelMode,
    @SerialName("actions") val actions: List<RouteActionDto> = emptyList(),
    @SerialName("departure") val departure: RouteWaypointDto? = null,
    @SerialName("arrival") val arrival: RouteWaypointDto? = null,
    @SerialName("geometry") val geometry: RouteGeometry? = null,
    @SerialName("tollSystems") val tollSystems: List<TollSystemDto> = emptyList(),
    @SerialName("tolls") val tolls: List<TollCostDto> = emptyList(),
)
