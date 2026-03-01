package com.takaotech.navigation.routing.dto.response.toll

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Transponder system for which a fare is applicable.
 *
 * @property system Transponder system name.
 */
@Serializable
data class TransponderSystem(
    @SerialName("system") val system: String? = null
)

/**
 * Contains information about a single toll fare needed for this section of the route.
 *
 * @property id Unique Fare id. Used to deduplicate fares that apply to multiple sections.
 * @property name Name of a toll fare. **Deprecated**: use `TollCost.tollSystems` instead.
 * @property price The fare price.
 * @property convertedPrice The fare price converted to another currency.
 * @property reason Extensible enum: `ride` `parking` `toll`. Reason for the cost described in this `Fare` element.
 * @property paymentMethods Specifies the payment methods for which this fare is valid.
 * @property pass Specifies whether this `Fare` is a multi-travel pass, and its characteristics.
 * @property applicableTimes Specifies the time domain when this fare is valid. If missing, the fare is always valid.
 * @property transponders List of transponder systems for which this fare is applicable.
 */
@Serializable
data class TollFare(
    @SerialName("id") val id: String,
    @SerialName("name") val name: String,
    @SerialName("price") val price: FarePrice,
    @SerialName("convertedPrice") val convertedPrice: FarePrice? = null,
    @SerialName("reason") val reason: String? = null,
    @SerialName("paymentMethods") val paymentMethods: List<String>? = null,
    @SerialName("pass") val pass: FarePass? = null,
    @SerialName("applicableTimes") val applicableTimes: String? = null,
    @SerialName("transponders") val transponders: List<TransponderSystem>? = null
)
