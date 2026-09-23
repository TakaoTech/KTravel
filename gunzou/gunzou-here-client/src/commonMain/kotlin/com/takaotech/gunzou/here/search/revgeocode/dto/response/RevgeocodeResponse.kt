package com.takaotech.gunzou.here.search.revgeocode.dto.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Response of the Reverse Geocode endpoint.
 *
 * @property items Results, most likely first
 */
@Serializable
data class RevgeocodeResponse(@SerialName("items") val items: List<RevgeocodeResultItem>)
