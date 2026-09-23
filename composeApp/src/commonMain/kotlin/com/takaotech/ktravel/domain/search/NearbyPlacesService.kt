package com.takaotech.ktravel.domain.search

import com.takaotech.ktravel.core.annotation.OpenForMokkery
import com.takaotech.ktravel.domain.search.model.GeoArea
import com.takaotech.ktravel.domain.search.model.PlaceCandidate
import com.takaotech.ktravel.domain.search.model.PlaceCategory

/**
 * The places worth visiting inside the area the traveller has framed on the map.
 *
 * Browsing rather than searching: nothing is typed, the map is the query.
 */
@OpenForMokkery
interface NearbyPlacesService {

    /**
     * The places inside [area], closest to its centre first.
     *
     * @param category Only places of this kind, or every kind when `null`.
     * @throws PlaceSearchFailure when the places could not be fetched.
     */
    suspend fun placesIn(area: GeoArea, category: PlaceCategory?): List<PlaceCandidate>
}
