package com.takaotech.gunzou.api.common

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * A point on the Earth in the WGS84 coordinate system.
 *
 * Deliberately unvalidated: an out of range coordinate must reach the caller as an
 * [com.takaotech.gunzou.api.error.ErrorResponse] with
 * [com.takaotech.gunzou.api.error.ErrorCode.INVALID_REQUEST], which is what the server's request
 * validation produces. A `require` here would instead surface as a decoding failure, before any
 * handler runs, and the client would get an untyped 400.
 *
 * @property lat Latitude, expected in `-90..90`.
 * @property lng Longitude, expected in `-180..180`.
 */
@Serializable
data class GeoPoint(@SerialName("lat") val lat: Double, @SerialName("lng") val lng: Double)
