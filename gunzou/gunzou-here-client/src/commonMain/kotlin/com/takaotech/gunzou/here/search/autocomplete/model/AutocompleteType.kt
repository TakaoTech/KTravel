package com.takaotech.gunzou.here.search.autocomplete.model

/**
 * A kind of result the Autocomplete endpoint can be limited to, through the `types` query parameter.
 *
 * @property value Representation the API expects in the query string
 */
enum class AutocompleteType(val value: String) {
    /** House numbers, streets, postal code points and intersections. */
    ADDRESS("address"),

    /** Localities and administrative areas, with all their sub-types. */
    AREA("area"),

    /** Localities of type city. */
    CITY("city"),

    /** House numbers, both point addresses and interpolated ones. */
    HOUSE_NUMBER("houseNumber"),

    /** Postal code points and localities of type postal code. */
    POSTAL_CODE("postalCode"),

    /** Streets. */
    STREET("street"),
    ;

    /** Converts the type to its query string representation. */
    fun toQueryString(): String = value
}
