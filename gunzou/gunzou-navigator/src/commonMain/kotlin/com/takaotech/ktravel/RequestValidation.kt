package com.takaotech.ktravel

import com.takaotech.navigator.api.common.GeoPoint
import com.takaotech.navigator.api.here.HereCarRouteRequest
import com.takaotech.navigator.api.here.HereReturnAttribute
import com.takaotech.navigator.api.here.HereTransitRouteRequest
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.plugins.requestvalidation.RequestValidation
import io.ktor.server.plugins.requestvalidation.ValidationResult

private const val MIN_LATITUDE = -90.0
private const val MAX_LATITUDE = 90.0
private const val MIN_LONGITUDE = -180.0
private const val MAX_LONGITUDE = 180.0

// The window HERE accepts for a walking speed. Outside it the API rejects the whole request, so
// catching it here turns a vendor error into one that names the field.
private const val MIN_WALKING_SPEED = 0.5
private const val MAX_WALKING_SPEED = 2.0

/**
 * What is wrong with a request whatever provider is behind it.
 *
 * The split against the per profile checks in
 * [com.takaotech.ktravel.endpoint.requireWithinLimits] is on purpose: a coordinate off the globe or
 * a request for zero routes is nonsense for every engine and is rejected once, here; a ceiling on
 * alternatives is something a specific profile published about itself and is enforced where that
 * descriptor lives. The plugin has no access to the call, so it could not read the descriptor even
 * if it wanted to.
 *
 * The types carry the rest. There is no check that `transportMode` is one of the known modes, or
 * that `time` is one of the three shapes, because a body that got this far already decoded into
 * those types — an invalid one never reaches validation and leaves as a 400 from the deserializer.
 */
fun Application.configureRequestValidation() {
    install(RequestValidation) {
        validate<HereCarRouteRequest> { it.invalidReasons().toValidationResult() }
        validate<HereTransitRouteRequest> { it.invalidReasons().toValidationResult() }
    }
}

/** Everything wrong with a road request, so the caller is told all of it at once. */
private fun HereCarRouteRequest.invalidReasons(): List<String> = buildList {
    addAll(origin.invalidReasons("origin"))
    addAll(destination.invalidReasons("destination"))
    via.forEachIndexed { index, point -> addAll(point.invalidReasons("via[$index]")) }

    if (alternatives < 1) {
        add("alternatives must be at least 1, was $alternatives")
    }
    addAll(returnAttributes.unmetDependencies())
}

/** Everything wrong with a journey request. */
private fun HereTransitRouteRequest.invalidReasons(): List<String> = buildList {
    addAll(origin.invalidReasons("origin"))
    addAll(destination.invalidReasons("destination"))

    if (alternatives < 1) {
        add("alternatives must be at least 1, was $alternatives")
    }
    changes?.takeIf { it < 0 }?.let {
        add("changes cannot be negative, was $it")
    }
    pedestrianSpeedMetersPerSecond?.takeIf { it !in MIN_WALKING_SPEED..MAX_WALKING_SPEED }?.let {
        add("pedestrianSpeedMetersPerSecond must be within $MIN_WALKING_SPEED..$MAX_WALKING_SPEED, was $it")
    }
    pedestrianMaxDistanceMeters?.takeIf { it < 0 }?.let {
        add("pedestrianMaxDistanceMeters cannot be negative, was $it")
    }
}

private fun List<String>.toValidationResult(): ValidationResult =
    if (isEmpty()) ValidationResult.Valid else ValidationResult.Invalid(this)

/** Why this point is not somewhere on the Earth, or nothing at all if it is. */
private fun GeoPoint.invalidReasons(field: String): List<String> = buildList {
    if (lat.isNaN() || lat !in MIN_LATITUDE..MAX_LATITUDE) {
        add("$field.lat must be within $MIN_LATITUDE..$MAX_LATITUDE, was $lat")
    }
    if (lng.isNaN() || lng !in MIN_LONGITUDE..MAX_LONGITUDE) {
        add("$field.lng must be within $MIN_LONGITUDE..$MAX_LONGITUDE, was $lng")
    }
}

/**
 * The attributes HERE will not compute without another one alongside them.
 *
 * Checked here rather than at the upstream call so the caller is told what is wrong with their
 * request instead of being handed a vendor error they would have to look up.
 */
private fun List<HereReturnAttribute>.unmetDependencies(): List<String> = buildList {
    if (HereReturnAttribute.ACTIONS in this@unmetDependencies &&
        HereReturnAttribute.POLYLINE !in this@unmetDependencies
    ) {
        add("returnAttributes: ACTIONS also requires POLYLINE")
    }
    if (HereReturnAttribute.INSTRUCTIONS in this@unmetDependencies &&
        HereReturnAttribute.ACTIONS !in this@unmetDependencies
    ) {
        add("returnAttributes: INSTRUCTIONS also requires ACTIONS")
    }
    if (HereReturnAttribute.TURN_BY_TURN_ACTIONS in this@unmetDependencies &&
        HereReturnAttribute.POLYLINE !in this@unmetDependencies
    ) {
        add("returnAttributes: TURN_BY_TURN_ACTIONS also requires POLYLINE")
    }
}
