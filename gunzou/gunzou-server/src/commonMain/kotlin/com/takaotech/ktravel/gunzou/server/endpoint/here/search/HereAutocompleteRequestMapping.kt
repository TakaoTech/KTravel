package com.takaotech.ktravel.gunzou.server.endpoint.here.search

import com.takaotech.gunzou.api.common.GeoPoint
import com.takaotech.gunzou.api.error.ErrorCode
import com.takaotech.gunzou.api.search.SearchArea
import com.takaotech.gunzou.api.search.autocomplete.AutocompleteRequest
import com.takaotech.gunzou.here.common.model.Coordinate
import com.takaotech.gunzou.here.search.autosuggest.dto.request.AutosuggestRequest
import com.takaotech.ktravel.gunzou.server.endpoint.NavigatorException
import com.vanniktech.locale.Country
import com.vanniktech.locale.Locale
import com.takaotech.gunzou.here.search.common.model.SearchArea as HereSearchArea

// What this server sends to HERE Autosuggest.

/**
 * Builds the vendor request HERE Autosuggest expects.
 *
 * Called after the request is validated and the descriptor's limits are enforced: the vendor DTO
 * checks the same constraints in its constructor, and a request that reached it unchecked would fail
 * as an internal error instead of an [ErrorCode.INVALID_REQUEST] naming the field.
 *
 * @throws NavigatorException [ErrorCode.INVALID_REQUEST] for a language that is not a locale.
 *   Request validation already refuses it; this is what keeps a gap there from becoming an internal
 *   error here.
 */
internal fun AutocompleteRequest.toHereAutosuggestRequest(): AutosuggestRequest = AutosuggestRequest(
    query = query,
    at = at?.toCoordinate(),
    area = area?.toHere(),
    lang = listOf(
        Locale.fromOrNull(language) ?: throw NavigatorException(
            code = ErrorCode.INVALID_REQUEST,
            message = "language: '$language' is not an IETF BCP 47 tag",
        ),
    ),
    limit = limit,
)

private fun GeoPoint.toCoordinate(): Coordinate = Coordinate(lat = lat, lng = lng)

/**
 * The same area in HERE's terms, where countries are named by their alpha-3 code.
 *
 * @throws NavigatorException [ErrorCode.INVALID_REQUEST] for a country code that names no country.
 *   Request validation already refuses them; this is what keeps a gap there from becoming an
 *   internal error here.
 */
private fun SearchArea.toHere(): HereSearchArea = when (this) {
    is SearchArea.Countries -> HereSearchArea.Countries(codes.map { it.toAlpha3() })
    is SearchArea.Circle -> HereSearchArea.Circle(center = center.toCoordinate(), radiusMeters = radiusMeters)
    is SearchArea.BoundingBox -> HereSearchArea.BoundingBox(west = west, south = south, east = east, north = north)
}

private fun String.toAlpha3(): String = Country.fromOrNull(this)?.code3
    ?: throw NavigatorException(
        code = ErrorCode.INVALID_REQUEST,
        message = "area.codes: '$this' is not an ISO 3166-1 alpha-2 country code",
    )
