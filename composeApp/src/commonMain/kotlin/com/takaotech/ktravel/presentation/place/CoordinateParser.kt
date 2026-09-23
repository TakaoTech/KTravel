package com.takaotech.ktravel.presentation.place

import com.takaotech.ktravel.domain.search.model.GeoCoordinate
import com.takaotech.ktravel.domain.search.model.PlaceCandidate
import com.takaotech.ktravel.domain.search.model.PlaceCandidateSource
import kotlin.math.abs
import kotlin.math.roundToLong

/**
 * Reads a `lat, lng` pair out of what the traveller typed in the search bar.
 *
 * Only a text made of exactly two numbers counts, so a street name with a house number never turns
 * into coordinates. The numbers are separated by a comma, a semicolon or spaces, and use a dot as
 * the decimal separator — the form maps and messaging apps copy coordinates in.
 */
object CoordinateParser {

    private const val MAX_LATITUDE = 90.0
    private const val MAX_LONGITUDE = 180.0

    /** Five decimals of a degree: about a metre. */
    private const val DISPLAY_SCALE = 100_000L
    private const val DISPLAY_DECIMALS = 5

    private val pairPattern = Regex("""^\s*([+-]?\d{1,3}(?:\.\d+)?)\s*(?:[,;]\s*|\s+)([+-]?\d{1,3}(?:\.\d+)?)\s*$""")

    /** The coordinate [text] spells out, or `null` when it is not a pair of degrees in range. */
    fun parse(text: String): GeoCoordinate? {
        val match = pairPattern.matchEntire(text)
        val lat = match?.groupValues?.get(1)?.toDoubleOrNull()
        val lng = match?.groupValues?.get(2)?.toDoubleOrNull()

        val isInRange = lat != null && lng != null && abs(lat) <= MAX_LATITUDE && abs(lng) <= MAX_LONGITUDE
        return if (isInRange) GeoCoordinate(lat = lat, lng = lng) else null
    }

    /**
     * The candidate a typed coordinate becomes.
     *
     * Named after the coordinate itself: it is the only name the place has, it needs no translation,
     * and the traveller can rename it once it is in the trip.
     */
    fun candidateFor(coordinate: GeoCoordinate): PlaceCandidate {
        val title = format(coordinate)
        return PlaceCandidate(
            id = "${PlaceCandidateSource.COORDINATES.name}:$title",
            title = title,
            coordinate = coordinate,
            source = PlaceCandidateSource.COORDINATES,
        )
    }

    /** [coordinate] written as `lat, lng`, with five decimals. */
    fun format(coordinate: GeoCoordinate): String = "${coordinate.lat.format()}, ${coordinate.lng.format()}"

    private fun Double.format(): String {
        val scaled = abs(this * DISPLAY_SCALE).roundToLong()
        val sign = if (this < 0 && scaled != 0L) "-" else ""
        val decimals = (scaled % DISPLAY_SCALE).toString().padStart(DISPLAY_DECIMALS, '0')
        return "$sign${scaled / DISPLAY_SCALE}.$decimals"
    }
}
