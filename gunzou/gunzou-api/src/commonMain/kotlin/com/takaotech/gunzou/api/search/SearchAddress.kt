package com.takaotech.gunzou.api.search

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Postal address of a search result, reduced to the components every provider can fill.
 *
 * Every component is optional: a country has no street, a street has no house number, and a provider
 * may simply not know one. An absent component means unknown or not applicable, never empty.
 *
 * @property label The whole address as a single line, assembled by the provider following the
 *   country's postal rules, such as `Via del Corso 1, 00186 Roma RM, Italia`.
 * @property countryCode ISO 3166-1 alpha-2 country code, such as `IT`.
 * @property countryName Country name, in the language of the result.
 * @property state First-level division of the country: a region in Italy.
 * @property county Second-level division of the country: a province in Italy.
 * @property city Primary locality.
 * @property district Division of the city.
 * @property street Street name.
 * @property postalCode Postal code.
 * @property houseNumber House number, as text since many are not numbers.
 */
@Serializable
data class SearchAddress(
    @SerialName("label") val label: String? = null,
    @SerialName("countryCode") val countryCode: String? = null,
    @SerialName("countryName") val countryName: String? = null,
    @SerialName("state") val state: String? = null,
    @SerialName("county") val county: String? = null,
    @SerialName("city") val city: String? = null,
    @SerialName("district") val district: String? = null,
    @SerialName("street") val street: String? = null,
    @SerialName("postalCode") val postalCode: String? = null,
    @SerialName("houseNumber") val houseNumber: String? = null,
)
