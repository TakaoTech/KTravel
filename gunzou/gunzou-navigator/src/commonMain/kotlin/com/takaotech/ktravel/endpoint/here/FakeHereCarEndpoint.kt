package com.takaotech.ktravel.endpoint.here

import com.takaotech.ktravel.endpoint.ProviderCredentials
import com.takaotech.ktravel.endpoint.requireWithinLimits
import com.takaotech.navigator.api.common.GeoPoint
import com.takaotech.navigator.api.common.TravelMode
import com.takaotech.navigator.api.here.HereCarRouteRequest
import com.takaotech.navigator.api.here.HereTransportMode
import com.takaotech.navigator.api.response.NoticeDto
import com.takaotech.navigator.api.response.RouteDto
import com.takaotech.navigator.api.response.RouteResponse
import com.takaotech.navigator.api.response.RouteSectionDto
import com.takaotech.navigator.api.response.RouteSummaryDto
import com.takaotech.navigator.api.response.RouteWaypointDto
import kotlin.math.asin
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.math.roundToLong
import kotlin.math.sin
import kotlin.math.sqrt

private const val EARTH_RADIUS_METERS = 6_371_008.8
private const val DEGREES_TO_RADIANS = 0.017453292519943295
private const val DETOUR_PER_ALTERNATIVE = 0.15

private const val DRIVING_SPEED_METERS_PER_SECOND = 22.2
private const val CYCLING_SPEED_METERS_PER_SECOND = 4.5
private const val WALKING_SPEED_METERS_PER_SECOND = 1.4

/**
 * A [HereCarEndpoint] that answers from the request alone, without HERE behind it.
 *
 * It exists so the routes, the error mapping and the client can be built and tested against the real
 * contract before a single upstream call is made — and so the whole suite keeps running without a
 * key, on a machine with no network, at no cost per run. Every route it returns is a straight line
 * at a constant speed, which is wrong as navigation and exactly right as a fixture: the same request
 * always produces the same bytes.
 *
 * The answer is marked with [FAKE_NOTICE_CODE] so it can never be mistaken for a real one, in a log
 * or in a screenshot.
 */
class FakeHereCarEndpoint : HereCarEndpoint {

    override suspend fun route(request: HereCarRouteRequest, credentials: ProviderCredentials?): RouteResponse {
        descriptor.requireWithinLimits(
            alternatives = request.alternatives,
            viaCount = request.via.size,
            time = request.time,
        )

        val legs = buildList {
            add(request.origin)
            addAll(request.via)
            add(request.destination)
        }.zipWithNext()

        val mode = request.transportMode.toTravelMode()
        val speed = request.transportMode.metersPerSecond()

        return RouteResponse(
            provider = descriptor.provider,
            profile = descriptor.profile,
            routes = (0 until request.alternatives).map { alternative ->
                buildRoute(legs = legs, mode = mode, speed = speed, alternative = alternative)
            },
            notices = listOf(
                NoticeDto(
                    code = FAKE_NOTICE_CODE,
                    title = "Straight line estimate: no routing engine was called",
                ),
            ),
        )
    }

    /**
     * Builds one alternative.
     *
     * Later alternatives are stretched by [DETOUR_PER_ALTERNATIVE] so a caller asking for several
     * gets answers it can tell apart and order, which is what a client displaying a list has to do.
     */
    private fun buildRoute(
        legs: List<Pair<GeoPoint, GeoPoint>>,
        mode: TravelMode,
        speed: Double,
        alternative: Int,
    ): RouteDto {
        val stretch = 1.0 + alternative * DETOUR_PER_ALTERNATIVE
        val sections = legs.map { (from, to) ->
            val distance = greatCircleMeters(from, to) * stretch
            RouteSectionDto(
                summary = RouteSummaryDto(
                    durationSeconds = (distance / speed).roundToLong(),
                    distanceMeters = distance.roundToInt(),
                ),
                mode = mode,
                departure = RouteWaypointDto(place = from),
                arrival = RouteWaypointDto(place = to),
            )
        }

        return RouteDto(
            summary = RouteSummaryDto(
                durationSeconds = sections.sumOf { it.summary.durationSeconds },
                distanceMeters = sections.sumOf { it.summary.distanceMeters },
            ),
            sections = sections,
        )
    }

    /** The marker every fake answer carries. */
    companion object {
        /** Notice code identifying an answer that no routing engine produced. */
        const val FAKE_NOTICE_CODE: String = "fakeProviderStraightLine"
    }
}

/** Maps the requested HERE mode onto the mode the response is expressed in. */
private fun HereTransportMode.toTravelMode(): TravelMode = when (this) {
    HereTransportMode.CAR -> TravelMode.CAR
    HereTransportMode.TRUCK -> TravelMode.TRUCK
    HereTransportMode.TAXI -> TravelMode.TAXI
    HereTransportMode.BUS, HereTransportMode.PRIVATE_BUS -> TravelMode.BUS
    HereTransportMode.PEDESTRIAN -> TravelMode.PEDESTRIAN
    HereTransportMode.BICYCLE -> TravelMode.BICYCLE
    HereTransportMode.SCOOTER -> TravelMode.SCOOTER
}

/** A plausible constant speed per mode, so a walking estimate is not returned at motorway pace. */
private fun HereTransportMode.metersPerSecond(): Double = when (this) {
    HereTransportMode.PEDESTRIAN -> WALKING_SPEED_METERS_PER_SECOND
    HereTransportMode.BICYCLE, HereTransportMode.SCOOTER -> CYCLING_SPEED_METERS_PER_SECOND
    else -> DRIVING_SPEED_METERS_PER_SECOND
}

/**
 * Distance along the surface of the Earth between two points, by the haversine formula.
 *
 * Accurate enough for a fixture and, unlike a flat approximation, it does not fall apart near the
 * poles or across the antimeridian — a fake that returned a nonsensical distance for a valid request
 * would fail tests that have nothing to do with it.
 */
private fun greatCircleMeters(from: GeoPoint, to: GeoPoint): Double {
    val fromLat = from.lat * DEGREES_TO_RADIANS
    val toLat = to.lat * DEGREES_TO_RADIANS
    val deltaLat = (to.lat - from.lat) * DEGREES_TO_RADIANS
    val deltaLng = (to.lng - from.lng) * DEGREES_TO_RADIANS

    val a = sin(deltaLat / 2) * sin(deltaLat / 2) +
        cos(fromLat) * cos(toLat) * sin(deltaLng / 2) * sin(deltaLng / 2)

    return 2 * EARTH_RADIUS_METERS * asin(min(1.0, sqrt(a)))
}
