package com.takaotech.navigation.routing.dto.response.toll

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Specifies whether this `Fare` is a multi-travel pass, and its characteristics.
 *
 * @property returnJourney This pass includes the fare for the return journey.
 * @property validityPeriod Temporal validity period for the pass.
 * @property travels This pass allows for the specified number of travels.
 * @property transfers Indicates if transfers are permitted with this pass, and if so, how many.
 * @property seniorPass This pass is valid only if presented by a senior person.
 */
@Serializable
data class FarePass(
    @SerialName("returnJourney") val returnJourney: Boolean? = null,
    @SerialName("validityPeriod") val validityPeriod: FarePassValidityPeriod? = null,
    @SerialName("travels") val travels: Int? = null,
    @SerialName("transfers") val transfers: Int? = null,
    @SerialName("seniorPass") val seniorPass: Boolean? = null
)

/**
 * Specifies a temporal validity period for a pass.
 *
 * @property period Extensible enum: `annual` `extendedAnnual` `minutes` `days` `months`.
 * @property count Required if period is `minutes`, `days` or `months`; specifies how many of these units are covered by the pass.
 */
@Serializable
data class FarePassValidityPeriod(
    @SerialName("period") val period: String,
    @SerialName("count") val count: Int? = null
)
