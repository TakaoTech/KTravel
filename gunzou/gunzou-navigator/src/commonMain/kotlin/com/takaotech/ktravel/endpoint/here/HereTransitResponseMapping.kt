package com.takaotech.ktravel.endpoint.here

import com.takaotech.navigation.publictransit.dto.response.Notice
import com.takaotech.navigation.publictransit.dto.response.TransitAgency
import com.takaotech.navigation.publictransit.dto.response.TransitDeparture
import com.takaotech.navigation.publictransit.dto.response.TransitPlace
import com.takaotech.navigation.publictransit.dto.response.TransitRouteResponse
import com.takaotech.navigation.publictransit.dto.response.TransitRouteSection
import com.takaotech.navigation.publictransit.dto.response.TransitStop
import com.takaotech.navigation.publictransit.dto.response.TransitTransport
import com.takaotech.navigator.api.common.ProviderId
import com.takaotech.navigator.api.common.ProviderProfile
import com.takaotech.navigator.api.response.NoticeDto
import com.takaotech.navigator.api.response.PolylineEncoding
import com.takaotech.navigator.api.response.RouteGeometry
import com.takaotech.navigator.api.response.RouteSummaryDto
import com.takaotech.navigator.api.response.RouteWaypointDto
import com.takaotech.navigator.api.response.TransitAgencyDto
import com.takaotech.navigator.api.response.TransitJourneyDto
import com.takaotech.navigator.api.response.TransitJourneyResponse
import com.takaotech.navigator.api.response.TransitJourneyStep
import com.takaotech.navigator.api.response.TransitLineDto
import com.takaotech.navigator.api.response.TransitStopDto
import com.takaotech.navigator.api.response.WheelchairAccess

private const val PEDESTRIAN_SECTION_TYPE = "pedestrian"

/** Translates a HERE transit answer into the shape every timetable profile answers in. */
fun TransitRouteResponse.toJourneyResponse(): TransitJourneyResponse = TransitJourneyResponse(
    provider = ProviderId.HERE,
    profile = ProviderProfile.TRANSIT,
    journeys = routes.map { route ->
        val steps = route.sections.map { it.toStep() }
        TransitJourneyDto(
            summary = RouteSummaryDto(
                durationSeconds = steps.sumOf { it.summary.durationSeconds },
                distanceMeters = steps.sumOf { it.summary.distanceMeters },
            ),
            steps = steps,
            notices = route.notices.orEmpty().map { it.toNoticeDto() },
        )
    },
    notices = notices.orEmpty().map { it.toNoticeDto() },
)

/**
 * One step of a journey, as one of the two things a step can be.
 *
 * Upstream discriminates on `type` and so does the contract; this is the one place where that string
 * turns into a branch of the sealed type. A section with no transport on it walks, which is also
 * what an unrecognised one does: a kind of step HERE invents later is still a stretch the traveller
 * covers, and drawing it as a walk is wrong in the icon rather than wrong in the itinerary.
 */
private fun TransitRouteSection.toStep(): TransitJourneyStep {
    val summary = RouteSummaryDto(
        durationSeconds = (travelSummary?.duration ?: 0).toLong(),
        distanceMeters = travelSummary?.length ?: 0,
    )
    // Passed through in HERE's own encoding rather than converted, for the same reason the road
    // profile does it: re-encoding rounds coordinates the router bothered to compute.
    val geometry = polyline?.let { RouteGeometry(encoding = PolylineEncoding.HERE_FLEXIBLE, value = it) }
    val line = transport?.takeUnless { type.equals(PEDESTRIAN_SECTION_TYPE, ignoreCase = true) }

    return if (line == null) {
        TransitJourneyStep.Walk(
            summary = summary,
            geometry = geometry,
            departure = departure?.toWaypointDtoOrNull(),
            arrival = arrival?.toWaypointDtoOrNull(),
        )
    } else {
        TransitJourneyStep.Ride(
            summary = summary,
            line = line.toLineDto(),
            geometry = geometry,
            agency = agency?.toAgencyDtoOrNull(),
            boarding = departure?.toStopDtoOrNull(),
            alighting = arrival?.toStopDtoOrNull(),
            intermediateStops = intermediateStops.orEmpty().mapNotNull { it.toStopDtoOrNull() },
        )
    }
}

