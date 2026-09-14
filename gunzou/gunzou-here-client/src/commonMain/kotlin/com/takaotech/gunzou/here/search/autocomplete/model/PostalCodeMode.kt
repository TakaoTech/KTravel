package com.takaotech.gunzou.here.search.autocomplete.model

/**
 * How a postal code shared by more than one city or district is completed, through the
 * `postalCodeMode` query parameter.
 *
 * Without a mode HERE returns a single result for such a postal code, whose city or district may be
 * blank or a default name.
 *
 * @property value Representation the API expects in the query string
 */
enum class PostalCodeMode(val value: String) {
    /** One result for each city the postal code spans. */
    CITY_LOOKUP("cityLookup"),

    /** One result for each district the postal code spans, within one city or across several. */
    DISTRICT_LOOKUP("districtLookup"),
    ;

    /** Converts the mode to its query string representation. */
    fun toQueryString(): String = value
}
