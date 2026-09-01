package com.takaotech.gunzou.here.publictransit.dto.request

import com.takaotech.gunzou.here.common.model.Coordinate
import com.takaotech.gunzou.here.common.model.Units
import com.takaotech.gunzou.here.publictransit.model.ReturnAttribute
import com.takaotech.gunzou.here.publictransit.model.TransitMode
import com.vanniktech.locale.Locale
import kotlin.time.Instant

/**
 * Request parameters for the Public Transit Routes API.
 *
 * @property origin Trip origin coordinates
 * @property destination Trip destination coordinates
 * @property lang Preferred languages for the response (IETF BCP 47 format)
 * @property units Units of measurement (metric or imperial)
 * @property departureTime Time of departure. An absolute instant rather than a local date and time:
 *   the API reads a bare `2019-06-24T01:23:45` as local at the origin, and a caller that is not at
 *   the origin has no way to express what it means. It is sent in UTC, which the API accepts and
 *   which cannot be misread.
 * @property arrivalTime Time of arrival, under the same rule as [departureTime]
 * @property alternatives Number of alternative routes (0-5, default 0)
 * @property changes Maximum number of changes/transfers allowed (0-6)
 * @property modes Transit mode filter
 * @property pedestrianSpeed Walking speed in meters per second (0.5-2, default 1)
 * @property pedestrianMaxDistance Maximum walking distance in meters (0-6000, default 2000)
 * @property accessibility Accessibility requirements (e.g., "wheelchair")
 * @property returnAttributes Attributes to include in the response
 */
data class TransitRoutesRequest(
    val origin: Coordinate,
    val destination: Coordinate,
    val lang: List<Locale> = listOf(Locale.from("it-IT")),
    val units: Units = Units.METRIC,
    val departureTime: Instant? = null,
    val arrivalTime: Instant? = null,
    val alternatives: Int = 0,
    val changes: Int? = null,
    val modes: TransitMode? = null,
    val pedestrianSpeed: Double? = null,
    val pedestrianMaxDistance: Int? = null,
    val accessibility: List<String>? = null,
    val returnAttributes: List<ReturnAttribute>? = null,
) {
    init {
        require(lang.isNotEmpty()) { "Language list cannot be empty" }
    }
}
