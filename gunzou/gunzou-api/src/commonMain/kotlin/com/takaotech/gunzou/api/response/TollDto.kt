package com.takaotech.gunzou.api.response

import com.takaotech.gunzou.api.common.GeoPoint
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * An authority collecting tolls along a section.
 *
 * Carried once per section and referenced by index from [TollCostDto.tollSystemRefs], because a
 * single system charges for many of the costs in the same section and repeating its name on each of
 * them would be pure duplication.
 *
 * @property id The system's identifier in the provider's namespace.
 * @property name Display name, when the provider has one.
 */
@Serializable
data class TollSystemDto(@SerialName("id") val id: String, @SerialName("name") val name: String? = null)

/**
 * One payment due along a section.
 *
 * @property tollSystemRefs Indices into the section's [RouteSectionDto.tollSystems]. A single cost
 *   can belong to more than one system where two authorities share a stretch of road.
 * @property countryCode ISO 3166-1 alpha-3 code of the country collecting it.
 * @property fares The prices that may apply, since what is actually charged depends on the time of
 *   day, the payment method and the vehicle. A client that wants one number takes the first.
 * @property collectionLocations Where the money is taken, which for a distance based toll is both
 *   the entry and the exit gate.
 */
@Serializable
data class TollCostDto(
    @SerialName("tollSystemRefs") val tollSystemRefs: List<Int> = emptyList(),
    @SerialName("countryCode") val countryCode: String? = null,
    @SerialName("fares") val fares: List<TollFareDto> = emptyList(),
    @SerialName("collectionLocations") val collectionLocations: List<GeoPoint> = emptyList(),
)

/**
 * One price that may be charged for a [TollCostDto].
 *
 * @property price What it costs.
 * @property name The fare's name, such as the class of vehicle it applies to.
 * @property paymentMethods How it can be paid, in the provider's vocabulary.
 */
@Serializable
data class TollFareDto(
    @SerialName("price") val price: TollPriceDto,
    @SerialName("name") val name: String? = null,
    @SerialName("paymentMethods") val paymentMethods: List<String> = emptyList(),
)

/**
 * The amount of a fare.
 *
 * [minimum] and [maximum] are equal for a fare quoted as a single figure, so a client formats one
 * shape instead of switching on a sealed hierarchy for a difference it usually renders identically.
 *
 * @property currency ISO 4217 code.
 * @property minimum Lowest amount that can be charged.
 * @property maximum Highest amount that can be charged.
 * @property estimated True when the provider computed the figure rather than reading a published
 *   tariff, which is worth telling the user before they budget on it.
 */
@Serializable
data class TollPriceDto(
    @SerialName("currency") val currency: String,
    @SerialName("minimum") val minimum: Double,
    @SerialName("maximum") val maximum: Double,
    @SerialName("estimated") val estimated: Boolean = false,
)
