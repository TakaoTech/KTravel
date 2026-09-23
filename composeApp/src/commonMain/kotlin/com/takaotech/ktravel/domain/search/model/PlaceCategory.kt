package com.takaotech.ktravel.domain.search.model

/**
 * The kinds of place the traveller can narrow a search to.
 *
 * A subset of the navigator's category groups, chosen for planning a trip rather than for mapping
 * every business: a place in a group this enum does not list simply has no category. Sights and
 * museums are one entry because the contract groups them together, and splitting them here would
 * promise a distinction no provider answers.
 */
enum class PlaceCategory {
    /** Monuments, landmarks, museums and galleries. */
    SIGHTS_AND_MUSEUMS,

    /** Parks, beaches, mountains and other natural places. */
    NATURE,

    /** Restaurants, cafés and bars. */
    EAT_AND_DRINK,

    /** Hotels and every other place to sleep. */
    ACCOMMODATION,
}
