package com.takaotech.ktravel.data.routing

import com.takaotech.ktravel.domain.routing.RoutingProviderSettings
import com.takaotech.ktravel.domain.routing.model.Route
import com.takaotech.ktravel.domain.routing.model.RouteAction
import com.takaotech.ktravel.domain.routing.model.RouteDeparture
import com.takaotech.ktravel.domain.routing.model.RouteLocation
import com.takaotech.ktravel.domain.routing.model.RouteSection
import com.takaotech.ktravel.domain.routing.model.RouteSummary
import com.takaotech.ktravel.domain.routing.model.RouteTollCost
import com.takaotech.ktravel.domain.routing.model.RouteTollSystem
import com.takaotech.ktravel.domain.routing.model.RouteTransport
import com.takaotech.ktravel.domain.routing.model.Routes
import com.takaotech.navigator.api.common.GeoPoint
import com.takaotech.navigator.api.common.RouteTime
import com.takaotech.navigator.api.common.ZonedTime
import com.takaotech.navigator.api.here.HereCarRouteRequest
import com.takaotech.navigator.api.here.HereReturnAttribute
import com.takaotech.navigator.api.here.HereRoutingMode
import com.takaotech.navigator.api.here.HereTransportMode
import com.takaotech.navigator.api.response.RouteResponse
import com.takaotech.navigator.api.response.RouteSectionDto
import com.takaotech.navigator.api.response.TollCostDto
import io.nacular.measured.units.Length
import io.nacular.measured.units.times
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.UtcOffset
import kotlinx.datetime.format
import kotlinx.datetime.format.DateTimeComponents
import kotlinx.datetime.toInstant
import kotlin.time.Duration.Companion.seconds

// Between the app's own routing model and the navigator contract.
//
// It is the only place in the app that knows the contract exists. Everything above it works in
// domain types and cannot tell which engine, or which machine, produced the answer.

/**
 * Builds the request for the road profile out of the plan's settings.
 *
 * The settings model has one field per option because that is what the settings screen edits; the
 * contract has one request per provider API. This is where the two meet, and it is the only thing
 * that has to change when the app starts offering a second profile.
 */
fun RoutingProviderSettings.Here.toCarRouteRequest(origin: GeoPoint, destination: GeoPoint): HereCarRouteRequest =
    HereCarRouteRequest(
        origin = origin,
        destination = destination,
        transportMode = transportMode.toContract(),
        routingMode = routingMode.toContract(),
        alternatives = alternatives,
        time = departureRouteTime(),
        returnAttributes = HereReturnAttribute.NAVIGATION_WITH_TOLLS,
    )

/**
 * When the traveller wants to leave.
 *
 * The settings carry a date and a time separately and either can be unset, so only both together
 * mean anything — which is the behaviour the previous provider had, spelled out here instead of
 * being implied by a pair of null checks. The instant is resolved in the device's own timezone,
 * which is the one the user was looking at when they picked it.
 */
private fun RoutingProviderSettings.Here.departureRouteTime(): RouteTime {
    val date = departureDate
    val time = departureTime

    return if (date == null || time == null) {
        RouteTime.Now
    } else {
        RouteTime.DepartAt(LocalDateTime(date, time).toInstant(TimeZone.currentSystemDefault()))
    }
}

private fun RoutingProviderSettings.Here.HereTransportMode.toContract(): HereTransportMode = when (this) {
    RoutingProviderSettings.Here.HereTransportMode.CAR -> HereTransportMode.CAR
    RoutingProviderSettings.Here.HereTransportMode.PEDESTRIAN -> HereTransportMode.PEDESTRIAN
    RoutingProviderSettings.Here.HereTransportMode.BICYCLE -> HereTransportMode.BICYCLE
    RoutingProviderSettings.Here.HereTransportMode.SCOOTER -> HereTransportMode.SCOOTER
}

