package com.takaotech.gunzou.here.publictransit.dto.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Transport information for a transit section.
 *
 * @property url Where the operator publishes the state of this line.
 * @property wheelchairAccessible Whether the vehicle can carry a wheelchair, in HERE's own
 *   vocabulary: `yes`, `no`, `limited` or `unknown`.
 */
@Serializable
data class TransitTransport(
    @SerialName("mode") val mode: String? = null,
    @SerialName("name") val name: String? = null,
    @SerialName("category") val category: String? = null,
    @SerialName("color") val color: String? = null,
    @SerialName("textColor") val textColor: String? = null,
    @SerialName("headsign") val headsign: String? = null,
    @SerialName("shortName") val shortName: String? = null,
    @SerialName("longName") val longName: String? = null,
    @SerialName("url") val url: String? = null,
    @SerialName("wheelchairAccessible") val wheelchairAccessible: String? = null,
)
