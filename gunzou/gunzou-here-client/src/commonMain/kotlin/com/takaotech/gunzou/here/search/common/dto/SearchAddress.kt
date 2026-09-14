package com.takaotech.gunzou.here.search.common.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Postal address of a HERE search result, split into the components the country uses.
 *
 * @property label The whole address assembled by the regional postal rules, such as
 *   `Schulstraße 4, 32547 Bad Oeynhausen, Germany`
 * @property countryCode ISO 3166-1 alpha-3 country code, such as `DEU`
 * @property countryName Localized country name
 * @property stateCode State code or abbreviation, country specific, such as `CA`
 * @property state First-level division of the country
 * @property countyCode County code or abbreviation, country specific: in Italy the province, `RM`
 * @property county Second-level division of the country
 * @property city Primary locality
 * @property district Division of the city
 * @property subdistrict Division of the district
 * @property street Street name
 * @property streets Names of the crossing streets, when the result is an intersection
 * @property block Block name
 * @property subblock Sub-block name
 * @property postalCode Postal code
 * @property houseNumber House number
 * @property building Building name
 * @property unit Floor and suite details. Browse does not return it; other search endpoints do.
 */
@Serializable
data class SearchAddress(
    @SerialName("label") val label: String? = null,
    @SerialName("countryCode") val countryCode: String? = null,
    @SerialName("countryName") val countryName: String? = null,
    @SerialName("stateCode") val stateCode: String? = null,
    @SerialName("state") val state: String? = null,
    @SerialName("countyCode") val countyCode: String? = null,
    @SerialName("county") val county: String? = null,
    @SerialName("city") val city: String? = null,
    @SerialName("district") val district: String? = null,
    @SerialName("subdistrict") val subdistrict: String? = null,
    @SerialName("street") val street: String? = null,
    @SerialName("streets") val streets: List<String>? = null,
    @SerialName("block") val block: String? = null,
    @SerialName("subblock") val subblock: String? = null,
    @SerialName("postalCode") val postalCode: String? = null,
    @SerialName("houseNumber") val houseNumber: String? = null,
    @SerialName("building") val building: String? = null,
    @SerialName("unit") val unit: String? = null,
)