private fun RoutingProviderSettings.Here.HereRoutingMode.toContract(): HereRoutingMode = when (this) {
    RoutingProviderSettings.Here.HereRoutingMode.FAST -> HereRoutingMode.FAST
    RoutingProviderSettings.Here.HereRoutingMode.SHORT -> HereRoutingMode.SHORT
}

/**
 * Reads a `lat,lng` pair, which is how the plan stores a place.
 *
 * @throws IllegalArgumentException when the string is not a coordinate, which is a bug in whatever
 *   wrote it rather than something a traveller can be asked to fix.
 */
fun String.toGeoPoint(): GeoPoint {
    val parts = split(',')
    require(parts.size >= 2) { "Expected a 'lat,lng' coordinate but got '$this'" }

    val lat = requireNotNull(parts[0].trim().toDoubleOrNull()) { "Latitude is not a number in '$this'" }
    val lng = requireNotNull(parts[1].trim().toDoubleOrNull()) { "Longitude is not a number in '$this'" }

    return GeoPoint(lat = lat, lng = lng)
}

/** Translates a navigator answer into the model the app draws from. */
fun RouteResponse.toDomainRoutes(): Routes = Routes(
    routes = routes.map { route -> Route(sections = route.sections.map { it.toDomain() }) },
)

private fun RouteSectionDto.toDomain(): RouteSection = RouteSection(
    summary = RouteSummary(
        durationSeconds = summary.durationSeconds.seconds,
        distanceMeters = summary.distanceMeters,
    ),
    actions = actions.map { action ->
        RouteAction(
            action = action.action,
            durationSeconds = action.durationSeconds.seconds,
            distanceMeters = (action.distanceMeters ?: 0) * Length.meters,
            instruction = action.instruction,
            offset = action.offset,
            direction = action.direction,
            severity = action.severity,
        )
    },
    departure = departure?.let { RouteDeparture(it.place.toLocation(), it.time?.toDateTimeComponents()) },
    arrival = arrival?.let { RouteDeparture(it.place.toLocation(), it.time?.toDateTimeComponents()) },
    transport = RouteTransport(mode = mode.name),
    // The encoding travels declared, and the app only knows how to draw one of them. Anything else
    // is dropped rather than handed to a decoder that would read it as nonsense.
    polyline = geometry?.takeIf { it.encoding == DRAWABLE_ENCODING }?.value,
    tollSystems = tollSystems.map { RouteTollSystem(id = it.id, name = it.name) },
    tolls = tolls.map { it.toDomain(tollSystems.map { system -> system.id }) },
)

private fun GeoPoint.toLocation(): RouteLocation = RouteLocation(lat = lat, lng = lng)

/**
 * A toll, in the shape the app's model still has.
 *
 * That model carries HERE's deprecated singular fields as non-null, so they are filled from the
 * first reference rather than left out. Nothing reads them today; they disappear the next time the
 * domain model is touched.
 */
private fun TollCostDto.toDomain(systemIds: List<String>): RouteTollCost = RouteTollCost(
    tollSystem = tollSystemRefs.firstOrNull()?.let { systemIds.getOrNull(it) }.orEmpty(),
    tollSystemRef = tollSystemRefs.firstOrNull() ?: 0,
    tollSystems = tollSystemRefs.ifEmpty { null },
    countryCode = countryCode,
)

/**
 * The app's model dates from when times arrived as strings, so it holds a [DateTimeComponents].
 *
 * Building one means formatting and parsing again, because that type has no public constructor: it
 * exists to be produced by a parser. The round trip is cheap and it is the only way to keep the
 * offset — which is the local one at the stop, and the only reason the field is not just an instant.
 */
private fun ZonedTime.toDateTimeComponents(): DateTimeComponents = DateTimeComponents.Formats.ISO_DATE_TIME_OFFSET
    .parse(instant.format(DateTimeComponents.Formats.ISO_DATE_TIME_OFFSET, UtcOffset(seconds = offsetSeconds)))

/** The only polyline encoding the map layer can draw today. */
private val DRAWABLE_ENCODING = com.takaotech.navigator.api.response.PolylineEncoding.HERE_FLEXIBLE
