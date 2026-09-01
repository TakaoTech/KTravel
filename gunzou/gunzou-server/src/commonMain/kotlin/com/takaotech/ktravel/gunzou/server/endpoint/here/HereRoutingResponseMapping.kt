package com.takaotech.ktravel.gunzou.server.endpoint.here

import com.takaotech.gunzou.api.common.GeoPoint
import com.takaotech.gunzou.api.common.ProviderId
import com.takaotech.gunzou.api.common.ProviderProfile
import com.takaotech.gunzou.api.common.TravelMode
import com.takaotech.gunzou.api.response.NoticeDto
import com.takaotech.gunzou.api.response.PolylineEncoding
import com.takaotech.gunzou.api.response.RouteActionDto
import com.takaotech.gunzou.api.response.RouteGeometry
import com.takaotech.gunzou.api.response.RouteSummaryDto
import com.takaotech.gunzou.api.response.RouteWaypointDto
import com.takaotech.gunzou.api.response.RoutingRouteDto
import com.takaotech.gunzou.api.response.RoutingRouteResponse
import com.takaotech.gunzou.api.response.RoutingSectionDto
import com.takaotech.gunzou.api.response.TollCostDto
import com.takaotech.gunzou.api.response.TollFareDto
import com.takaotech.gunzou.api.response.TollPriceDto
import com.takaotech.gunzou.api.response.TollSystemDto
import com.takaotech.gunzou.here.common.dto.Location
import com.takaotech.gunzou.here.routing.dto.response.RouterRouteResponse
import com.takaotech.gunzou.here.routing.dto.response.RouterSection
import com.takaotech.gunzou.here.routing.dto.response.VehicleDeparture
import com.takaotech.gunzou.here.routing.dto.response.toll.FarePrice
import com.takaotech.gunzou.here.routing.dto.response.toll.TollCost
import com.takaotech.gunzou.here.routing.model.TransportMode

/**
 * Translates a HERE road answer into the shape every road profile answers in.
 *
 * Moved here from `HereRoutingProvider` in the app: it is the same translation, and this is where it
 * belongs now that nothing but the server sees a HERE DTO.
 */
fun RouterRouteResponse.toRoutingRouteResponse(): RoutingRouteResponse = RoutingRouteResponse(
    provider = ProviderId.HERE,
    profile = ProviderProfile.ROUTING,
    routes = routes.map { route ->
        val sections = route.sections.map { it.toSectionDto() }
        RoutingRouteDto(summary = sections.aggregateSummary(), sections = sections)
    },
    notices = notices.orEmpty().map { notice ->
        NoticeDto(
            code = notice.code?.name ?: notice.title,
            title = notice.title,
            severity = notice.severity.toNoticeSeverity(),
        )
    },
)

/**
 * Totals for a whole alternative.
 *
 * Summed here rather than asked of HERE, which reports no route level summary: doing it on the
 * server means every client gets the same number instead of each one writing the same loop.
 */
private fun List<RoutingSectionDto>.aggregateSummary(): RouteSummaryDto = RouteSummaryDto(
    durationSeconds = sumOf { it.summary.durationSeconds },
    distanceMeters = sumOf { it.summary.distanceMeters },
    // Only reported when every section has it: a partial sum would understate the traffic delay
    // without anything saying so.
    baseDurationSeconds = takeIf { sections -> sections.all { it.summary.baseDurationSeconds != null } }
        ?.sumOf { it.summary.baseDurationSeconds ?: 0 },
)

private fun RouterSection.toSectionDto(): RoutingSectionDto = RoutingSectionDto(
    summary = RouteSummaryDto(
        durationSeconds = (summary?.duration ?: 0).toLong(),
        distanceMeters = summary?.length ?: 0,
        baseDurationSeconds = summary?.baseDuration?.toLong(),
    ),
    mode = transport.mode.toTravelMode(),
    actions = actions.orEmpty().map { action ->
        RouteActionDto(
            action = action.action,
            durationSeconds = action.duration.toLong(),
            distanceMeters = action.length,
            instruction = action.instruction,
            offset = action.offset,
            direction = action.direction,
            severity = action.severity,
        )
    },
    departure = departure.toWaypointDto(),
    arrival = arrival.toWaypointDto(),
    // Passed through in HERE's own encoding rather than converted. Re-encoding to a coarser
    // precision would round coordinates the router bothered to compute, and every client has to
    // decode something anyway.
    geometry = polyline?.let { RouteGeometry(encoding = PolylineEncoding.HERE_FLEXIBLE, value = it) },
    tollSystems = tollSystems.orEmpty().map { TollSystemDto(id = it.id, name = it.name) },
    tolls = tolls.orEmpty().map { it.toTollCostDto() },
)

private fun VehicleDeparture.toWaypointDto(): RouteWaypointDto = RouteWaypointDto(
    place = place.location.toGeoPoint(),
    time = time?.toZonedTime(),
    name = place.name,
)

internal fun Location.toGeoPoint(): GeoPoint = GeoPoint(lat = lat, lng = lng)

private fun TransportMode.toTravelMode(): TravelMode = when (this) {
    TransportMode.CAR -> TravelMode.CAR
    TransportMode.TRUCK -> TravelMode.TRUCK
    TransportMode.TAXI -> TravelMode.TAXI
    TransportMode.BUS, TransportMode.PRIVATE_BUS -> TravelMode.BUS
    TransportMode.PEDESTRIAN -> TravelMode.PEDESTRIAN
    TransportMode.BICYCLE -> TravelMode.BICYCLE
    TransportMode.SCOOTER -> TravelMode.SCOOTER
}

/**
 * A toll payment.
 *
 * HERE's deprecated single `tollSystemRef` is folded into the plural list rather than carried
 * beside it: the contract is new, and there is no reason for it to be born with two ways of saying
 * the same thing.
 */
private fun TollCost.toTollCostDto(): TollCostDto = TollCostDto(
    tollSystemRefs = tollSystems ?: listOf(tollSystemRef),
    countryCode = countryCode,
    fares = fares.map { fare ->
        TollFareDto(
            price = fare.price.toPriceDto(),
            name = fare.name,
            paymentMethods = fare.paymentMethods.orEmpty(),
        )
    },
    collectionLocations = tollCollectionLocations.orEmpty().map { it.location.toGeoPoint() },
)

/**
 * Flattens HERE's two price shapes into one.
 *
 * A single value becomes a range whose ends are equal, so a client formats one thing. Nothing is
 * lost: a caller that needs to know whether the price was quoted as a range compares the two ends.
 */
private fun FarePrice.toPriceDto(): TollPriceDto = when (this) {
    is FarePrice.SinglePrice -> TollPriceDto(
        currency = currency,
        minimum = value,
        maximum = value,
        estimated = estimated,
    )

    is FarePrice.RangePrice -> TollPriceDto(
        currency = currency,
        minimum = minimum,
        maximum = maximum,
        estimated = estimated,
    )
}
