package com.takaotech.gunzou.here.search.common.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Bounding box of the area or line a result covers. `place` results have none.
 *
 * @property west Longitude of the western side
 * @property south Latitude of the southern side
 * @property east Longitude of the eastern side
 * @property north Latitude of the northern side
 */
@Serializable
data class MapView(
    @SerialName("west") val west: Double,
    @SerialName("south") val south: Double,
    @SerialName("east") val east: Double,
    @SerialName("north") val north: Double,
)
