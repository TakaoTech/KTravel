package com.takaotech.ktravel.data.search

import com.takaotech.gunzou.api.common.SearchProviderId
import com.takaotech.gunzou.api.search.PlaceCategoryGroup
import com.takaotech.gunzou.api.search.autocomplete.AutocompleteSuggestion
import com.takaotech.ktravel.domain.search.PlaceSearchProvider
import com.takaotech.ktravel.domain.search.model.GeoCoordinate
import com.takaotech.ktravel.domain.search.model.PlaceCandidate
import com.takaotech.ktravel.domain.search.model.PlaceCandidateSource
import com.takaotech.ktravel.domain.search.model.PlaceCategory

/** A provider as the selector shows it: its brand, not the profile's descriptive name. */
internal fun SearchProviderId.toDomain(): PlaceSearchProvider = PlaceSearchProvider(
    id = value,
    name = when (this) {
        SearchProviderId.Here -> "HERE"
        SearchProviderId.Photon -> "Photon"
        else -> value
    },
)

/** A place suggestion as the screen draws it, with the distance the provider measured. */
internal fun AutocompleteSuggestion.Place.toCandidate(): PlaceCandidate = PlaceCandidate(
    id = "${PlaceCandidateSource.SEARCH.name}:$id",
    title = title,
    coordinate = GeoCoordinate(lat = position.lat, lng = position.lng),
    source = PlaceCandidateSource.SEARCH,
    locality = address.city ?: address.county ?: address.state ?: address.countryName,
    addressLabel = address.label,
    category = category?.toDomain(),
    distanceMeters = distanceMeters,
)

/** The category a group maps onto, or `null` for the groups a trip planner does not filter by. */
internal fun PlaceCategoryGroup.toDomain(): PlaceCategory? = when (this) {
    PlaceCategoryGroup.SIGHTS_AND_MUSEUMS -> PlaceCategory.SIGHTS_AND_MUSEUMS
    PlaceCategoryGroup.NATURAL_AND_GEOGRAPHICAL -> PlaceCategory.NATURE
    PlaceCategoryGroup.EAT_AND_DRINK -> PlaceCategory.EAT_AND_DRINK
    PlaceCategoryGroup.ACCOMMODATIONS -> PlaceCategory.ACCOMMODATION
    else -> null
}
