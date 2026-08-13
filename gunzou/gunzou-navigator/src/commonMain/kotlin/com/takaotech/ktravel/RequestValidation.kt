package com.takaotech.ktravel

import com.takaotech.navigator.api.common.GeoPoint
import com.takaotech.navigator.api.here.HereCarRouteRequest
import com.takaotech.navigator.api.here.HereReturnAttribute
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.plugins.requestvalidation.RequestValidation
import io.ktor.server.plugins.requestvalidation.ValidationResult

private const val MIN_LATITUDE = -90.0
private const val MAX_LATITUDE = 90.0
private const val MIN_LONGITUDE = -180.0
private const val MAX_LONGITUDE = 180.0

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
        validate<HereCarRouteRequest> { request ->
            val reasons = buildList {
                addAll(request.origin.invalidReasons("origin"))
                addAll(request.destination.invalidReasons("destination"))
                request.via.forEachIndexed { index, point -> addAll(point.invalidReasons("via[$index]")) }

                if (request.alternatives < 1) {
                    add("alternatives must be at least 1, was ${request.alternatives}")
                }
                addAll(request.returnAttributes.unmetDependencies())
            }

            if (reasons.isEmpty()) ValidationResult.Valid else ValidationResult.Invalid(reasons)
        }
    }
}

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
