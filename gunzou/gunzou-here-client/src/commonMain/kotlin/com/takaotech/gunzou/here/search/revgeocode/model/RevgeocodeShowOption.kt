package com.takaotech.gunzou.here.search.revgeocode.model

/**
 * An optional section of a Reverse Geocode result, requested through the `show` query parameter.
 *
 * Only the sections this client reads are listed. Some of them cost HERE an extra call per result,
 * so request only what is displayed.
 *
 * @property value Representation the API expects in the query string
 */
enum class RevgeocodeShowOption(val value: String) {
    /** The ISO 3166-1 alpha-2 and alpha-3 codes of the country, in `countryInfo`. */
    COUNTRY_INFO("countryInfo"),

    /** The street name split into its parts, in `streetInfo`. */
    STREET_INFO("streetInfo"),

    /** The time zone of each result, in `timeZone`. */
    TIME_ZONE("tz"),
    ;

    /** Converts the option to its query string representation. */
    fun toQueryString(): String = value
}
