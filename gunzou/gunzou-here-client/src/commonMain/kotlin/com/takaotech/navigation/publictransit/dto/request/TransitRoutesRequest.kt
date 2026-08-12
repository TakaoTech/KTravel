package com.takaotech.navigation.publictransit.dto.request

import com.takaotech.navigation.common.model.Coordinate
import com.takaotech.navigation.common.model.Units
import com.takaotech.navigation.publictransit.model.ReturnAttribute
import com.takaotech.navigation.publictransit.model.TransitMode
import com.vanniktech.locale.Locale
import kotlinx.datetime.LocalDateTime

/**
 * Request parameters for the Public Transit Routes API.
 *
 * @property origin Trip origin coordinates
 * @property destination Trip destination coordinates
 * @property lang Preferred languages for the response (IETF BCP 47 format)
 * @property units Units of measurement (metric or imperial)
 * @property departureTime Time of departure (RFC 3339 format, e.g., "2019-06-24T01:23:45")
 * @property arrivalTime Time of arrival (RFC 3339 format)
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
    val departureTime: LocalDateTime? = null,
    val arrivalTime: LocalDateTime? = null,
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
