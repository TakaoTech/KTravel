package com.takaotech.ktravel.endpoint.here

import com.takaotech.navigation.routing.dto.request.DepartureTime
import com.takaotech.navigation.routing.dto.request.RoutesRequest
import com.takaotech.navigation.routing.dto.request.Waypoint
import com.takaotech.navigation.routing.model.AvoidFeature
import com.takaotech.navigation.routing.model.AvoidOptions
import com.takaotech.navigation.routing.model.ReturnAttribute
import com.takaotech.navigation.routing.model.RoutingMode
import com.takaotech.navigation.routing.model.TransportMode
import com.takaotech.navigator.api.common.GeoPoint
import com.takaotech.navigator.api.common.RouteTime
import com.takaotech.navigator.api.common.Units
import com.takaotech.navigator.api.here.HereAvoidFeature
import com.takaotech.navigator.api.here.HereCarRouteRequest
import com.takaotech.navigator.api.here.HereReturnAttribute
import com.takaotech.navigator.api.here.HereRoutingMode
import com.takaotech.navigator.api.here.HereTransportMode
import com.vanniktech.locale.Locale
import kotlin.time.Instant
import com.takaotech.navigation.common.model.Units as HereUnits

// What this server sends to the HERE road API. Its counterpart is HereCarResponseMapping.
//
// The two halves share one decision worth stating once: the enums on the wire are this contract's,
// never HERE's. Copying them costs this file, and buys a contract that does not change under a
// client the day a vendor renames a constant.

/** Builds the vendor request the HERE road API expects. */
fun HereCarRouteRequest.toHereRoutesRequest(): RoutesRequest = RoutesRequest(
    transportMode = transportMode.toHere(),
    origin = origin.toWaypoint(),
    destination = destination.toWaypoint(),
    via = via.map { it.toWaypoint() }.ifEmpty { null },
    routingMode = routingMode.toHere(),
    alternatives = alternatives,
    departureTime = (time as? RouteTime.DepartAt)?.let { DepartureTime.fromInstantUtc(it.instant) },
    arrivalTime = (time as? RouteTime.ArriveBy)?.instant?.toHereTimestamp(),
    avoid = avoid?.features?.takeIf { it.isNotEmpty() }?.let { AvoidOptions(features = it.map { f -> f.toHere() }) },
    units = units.toHere(),
    // Never the server's own locale: RoutesRequest defaults to it, which would answer a caller in
    // whatever language the machine running the server happens to be configured for.
    lang = language?.let(Locale::fromOrNull),
    returnAttributes = returnAttributes.map { it.toHere() } + avoidAsReturnAttributes(),
)

/**
 * Tolls have to be computed to be avoided intelligently, so asking to avoid them implies asking for
 * them back.
 *
 * The avoidance itself travels in `avoid[features]`; this is the other half. HERE treats avoidance
 * as a preference and routes through a toll road where there is no alternative, so a client that
 * asked for a toll-free route still has to be able to show what the answer costs.
 */
private fun HereCarRouteRequest.avoidAsReturnAttributes(): List<ReturnAttribute> =
    if (avoid?.features?.contains(HereAvoidFeature.TOLL_ROAD) == true &&
        HereReturnAttribute.TOLLS !in returnAttributes
    ) {
        listOf(ReturnAttribute.TOLLS)
    } else {
        emptyList()
    }

private fun GeoPoint.toWaypoint(): Waypoint = Waypoint(lat = lat, lng = lng)

private fun HereTransportMode.toHere(): TransportMode = when (this) {
    HereTransportMode.CAR -> TransportMode.CAR
    HereTransportMode.TRUCK -> TransportMode.TRUCK
    HereTransportMode.PEDESTRIAN -> TransportMode.PEDESTRIAN
    HereTransportMode.BICYCLE -> TransportMode.BICYCLE
    HereTransportMode.SCOOTER -> TransportMode.SCOOTER
    HereTransportMode.TAXI -> TransportMode.TAXI
    HereTransportMode.BUS -> TransportMode.BUS
    HereTransportMode.PRIVATE_BUS -> TransportMode.PRIVATE_BUS
}

private fun HereRoutingMode.toHere(): RoutingMode = when (this) {
    HereRoutingMode.FAST -> RoutingMode.FAST
    HereRoutingMode.SHORT -> RoutingMode.SHORT
}

private fun HereAvoidFeature.toHere(): AvoidFeature = when (this) {
    HereAvoidFeature.TOLL_ROAD -> AvoidFeature.TOLL_ROAD
    HereAvoidFeature.CONTROLLED_ACCESS_HIGHWAY -> AvoidFeature.CONTROLLED_ACCESS_HIGHWAY
    HereAvoidFeature.FERRY -> AvoidFeature.FERRY
    HereAvoidFeature.CAR_SHUTTLE_TRAIN -> AvoidFeature.CAR_SHUTTLE_TRAIN
    HereAvoidFeature.TUNNEL -> AvoidFeature.TUNNEL
    HereAvoidFeature.DIRT_ROAD -> AvoidFeature.DIRT_ROAD
    HereAvoidFeature.DIFFICULT_TURNS -> AvoidFeature.DIFFICULT_TURNS
    HereAvoidFeature.U_TURNS -> AvoidFeature.U_TURNS
    HereAvoidFeature.SEASONAL_CLOSURE -> AvoidFeature.SEASONAL_CLOSURE
}

internal fun Units.toHere(): HereUnits = when (this) {
    Units.METRIC -> HereUnits.METRIC
    Units.IMPERIAL -> HereUnits.IMPERIAL
}

private fun HereReturnAttribute.toHere(): ReturnAttribute = when (this) {
    HereReturnAttribute.POLYLINE -> ReturnAttribute.POLYLINE
    HereReturnAttribute.ACTIONS -> ReturnAttribute.ACTIONS
    HereReturnAttribute.INSTRUCTIONS -> ReturnAttribute.INSTRUCTIONS
    HereReturnAttribute.SUMMARY -> ReturnAttribute.SUMMARY
    HereReturnAttribute.TRAVEL_SUMMARY -> ReturnAttribute.TRAVEL_SUMMARY
    HereReturnAttribute.TURN_BY_TURN_ACTIONS -> ReturnAttribute.TURN_BY_TURN_ACTIONS
    HereReturnAttribute.TYPICAL_DURATION -> ReturnAttribute.TYPICAL_DURATION
    HereReturnAttribute.ELEVATION -> ReturnAttribute.ELEVATION
    HereReturnAttribute.TOLLS -> ReturnAttribute.TOLLS
    HereReturnAttribute.INCIDENTS -> ReturnAttribute.INCIDENTS
    HereReturnAttribute.ROUTE_LABELS -> ReturnAttribute.ROUTE_LABELS
}

/**
 * RFC 3339 in UTC, which is what `arrivalTime` is passed as.
 *
 * [Instant.toString] already produces exactly that — `2026-08-13T09:00:00Z` — so no formatter is
 * involved. The contract carries an instant precisely so this is unambiguous: a bare local date and
 * time would be read by HERE as local at the origin, and the server does not know the origin's
 * offset.
 */
private fun Instant.toHereTimestamp(): String = toString()
