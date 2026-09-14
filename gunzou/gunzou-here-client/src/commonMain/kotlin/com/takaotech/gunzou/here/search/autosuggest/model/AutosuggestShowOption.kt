package com.takaotech.gunzou.here.search.autosuggest.model

/**
 * An optional section of an Autosuggest result, requested through the `show` query parameter.
 *
 * Only the sections this client reads are listed: `fuel` and `truck`, which are restricted and fill
 * the `extended` section, are left out. Some sections cost HERE an extra call per result, so
 * request only what is displayed.
 *
 * @property value Representation the API expects in the query string
 */
enum class AutosuggestShowOption(val value: String) {
    /** Address and contact details and opening hours of places, in `contacts` and `openingHours`. */
    DETAILS("details"),

    /** Pronunciations of address and place names, in `phonemes`. */
    PHONEMES("phonemes"),

    /** The place categories each supplier reference relates to, in `references[].categories`. */
    REFERENCE_CATEGORIES("referenceCategories"),

    /** The street name split into its parts, in `streetInfo`. */
    STREET_INFO("streetInfo"),

    /** The time zone of each result, in `timeZone`. */
    TIME_ZONE("tz"),
    ;

    /** Converts the option to its query string representation. */
    fun toQueryString(): String = value
}
