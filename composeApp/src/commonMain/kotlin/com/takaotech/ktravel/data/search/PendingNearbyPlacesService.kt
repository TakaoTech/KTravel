package com.takaotech.ktravel.data.search

import com.takaotech.ktravel.core.logging.AppLogger
import com.takaotech.ktravel.di.PlanningGraphScope
import com.takaotech.ktravel.domain.search.NearbyPlacesService
import com.takaotech.ktravel.domain.search.model.GeoArea
import com.takaotech.ktravel.domain.search.model.PlaceCandidate
import com.takaotech.ktravel.domain.search.model.PlaceCategory
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject

/**
 * The places around the map, until the navigator can list them: always none.
 *
 * Bound so the screen can be built and tested against the real contract today; replacing it is the
 * only change the feature needs once the endpoint exists.
 */
// TODO Replace with a NavigatorNearbyPlacesService once gunzou serves a browse endpoint:
//  - contract in `gunzou-api/search/browse/`: a request carrying `SearchArea.BoundingBox`, the
//    `language`, a `limit` and an optional list of `PlaceCategoryGroup`, answered with the same
//    `AutocompleteSuggestion.Place` shape (or a shared `SearchPlace`) so the mapping in
//    NavigatorSearchMapping.kt is reused;
//  - a `SearchService.BROWSE` profile per provider in `SearchProfile`, advertised by
//    `/v1/search/profiles` and mounted in the Ktor `Routing.kt` next to autocomplete (HERE Browse
//    API for `here`);
//  - a `NavigatorClient` call, dispatched on the provider picked in the search bar, with the same
//    `callWithRecovery` and error mapping as NavigatorPlaceSearchService;
//  - PlaceCategory mapped back onto PlaceCategoryGroup for the filter.
@ContributesBinding(PlanningGraphScope::class)
@Inject
class PendingNearbyPlacesService(appLogger: AppLogger) : NearbyPlacesService {

    private val logger = appLogger.withTag("PendingNearbyPlacesService")

    override suspend fun placesIn(area: GeoArea, category: PlaceCategory?): List<PlaceCandidate> {
        logger.d { "No browse endpoint yet: no places for $area, category $category" }
        return emptyList()
    }
}
