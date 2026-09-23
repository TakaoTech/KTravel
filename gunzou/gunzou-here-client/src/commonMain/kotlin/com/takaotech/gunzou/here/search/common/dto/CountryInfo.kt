package com.takaotech.gunzou.here.search.common.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Country codes of a result, only with `show=countryInfo`.
 *
 * @property alpha2 ISO 3166-1 alpha-2 country code, such as `IT`
 * @property alpha3 ISO 3166-1 alpha-3 country code, such as `ITA`
 */
@Serializable
data class CountryInfo(
    @SerialName("alpha2") val alpha2: String? = null,
    @SerialName("alpha3") val alpha3: String? = null,
)
