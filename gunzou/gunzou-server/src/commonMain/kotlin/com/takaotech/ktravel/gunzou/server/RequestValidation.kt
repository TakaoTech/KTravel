package com.takaotech.ktravel.gunzou.server

import com.takaotech.gunzou.api.common.GeoPoint
import com.takaotech.gunzou.api.here.HereReturnAttribute
import com.takaotech.gunzou.api.here.HereRoutingRequest
import com.takaotech.gunzou.api.here.HereTransitRouteRequest
import com.takaotech.gunzou.api.search.SearchArea
import com.takaotech.gunzou.api.search.autocomplete.AutocompleteRequest
import com.vanniktech.locale.Country
import com.vanniktech.locale.Locale
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

/** An ISO 3166-1 alpha-2 code as the contract spells it: two uppercase letters. */
private val ALPHA_2_COUNTRY_CODE = Regex("[A-Z]{2}")

/**
 * What is wrong with a request whatever provider is behind it.
 *
 * The split against the per profile checks in
 * [com.takaotech.ktravel.gunzou.server.endpoint.requireWithinLimits] is on purpose: a coordinate off the globe or
 * a request for zero routes is nonsense for every engine and is rejected once, here; a ceiling on
 * alternatives is something a specific profile published about itself and is enforced where that
 * descriptor lives. The plugin has no access to the call, so it could not read the descriptor even
 * if it wanted to.
 *
 * The types carry the rest. There is no check that `time` is one of the three shapes, because a body
 * that got this far already decoded into those types — an invalid one never reaches validation and
 * leaves as a 400 from the deserializer. The vehicle is not checked here either: it is not in the
 * body at all, and the route that reads it out of the path refuses a segment naming no known mode.
 */
fun Application.configureRequestValidation() {
    install(RequestValidation) {
        validate<HereRoutingRequest> { it.invalidReasons().toValidationResult() }
        validate<HereTransitRouteRequest> { it.invalidReasons().toValidationResult() }
        validate<AutocompleteRequest> { it.invalidReasons().toValidationResult() }
    }
}

/** Everything wrong with a road request, so the caller is told all of it at once. */
private fun HereRoutingRequest.invalidReasons(): List<String> = buildList {
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

/**
 * Everything wrong with an autocomplete request whatever provider answers it.
 *
 * The ceiling on `limit` and whether a location is required are the service's own words, and are
 * enforced beside its descriptor instead.
 */
private fun AutocompleteRequest.invalidReasons(): List<String> = buildList {
    if (query.isBlank()) {
        add("query cannot be blank")
    }
    // Checked here rather than left to the provider: the answer is only readable in a language the
    // user speaks, and a tag no provider can parse would otherwise be silently answered in another.
    if (Locale.fromOrNull(language) == null) {
        add("language: '$language' is not an IETF BCP 47 tag, such as it-IT")
    }
    at?.let { addAll(it.invalidReasons("at")) }

    if (limit < 1) {
        add("limit must be at least 1, was $limit")
    }
    area?.let { addAll(it.invalidReasons()) }
}

/** Why this area cannot restrict a search, or nothing at all if it can. */
private fun SearchArea.invalidReasons(): List<String> = buildList {
    when (val area = this@invalidReasons) {
        is SearchArea.Countries -> {
            if (area.codes.isEmpty()) {
                add("area.codes cannot be empty")
            }
            // A code of the right shape can still name no country, and a provider that is handed
            // one either fails the whole request or silently ignores the filter.
            area.codes
                .filterNot { it.matches(ALPHA_2_COUNTRY_CODE) && Country.fromOrNull(it) != null }
                .forEach { add("area.codes: '$it' is not an ISO 3166-1 alpha-2 country code in uppercase") }
        }

        is SearchArea.Circle -> {
            addAll(area.center.invalidReasons("area.center"))
            if (area.radiusMeters <= 0) {
                add("area.radiusMeters must be positive, was ${area.radiusMeters}")
            }
        }

        is SearchArea.BoundingBox -> {
            addAll(GeoPoint(lat = area.south, lng = area.west).invalidReasons("area.southWest"))
            addAll(GeoPoint(lat = area.north, lng = area.east).invalidReasons("area.northEast"))
            if (area.south > area.north) {
                add("area.south cannot be north of area.north, was ${area.south} > ${area.north}")
            }
        }
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
