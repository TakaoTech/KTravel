package com.takaotech.ktravel.endpoint.here

import com.takaotech.navigation.publictransit.dto.response.TransitDeparture
import com.takaotech.navigation.publictransit.dto.response.TransitRouteResponse
import com.takaotech.navigation.publictransit.dto.response.TransitRouteSection
import com.takaotech.navigation.publictransit.dto.response.TransitStop
import com.takaotech.navigator.api.common.ProviderId
import com.takaotech.navigator.api.common.ProviderProfile
import com.takaotech.navigator.api.common.TravelMode
import com.takaotech.navigator.api.response.NoticeDto
import com.takaotech.navigator.api.response.PolylineEncoding
import com.takaotech.navigator.api.response.RouteDto
import com.takaotech.navigator.api.response.RouteGeometry
import com.takaotech.navigator.api.response.RouteResponse
import com.takaotech.navigator.api.response.RouteSectionDto
import com.takaotech.navigator.api.response.RouteSummaryDto
import com.takaotech.navigator.api.response.RouteWaypointDto
import com.takaotech.navigator.api.response.TransitDetailsDto
import com.takaotech.navigator.api.response.TransitStopDto

private const val PEDESTRIAN_SECTION_TYPE = "pedestrian"

/** Translates a HERE transit answer into the response every profile shares. */
fun TransitRouteResponse.toRouteResponse(): RouteResponse = RouteResponse(
    provider = ProviderId.HERE,
    profile = ProviderProfile.TRANSIT,
    routes = routes.map { route ->
        val sections = route.sections.map { it.toSectionDto() }
        RouteDto(
            summary = RouteSummaryDto(
                durationSeconds = sections.sumOf { it.summary.durationSeconds },
                distanceMeters = sections.sumOf { it.summary.distanceMeters },
            ),
            sections = sections,
        )
    },
    notices = notices.orEmpty().map { notice ->
        NoticeDto(
            code = notice.code ?: notice.title.orEmpty(),
            title = notice.title,
            severity = notice.severity.toNoticeSeverity(),
        )
    },
)

/**
 * One leg of a journey.
 *
 * The walking legs and the vehicle legs become the same [RouteSectionDto], told apart by
 * [RouteSectionDto.mode] and by whether [RouteSectionDto.transit] is populated. That is the design
 * decision this profile exists to test, and it holds: nothing here needs a section type the road
 * profile does not already have.
 */
private fun TransitRouteSection.toSectionDto(): RouteSectionDto {
    val isPedestrian = type.equals(PEDESTRIAN_SECTION_TYPE, ignoreCase = true)

    return RouteSectionDto(
        summary = RouteSummaryDto(
            durationSeconds = (travelSummary?.duration ?: 0).toLong(),
            distanceMeters = travelSummary?.length ?: 0,
        ),
        mode = if (isPedestrian) TravelMode.PEDESTRIAN else TravelMode.TRANSIT,
        departure = departure?.toWaypointDtoOrNull(),
        arrival = arrival?.toWaypointDtoOrNull(),
        geometry = polyline?.let { RouteGeometry(encoding = PolylineEncoding.HERE_FLEXIBLE, value = it) },
        transit = transport?.takeUnless { isPedestrian }?.let { transport ->
            TransitDetailsDto(
                mode = transport.mode.toTransitMode(),
                name = transport.name ?: transport.shortName,
                category = transport.category,
                headsign = transport.headsign,
                // HERE reports the operator on some feeds only and the vendor DTO does not model it:
                // this stays null until :gunzou-here-client carries an agency.
                agency = null,
                color = transport.color,
                textColor = transport.textColor,
                intermediateStops = intermediateStops.orEmpty().mapNotNull { it.toStopDtoOrNull() },
            )
        },
    )
}

/**
 * An end of a leg, or nothing when the feed gave no coordinates for it.
 *
 * Omitted rather than defaulted: a placeholder coordinate would put the stop off the coast of West
 * Africa, and a client drawing what it was sent has no way to tell that apart from a real place.
 */
private fun TransitDeparture.toWaypointDtoOrNull(): RouteWaypointDto? = place?.location?.let { location ->
    RouteWaypointDto(
        place = location.toGeoPoint(),
        time = time?.toZonedTime(),
        name = place?.name,
    )
}

/**
 * An intermediate stop, under the same rule as [toWaypointDtoOrNull].
 *
 * The place is looked for in three positions because HERE puts it in whichever one the feed
 * supports: on the stop itself, or inside the departure or the arrival at it. Reading only the first
 * finds nothing on the payloads the API actually returns.
 */
private fun TransitStop.toStopDtoOrNull(): TransitStopDto? {
    val stopPlace = place ?: departure?.place ?: arrival?.place
    val location = stopPlace?.location ?: return null

    return TransitStopDto(
        place = location.toGeoPoint(),
        name = stopPlace.name,
        arrival = arrival?.time?.toZonedTime(),
        departure = departure?.time?.toZonedTime(),
    )
}
