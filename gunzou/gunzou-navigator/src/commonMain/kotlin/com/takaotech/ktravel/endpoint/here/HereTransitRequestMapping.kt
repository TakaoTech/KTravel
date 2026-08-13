package com.takaotech.ktravel.endpoint.here

import com.takaotech.navigation.common.model.Coordinate
import com.takaotech.navigation.publictransit.dto.request.TransitRoutesRequest
import com.takaotech.navigation.publictransit.model.ReturnAttribute
import com.takaotech.navigator.api.common.RouteTime
import com.takaotech.navigator.api.here.HereTransitModeFilter
import com.takaotech.navigator.api.here.HereTransitRouteRequest
import com.vanniktech.locale.Locale
import com.takaotech.navigation.publictransit.model.TransitMode as HereTransitModes

// What this server sends to the HERE public transport API.
//
// Kept apart from the road one on purpose. The two share nothing upstream — different host,
// different parameters, a different answer — and the only thing they have in common is what they
// produce, which is the point being demonstrated: two unrelated APIs, one RouteResponse.

/**
 * What the transit profile always asks HERE for.
 *
 * Unlike the road profile there is no per request choice: every field these produce has a home in
 * the contract, and a journey without its geometry, its intermediate stops or its travel summary is
 * not one a client can draw.
 */
private val TRANSIT_RETURN_ATTRIBUTES = listOf(
    ReturnAttribute.POLYLINE,
    ReturnAttribute.TRAVEL_SUMMARY,
    ReturnAttribute.INTERMEDIATE,
)

/** English, so an unset language does not silently answer in whatever the vendor default is. */
private val DEFAULT_LANGUAGES = listOf(Locale.from("en-GB"))

/** Builds the vendor request the HERE transit API expects. */
fun HereTransitRouteRequest.toHereTransitRequest(): TransitRoutesRequest = TransitRoutesRequest(
    origin = Coordinate(lat = origin.lat, lng = origin.lng),
    destination = Coordinate(lat = destination.lat, lng = destination.lng),
    // The vendor request requires a non empty list and defaults to Italian. Neither is right on a
    // server, so the caller's language is used when there is one and English when there is not.
    lang = listOfNotNull(language?.let(Locale::fromOrNull)).ifEmpty { DEFAULT_LANGUAGES },
    units = units.toHere(),
    departureTime = (time as? RouteTime.DepartAt)?.instant,
    arrivalTime = (time as? RouteTime.ArriveBy)?.instant,
    alternatives = alternatives,
    changes = changes,
    modes = modes?.toHere(),
    pedestrianSpeed = pedestrianSpeedMetersPerSecond,
    pedestrianMaxDistance = pedestrianMaxDistanceMeters,
    returnAttributes = TRANSIT_RETURN_ATTRIBUTES,
)

/** Narrows the journey to the kinds of vehicle the caller will accept. */
private fun HereTransitModeFilter.toHere(): HereTransitModes = HereTransitModes(
    include = include.mapNotNull { it.toHereOrNull() },
    exclude = exclude.mapNotNull { it.toHereOrNull() },
)
