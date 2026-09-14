package com.takaotech.gunzou.api.search

import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline

/**
 * What a search result is: a place, or one of the levels of an address.
 *
 * A string rather than an enum because providers grow this vocabulary without notice — HERE says so
 * in its own specification — and a result of a kind this client does not know is still worth
 * showing by its title.
 *
 * The values follow the HERE result types, which are the finest grained of the providers this
 * contract has been designed against.
 *
 * @property value The type as it travels on the wire.
 */
@Serializable
@JvmInline
value class SearchResultType(val value: String) {
    override fun toString(): String = value

    /** The result types this contract knows. */
    companion object {
        /** A point of interest: a business, a landmark, a station. */
        val PLACE = SearchResultType("place")

        /** A single address, down to the house number. */
        val HOUSE_NUMBER = SearchResultType("houseNumber")

        /** A street, as a whole. */
        val STREET = SearchResultType("street")

        /** The crossing of two or more streets. */
        val INTERSECTION = SearchResultType("intersection")

        /** A postal code, either as a point or as the area it covers. */
        val POSTAL_CODE = SearchResultType("postalCode")

        /** A city, a district or a subdistrict. */
        val LOCALITY = SearchResultType("locality")

        /** A country, a state or a county. */
        val ADMINISTRATIVE_AREA = SearchResultType("administrativeArea")

        /** A block or sub-block, in the countries whose addresses use them. */
        val ADDRESS_BLOCK = SearchResultType("addressBlock")
    }
}
