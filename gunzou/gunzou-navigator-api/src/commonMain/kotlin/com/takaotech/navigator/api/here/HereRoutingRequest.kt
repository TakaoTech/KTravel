package com.takaotech.navigator.api.here

import com.takaotech.navigator.api.common.GeoPoint
import com.takaotech.navigator.api.common.RouteTime
import com.takaotech.navigator.api.common.Units
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Body of `POST /v1/here/routing/{transportMode}`, the road profile of HERE.
 *
 * One body for every vehicle: car, pedestrian, bicycle and truck routes all come from the same
 * upstream endpoint and ask for the same things. What tells them apart is the path — one per mode,
 * built with [com.takaotech.navigator.api.NavigatorApi.hereRouting] — so the vehicle is deliberately
 * absent from the fields below: it is named once, where the request is addressed.
 *
 * @property origin Where the route starts.
 * @property destination Where it ends.
 * @property via Stops along the way, in the order they must be visited.
 * @property routingMode What to optimize for.
 * @property alternatives How many routes to return, at least one.
 * @property time When the traveller wants to be moving.
 * @property units Units for the text the provider localizes. The numbers in the response stay
 *   metric either way.
 * @property language IETF BCP 47 tag for the instructions, such as `it-IT`. Null lets the server
 *   decide, which today means the provider's own default.
 * @property avoid What the route should keep off, when there is anything.
 * @property returnAttributes What to compute besides the route itself.
 */
@Serializable
data class HereRoutingRequest(
    @SerialName("origin") val origin: GeoPoint,
    @SerialName("destination") val destination: GeoPoint,
    @SerialName("via") val via: List<GeoPoint> = emptyList(),
    @SerialName("routingMode") val routingMode: HereRoutingMode = HereRoutingMode.FAST,
    @SerialName("alternatives") val alternatives: Int = 1,
    @SerialName("time") val time: RouteTime = RouteTime.Now,
    @SerialName("units") val units: Units = Units.METRIC,
    @SerialName("language") val language: String? = null,
    @SerialName("avoid") val avoid: HereAvoidOptions? = null,
    @SerialName("returnAttributes") val returnAttributes: List<HereReturnAttribute> = HereReturnAttribute.NAVIGATION,
)
