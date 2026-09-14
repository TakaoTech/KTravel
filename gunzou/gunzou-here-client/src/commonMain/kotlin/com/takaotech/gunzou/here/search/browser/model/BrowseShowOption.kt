package com.takaotech.gunzou.here.search.browser.model

/**
 * An optional section of a Browse result, requested through the `show` query parameter.
 *
 * Only the sections this client reads are listed. Some of them cost HERE an extra call per result,
 * so request only what is displayed.
 *
 * @property value Representation the API expects in the query string
 */
enum class BrowseShowOption(val value: String) {
    /** The time zone of each result, in `timeZone`. */
    TIME_ZONE("tz"),

    /** The place categories each supplier reference relates to, in `references[].categories`. */
    REFERENCE_CATEGORIES("referenceCategories"),

    /** Pronunciations of address and place names, in `phonemes`. */
    PHONEMES("phonemes"),

    /** The street name split into its parts, in `streetInfo`. */
    STREET_INFO("streetInfo"),

    /** Tripadvisor images, editorials and ratings, in `media`. Restricted to HERE contracts. */
    TRIPADVISOR("tripadvisor"),

    /** The sizes of Tripadvisor images, in `media.images`. Restricted to HERE contracts. */
    TRIPADVISOR_IMAGE_VARIANTS("tripadvisorImageVariants"),
    ;

    /** Converts the option to its query string representation. */
    fun toQueryString(): String = value
}
