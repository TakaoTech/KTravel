package com.takaotech.navigation.routing.dto.response.toll

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Information for a single toll payment.
 *
 * @property tollSystem The name of the toll system collecting the toll.
 *   **Deprecated**: use [tollSystems] to get all the systems.
 * @property tollSystemRef Reference index of the affected toll system in the `tollSystems` array.
 *   **Deprecated**: use [tollSystems] to get all the systems.
 * @property tollSystems Reference indices of the associated toll system(s). These indices correspond
 *   to the `tollSystems` array in the enclosing section. A toll cost may be associated with multiple systems.
 * @property countryCode ISO-3166-1 alpha-3 country code.
 * @property tollCollectionLocations The toll places representing the location(s) where the fare is collected.
 *   For tolls measured by distance, both the entry and exit toll locations are returned.
 * @property fares The list of possible fares that may apply for the tolls. The specific fares can vary
 *   based on factors such as the time of day, payment method, and vehicle characteristics.
 */
@Serializable
data class TollCost(
    @SerialName("tollSystem") val tollSystem: String,
    @SerialName("tollSystemRef") val tollSystemRef: Int,
    @SerialName("tollSystems") val tollSystems: List<Int>? = null,
    @SerialName("countryCode") val countryCode: String? = null,
    @SerialName("tollCollectionLocations") val tollCollectionLocations: List<TollCollectionLocation>? = null,
    @SerialName("fares") val fares: List<TollFare>
)
