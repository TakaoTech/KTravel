package com.takaotech.navigation.routing.dto.response.toll

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Price of a fare. Can be either a [SinglePrice] or a [RangePrice].
 */
@Serializable
sealed class FarePrice {

    /**
     * A single price value for a toll fare.
     *
     * @property type Type of price (`value`).
     * @property estimated `true` if the fare price is estimated, `false` if it is an exact value.
     * @property currency Local currency of the price compliant to ISO 4217.
     * @property unit When set, the price is paid for a specific duration (in seconds).
     * @property value The price value.
     */
    @Serializable
    @SerialName("value")
    data class SinglePrice(
        @SerialName("type") val type: String,
        @SerialName("estimated") val estimated: Boolean = false,
        @SerialName("currency") val currency: String,
        @SerialName("unit") val unit: Int? = null,
        @SerialName("value") val value: Double
    ) : FarePrice()

    /**
     * A range price value for a toll fare.
     *
     * @property type Type of price (`range`).
     * @property estimated `true` if the fare price is estimated, `false` if it is an exact value.
     * @property currency Local currency of the price compliant to ISO 4217.
     * @property unit When set, the price is paid for a specific duration (in seconds).
     * @property minimum Minimum price.
     * @property maximum Maximum price.
     */
    @Serializable
    @SerialName("range")
    data class RangePrice(
        @SerialName("type") val type: String,
        @SerialName("estimated") val estimated: Boolean = false,
        @SerialName("currency") val currency: String,
        @SerialName("unit") val unit: Int? = null,
        @SerialName("minimum") val minimum: Double,
        @SerialName("maximum") val maximum: Double
    ) : FarePrice()
}
