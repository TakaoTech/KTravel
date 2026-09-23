package com.takaotech.gunzou.here.search.revgeocode.model

/**
 * A kind of result the Reverse Geocode endpoint can be limited to, through the `types` query
 * parameter.
 *
 * @property value Representation the API expects in the query string
 */
enum class RevgeocodeType(val value: String) {
    /** House numbers, streets, postal code points and address blocks. Intersections are not returned. */
    ADDRESS("address"),

    /** Localities and administrative areas, with all their sub-types. */
    AREA("area"),

    /** Localities of type city. */
    CITY("city"),

    /** House numbers: point addresses, micro point addresses and interpolated ones. */
    HOUSE_NUMBER("houseNumber"),

    /** Places. */
    PLACE("place"),

    /** Streets. The only type accepted together with a bearing. */
    STREET("street"),
    ;

    /** Converts the type to its query string representation. */
    fun toQueryString(): String = value
}
