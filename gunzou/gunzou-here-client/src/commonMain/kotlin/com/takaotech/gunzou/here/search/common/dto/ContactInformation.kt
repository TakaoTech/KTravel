package com.takaotech.gunzou.here.search.common.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * One set of contacts of a place, grouped by channel.
 *
 * @property phone Landline numbers
 * @property mobile Mobile numbers
 * @property tollFree Toll-free numbers
 * @property fax Fax numbers
 * @property www Web sites
 * @property email Email addresses
 */
@Serializable
data class ContactInformation(
    @SerialName("phone") val phone: List<Contact>? = null,
    @SerialName("mobile") val mobile: List<Contact>? = null,
    @SerialName("tollFree") val tollFree: List<Contact>? = null,
    @SerialName("fax") val fax: List<Contact>? = null,
    @SerialName("www") val www: List<Contact>? = null,
    @SerialName("email") val email: List<Contact>? = null,
)

/**
 * A single contact of a place.
 *
 * @property value The number, address or URL, as the channel it is listed under implies
 * @property label What the contact is for, such as `Customer Service` or `Pharmacy Fax`
 * @property categories Place categories the contact applies to, when not the whole place
 */
@Serializable
data class Contact(
    @SerialName("value") val value: String,
    @SerialName("label") val label: String? = null,
    @SerialName("categories") val categories: List<CategoryRef>? = null,
)
