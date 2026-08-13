package com.takaotech.navigator.api.here

import com.takaotech.navigator.api.common.GeoPoint
import com.takaotech.navigator.api.common.RouteTime
import com.takaotech.navigator.api.common.TransitMode
import com.takaotech.navigator.api.common.Units
import kotlinx.serialization.Serializable

/**
 * Body of `POST /v1/here/transit`, the public transport profile of HERE.
 *
 * A separate path from [HereCarRouteRequest] because it is a separate upstream API, on its own host
 * and with parameters that have no meaning on the road one — there is no `changes` on a car route
 * and no `routingMode` on a timetable.
 *
 * @property origin Where the journey starts.
 * @property destination Where it ends.
 * @property alternatives How many journeys to return, at least one.
 * @property time When the traveller wants to travel. Unlike the road profile this is the parameter
 *   that decides the answer: a timetable at 03:00 is a different journey from the same one at 08:00.
 * @property modes Which kinds of vehicle may or may not be used.
 * @property changes Most transfers the traveller will accept. Null leaves it to the provider.
 * @property pedestrianSpeedMetersPerSecond How fast the traveller walks between stops, which shifts
 *   every connection the journey depends on.
 * @property pedestrianMaxDistanceMeters How far the traveller is willing to walk in one leg.
 * @property language IETF BCP 47 tag for the response text, such as `it-IT`.
 * @property units Units for the text the provider localizes.
 */
@Serializable
data class HereTransitRouteRequest(
    val origin: GeoPoint,
    val destination: GeoPoint,
    val alternatives: Int = 1,
    val time: RouteTime = RouteTime.Now,
    val modes: HereTransitModeFilter? = null,
    val changes: Int? = null,
    val pedestrianSpeedMetersPerSecond: Double? = null,
    val pedestrianMaxDistanceMeters: Int? = null,
    val language: String? = null,
    val units: Units = Units.METRIC,
)

/**
 * Which kinds of vehicle a journey may use.
 *
 * Both lists may be given at once: HERE reads them as an allow list narrowed by a deny list. An
 * empty filter and no filter at all mean the same thing, so a caller with nothing to say omits it.
 *
 * [com.takaotech.navigator.api.common.TransitMode.OTHER] carries no meaning here — it exists so an
 * unknown mode in a *response* still decodes — and the server ignores it in either list.
 *
 * @property include Kinds the journey is allowed to use. Empty means all of them.
 * @property exclude Kinds the journey must not use.
 */
@Serializable
data class HereTransitModeFilter(
    val include: List<TransitMode> = emptyList(),
    val exclude: List<TransitMode> = emptyList(),
)
