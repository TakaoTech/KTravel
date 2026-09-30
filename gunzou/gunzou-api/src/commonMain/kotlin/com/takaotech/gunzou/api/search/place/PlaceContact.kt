package com.takaotech.gunzou.api.search.place

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline

/**
 * One way to reach a place.
 *
 * @property kind What [value] is: a phone number, a website...
 * @property value The number, the address or the URL, as the provider writes it. A phone number is
 *   usually in international format, such as `+39 06 3996 7700`, but it is not guaranteed to be.
 * @property label What the provider says it is for, such as a department, when it says.
 */
@Serializable
data class PlaceContact(
    @SerialName("kind") val kind: ContactKind,
    @SerialName("value") val value: String,
    @SerialName("label") val label: String? = null,
)

/**
 * What a [PlaceContact] is.
 *
 * A string rather than an enum, so a kind added by a newer server still decodes; a client shows a
 * contact of a kind it does not know as plain text, or not at all.
 *
 * @property value The kind as it travels on the wire.
 */
@Serializable
@JvmInline
value class ContactKind(val value: String) {
    override fun toString(): String = value

    /** The kinds this contract knows. */
    companion object {
        /** A landline phone number. */
        val PHONE = ContactKind("phone")

        /** A mobile phone number. */
        val MOBILE = ContactKind("mobile")

        /** A web page, as an absolute URL. */
        val WEBSITE = ContactKind("website")

        /** An email address. */
        val EMAIL = ContactKind("email")
    }
}
