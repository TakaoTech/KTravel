package com.takaotech.ktravel.domain.search.model

/**
 * A place the traveller may add to the trip, before it becomes one.
 *
 * Whatever produced it — a search, the places around the map, typed coordinates — the screen draws
 * and selects candidates the same way, which is why they share one type and [source] only says where
 * one came from.
 *
 * @property id Stable for the same place from the same source, so a selection survives the list
 *   being refreshed.
 * @property title The name the place is added to the trip with.
 * @property coordinate Where the place is.
 * @property source What produced the candidate.
 * @property locality The city or town, when the source knows it.
 * @property addressLabel The full address as the provider writes it.
 * @property category The kind of place, when the source knows it and it is one the trip filters by.
 * @property distanceMeters From the point the search was made around, when the source measured it.
 * @property reference The provider and identifier its details are read by, when the candidate came
 *   from a provider. Unlike [id], which only has to be unique on screen, it is what the provider
 *   itself knows the place as.
 */
data class PlaceCandidate(
    val id: String,
    val title: String,
    val coordinate: GeoCoordinate,
    val source: PlaceCandidateSource,
    val locality: String? = null,
    val addressLabel: String? = null,
    val category: PlaceCategory? = null,
    val distanceMeters: Long? = null,
    // TODO Check what server respond to this
    val reference: PlaceReference? = null,
)

/** Where a [PlaceCandidate] came from. */
enum class PlaceCandidateSource {
    /** A search the traveller typed. */
    SEARCH,

    /** The places inside the framed area of the map. */
    NEARBY,

    /** A `lat, lng` pair the traveller typed instead of a name. */
    COORDINATES,
}