private fun TransitTransport.toLineDto(): TransitLineDto = TransitLineDto(
    mode = mode.toTransitMode(),
    name = name ?: shortName,
    shortName = shortName,
    longName = longName,
    category = category,
    headsign = headsign,
    color = color,
    textColor = textColor,
    url = url,
    wheelchairAccessible = wheelchairAccessible.toWheelchairAccess(),
)

/**
 * The operator, or nothing when the feed names none.
 *
 * The name is the one thing this is shown by, so an agency without one is not an agency a user
 * interface can draw; the identifier and the website alone would render as an empty line.
 */
private fun TransitAgency.toAgencyDtoOrNull(): TransitAgencyDto? = name?.let {
    TransitAgencyDto(name = it, id = id, website = website)
}

/**
 * An end of a walk, or nothing when the feed gave no coordinates for it.
 *
 * Omitted rather than defaulted: a placeholder coordinate would put the place off the coast of West
 * Africa, and a client drawing what it was sent has no way to tell that apart from a real one.
 */
private fun TransitDeparture.toWaypointDtoOrNull(): RouteWaypointDto? = place?.location?.let { location ->
    RouteWaypointDto(place = location.toGeoPoint(), time = time?.toZonedTime(), name = place?.name)
}

/**
 * Where a vehicle is boarded or left, under the same rule as [toWaypointDtoOrNull].
 *
 * An end of a section carries one time, and which of the two it stands for follows from the end it
 * is. It is written to `departure` in both cases because a reader takes it through
 * [TransitJourneyStep.Ride.departureTime] and [TransitJourneyStep.Ride.arrivalTime], each of which
 * falls back to the other field; filling both here would state the same instant twice and claim a
 * dwell time of zero that the feed never reported.
 */
private fun TransitDeparture.toStopDtoOrNull(): TransitStopDto? = place?.location?.let { location ->
    TransitStopDto(
        place = location.toGeoPoint(),
        name = place?.name,
        departure = time?.toZonedTime(),
        url = place?.url,
        wheelchairAccessible = place?.wheelchairAccessible.toWheelchairAccess(),
    )
}

/**
 * A stop the vehicle calls at on the way.
 *
 * The place is looked for in three positions because HERE puts it in whichever one the feed
 * supports: on the stop itself, or inside the departure or the arrival at it. Reading only the first
 * finds nothing on the payloads the API actually returns.
 */
private fun TransitStop.toStopDtoOrNull(): TransitStopDto? {
    val stopPlace: TransitPlace? = place ?: departure?.place ?: arrival?.place

    return stopPlace?.location?.let { location ->
        TransitStopDto(
            place = location.toGeoPoint(),
            name = stopPlace.name,
            arrival = arrival?.time?.toZonedTime(),
            departure = departure?.time?.toZonedTime(),
            dwellSeconds = duration,
            offset = offset,
            url = stopPlace.url,
            wheelchairAccessible = stopPlace.wheelchairAccessible.toWheelchairAccess(),
        )
    }
}

/**
 * HERE's accessibility vocabulary, in the contract's.
 *
 * Anything unrecognised is [WheelchairAccess.UNKNOWN] and never [WheelchairAccess.NO]: a word this
 * build has not seen means the answer is not known, and turning that into a refusal would tell a
 * traveller a station is unusable on no evidence at all.
 */
private fun String?.toWheelchairAccess(): WheelchairAccess = when (this?.lowercase()) {
    "yes" -> WheelchairAccess.YES
    "limited" -> WheelchairAccess.LIMITED
    "no" -> WheelchairAccess.NO
    else -> WheelchairAccess.UNKNOWN
}

private fun Notice.toNoticeDto(): NoticeDto = NoticeDto(
    code = code ?: title.orEmpty(),
    title = title,
    severity = severity.toNoticeSeverity(),
)
