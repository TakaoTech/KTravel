package com.takaotech.ktravel.data.search

import com.takaotech.gunzou.api.common.SearchProviderId
import com.takaotech.gunzou.api.search.PlaceCategoryGroup
import com.takaotech.gunzou.api.search.SearchAddress
import com.takaotech.gunzou.api.search.autocomplete.AutocompleteSuggestion
import com.takaotech.ktravel.domain.search.PlaceSearchProvider
import com.takaotech.ktravel.domain.search.model.GeoCoordinate
import com.takaotech.ktravel.domain.search.model.PlaceCandidate
import com.takaotech.ktravel.domain.search.model.PlaceCandidateSource
import com.takaotech.ktravel.domain.search.model.PlaceCategory
import com.takaotech.ktravel.domain.search.model.PlaceReference

/** A provider as the selector shows it: its brand, not the profile's descriptive name. */
internal fun SearchProviderId.toDomain(): PlaceSearchProvider = PlaceSearchProvider(
    id = value,
    name = when (this) {
        SearchProviderId.Here -> "HERE"
        SearchProviderId.Photon -> "Photon"
        else -> value
    },
)

/**
 * A place suggestion as the screen draws it, with the distance the provider measured.
 *
 * [provider] is the one that answered, recorded in the candidate so its details are asked of the
 * same provider later, whatever the search bar has been switched to since.
 */
internal fun AutocompleteSuggestion.Place.toCandidate(provider: SearchProviderId): PlaceCandidate = PlaceCandidate(
    id = "${PlaceCandidateSource.SEARCH.name}:$id",
    title = title,
    coordinate = GeoCoordinate(lat = position.lat, lng = position.lng),
    source = PlaceCandidateSource.SEARCH,
    locality = address.locality(),
    addressLabel = address.label,
    category = category?.toDomain(),
    distanceMeters = distanceMeters,
    reference = PlaceReference(providerId = provider.value, placeId = id),
)

/** The category a group maps onto, or `null` for the groups a trip planner does not filter by. */
internal fun PlaceCategoryGroup.toDomain(): PlaceCategory? = when (this) {
    PlaceCategoryGroup.SIGHTS_AND_MUSEUMS -> PlaceCategory.SIGHTS_AND_MUSEUMS
    PlaceCategoryGroup.NATURAL_AND_GEOGRAPHICAL -> PlaceCategory.NATURE
    PlaceCategoryGroup.EAT_AND_DRINK -> PlaceCategory.EAT_AND_DRINK
    PlaceCategoryGroup.ACCOMMODATIONS -> PlaceCategory.ACCOMMODATION
    else -> null
}

/** The most precise place name the address has, from the city up to the country. */
internal fun SearchAddress.locality(): String? = city ?: county ?: state ?: countryName
