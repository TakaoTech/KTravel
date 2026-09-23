package com.takaotech.ktravel.presentation.place

import com.takaotech.ktravel.domain.model.StepDomain
import com.takaotech.ktravel.domain.model.TravelDayDomain
import com.takaotech.ktravel.domain.model.TravelPlanDomain
import com.takaotech.ktravel.domain.search.model.GeoCoordinate

/**
 * Where the map should open for adding places to [dayId], or to the trip when it is `null`.
 *
 * The last place already planned is where the next one is most likely to be: the day's own places
 * first, then the trip's backlog, then any other day. A trip with no place at all opens on
 * [MapCamera.DEFAULT].
 */
internal fun TravelPlanDomain.openingCamera(dayId: String?): MapCamera {
    val day = days.firstOrNull { it.id == dayId }
    val anchor = day?.lastCoordinate()
        ?: places.lastOrNull()?.let { GeoCoordinate(lat = it.lat, lng = it.lng) }
        ?: days.asReversed().firstNotNullOfOrNull { it.lastCoordinate() }

    return anchor?.let { MapCamera(target = it, zoom = MapCamera.AREA_ZOOM) } ?: MapCamera.DEFAULT
}

private fun TravelDayDomain.lastCoordinate(): GeoCoordinate? =
    places.lastOrNull()?.let { GeoCoordinate(lat = it.lat, lng = it.lng) }
        ?: steps.filterIsInstance<StepDomain.Place>().lastOrNull()?.let { GeoCoordinate(lat = it.lat, lng = it.lng) }
