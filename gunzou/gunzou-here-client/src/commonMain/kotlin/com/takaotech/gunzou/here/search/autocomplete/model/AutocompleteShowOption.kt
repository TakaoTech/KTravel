package com.takaotech.gunzou.here.search.autocomplete.model

/**
 * An optional section of an Autocomplete result, requested through the `show` query parameter.
 *
 * `hasRelatedMPA` is not listed: it is restricted to HERE contracts and still in alpha.
 *
 * @property value Representation the API expects in the query string
 */
enum class AutocompleteShowOption(val value: String) {
    /** The street name split into its parts, in `streetInfo`. */
    STREET_INFO("streetInfo"),
    ;

    /** Converts the option to its query string representation. */
    fun toQueryString(): String = value
}
