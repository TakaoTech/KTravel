package com.takaotech.gunzou.here.search.lookup.model

/**
 * An optional section of a Lookup result, requested through the `show` query parameter.
 *
 * Only the sections this client reads are listed. Some of them cost HERE an extra call, so request
 * only what is displayed.
 *
 * @property value Representation the API expects in the query string
 */
enum class LookupShowOption(val value: String) {
    /** The ISO 3166-1 alpha-2 and alpha-3 codes of the country, in `countryInfo`. */
    COUNTRY_INFO("countryInfo"),

    /** Pronunciations of address and place names, in `phonemes`. */
    PHONEMES("phonemes"),

    /** The place categories each supplier reference relates to, in `references[].categories`. */
    REFERENCE_CATEGORIES("referenceCategories"),

    /** The street name split into its parts, in `streetInfo`. */
    STREET_INFO("streetInfo"),

    /** The time zone of the result, in `timeZone`. */
    TIME_ZONE("tz"),

    /** Tripadvisor images, editorials and ratings, in `media`. Restricted to HERE contracts. */
    TRIPADVISOR("tripadvisor"),

    /** The sizes of Tripadvisor images, in `media.images`. Restricted to HERE contracts. */
    TRIPADVISOR_IMAGE_VARIANTS("tripadvisorImageVariants"),
    ;

    /** Converts the option to its query string representation. */
    fun toQueryString(): String = value
}
