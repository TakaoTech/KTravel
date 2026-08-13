package com.takaotech.navigator.api.response

import kotlinx.serialization.Serializable

/**
 * The drawable shape of a section, in whatever encoding the provider produced it.
 *
 * The encoding travels declared rather than normalized. HERE answers in flexible polyline, Valhalla
 * in Google polyline at precision 6, OSRM in either polyline5 or GeoJSON: re-encoding them all to
 * one of the three on the server would cost a decode and an encode per section and, going to a
 * coarser precision, would round coordinates the provider bothered to compute. The client already
 * has to decode something, so it may as well decode the original.
 *
 * @property encoding How to read [value].
 * @property value The encoded geometry.
 */
@Serializable
data class RouteGeometry(val encoding: PolylineEncoding, val value: String)

/**
 * The polyline encodings this contract can carry.
 *
 * Only [HERE_FLEXIBLE] is produced today; the other two are here because they are what the engines
 * on the roadmap answer with, and a client that switches on this enum should already be written
 * against the full set.
 */
@Serializable
enum class PolylineEncoding {
    /** HERE flexible polyline, which also carries elevation when it was requested. */
    HERE_FLEXIBLE,

    /** Google encoded polyline, five decimal digits of precision. */
    POLYLINE5,

    /** Google encoded polyline, six decimal digits of precision. */
    POLYLINE6,
}
