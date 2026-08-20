package com.takaotech.ktravel.data.routing

import com.takaotech.ktravel.domain.routing.RouteFeature
import com.takaotech.ktravel.domain.routing.RouteSelection
import com.takaotech.ktravel.domain.routing.RoutingMode
import com.takaotech.ktravel.domain.routing.RoutingOptionsSpec
import com.takaotech.ktravel.domain.routing.RoutingProfileId
import com.takaotech.ktravel.domain.routing.RoutingProfileInfo
import com.takaotech.ktravel.domain.routing.model.RouteAction
import com.takaotech.ktravel.domain.routing.model.RouteDeparture
import com.takaotech.ktravel.domain.routing.model.RouteLocation
import com.takaotech.ktravel.domain.routing.model.RouteSummary
import com.takaotech.ktravel.domain.routing.model.RouteTollCost
import com.takaotech.ktravel.domain.routing.model.RouteTollSystem
import com.takaotech.ktravel.domain.routing.model.RoutingRoute
import com.takaotech.ktravel.domain.routing.model.RoutingRoutes
import com.takaotech.ktravel.domain.routing.model.RoutingSection
import com.takaotech.ktravel.domain.routing.model.TransitAgency
import com.takaotech.ktravel.domain.routing.model.TransitJourney
import com.takaotech.ktravel.domain.routing.model.TransitJourneys
import com.takaotech.ktravel.domain.routing.model.TransitLine
import com.takaotech.ktravel.domain.routing.model.TransitStep
import com.takaotech.ktravel.domain.routing.model.TransitStop
import com.takaotech.ktravel.domain.routing.model.TransitTime
import com.takaotech.ktravel.domain.routing.model.WheelchairAccess
import com.takaotech.navigator.api.catalog.NavigatorProfile
import com.takaotech.navigator.api.catalog.ProviderProfileDescriptor
import com.takaotech.navigator.api.common.GeoPoint
import com.takaotech.navigator.api.common.RouteTime
import com.takaotech.navigator.api.common.TransitMode
import com.takaotech.navigator.api.common.ZonedTime
import com.takaotech.navigator.api.here.HereAvoidFeature
import com.takaotech.navigator.api.here.HereAvoidOptions
import com.takaotech.navigator.api.here.HereReturnAttribute
import com.takaotech.navigator.api.here.HereRoutingMode
import com.takaotech.navigator.api.here.HereRoutingRequest
import com.takaotech.navigator.api.here.HereTransitModeFilter
import com.takaotech.navigator.api.here.HereTransitRouteRequest
import com.takaotech.navigator.api.here.HereTransportMode
import com.takaotech.navigator.api.response.PolylineEncoding
import com.takaotech.navigator.api.response.RouteActionDto
import com.takaotech.navigator.api.response.RouteGeometry
import com.takaotech.navigator.api.response.RouteSummaryDto
import com.takaotech.navigator.api.response.RoutingRouteResponse
import com.takaotech.navigator.api.response.RoutingSectionDto
import com.takaotech.navigator.api.response.TollCostDto
import com.takaotech.navigator.api.response.TransitJourneyResponse
import com.takaotech.navigator.api.response.TransitJourneyStep
import com.takaotech.navigator.api.response.TransitLineDto
import com.takaotech.navigator.api.response.TransitStopDto
import io.nacular.measured.units.Length
import io.nacular.measured.units.Measure
import io.nacular.measured.units.times
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.UtcOffset
import kotlinx.datetime.format
import kotlinx.datetime.format.DateTimeComponents
import kotlinx.datetime.toInstant
import kotlin.time.Duration.Companion.seconds
import com.takaotech.navigator.api.response.WheelchairAccess as WheelchairAccessDto

// Between the app's own routing model and the navigator contract.
//
// It is the only place in the app that knows the contract exists. Everything above it works in
// domain types and cannot tell which engine, or which machine, produced the answer.

/**
 * What the app knows about a profile, out of what the contract declares about it.
 *
 * The modes are read from the profile's own declaration and are never pooled with another's: the
 * vocabularies of the two HERE APIs collide by name and not by meaning, so a mode only ever exists
 * attached to the profile it came from.
 */
fun NavigatorProfile.toProfileInfo(): RoutingProfileInfo = when (this) {
    is NavigatorProfile.HereRouting -> RoutingProfileInfo(
        id = descriptor.toProfileId(),
        displayName = descriptor.displayName,
        // Upstream Here Routing requires exactly one vehicle, so the selector is a choice.
        options = RoutingOptionsSpec.RoutingSingleMode(
            modes = modes.map { RoutingMode(it.name) },
            maxAlternatives = descriptor.maxAlternatives,
            modesSupportingShortest = modesSupportingShortest.map { RoutingMode(it.name) }.toSet(),
            supportsTolls = descriptor.supportsTolls,
        ),
        requiresApiKey = descriptor.requiresApiKey,
    )

    is NavigatorProfile.HereTransit -> RoutingProfileInfo(
        id = descriptor.toProfileId(),
        displayName = descriptor.displayName,
        // Upstream takes a set that restricts the answer, so the selector is a filter. Nothing
        // selected means no restriction, which is not the same as nothing allowed.
        options = RoutingOptionsSpec.TransitFilter(
            modes = modeFilter.map { RoutingMode(it.name) },
            maxAlternatives = descriptor.maxAlternatives,
        ),
        requiresApiKey = descriptor.requiresApiKey,
    )
}

fun ProviderProfileDescriptor.toProfileId(): RoutingProfileId =
    RoutingProfileId(provider = provider.value, profile = profile.value)

/**
 * Builds the road request.
 *
 * Tolls are asked for only where they can be charged. A walk and a bicycle ride pay none, so
 * computing them would be a question with no answer — one the navigator refuses and the provider
 * would bill for anyway.
 */
fun RouteSelection.Routing.toRoutingRequest(origin: GeoPoint, destination: GeoPoint): HereRoutingRequest =
    HereRoutingRequest(
        origin = origin,
        destination = destination,
        routingMode = if (shortestDistance) HereRoutingMode.SHORT else HereRoutingMode.FAST,
        alternatives = alternatives,
        time = departureRouteTime(),
        avoid = avoid
            .takeIf { it.isNotEmpty() }
            ?.let { features -> HereAvoidOptions(features = features.map { it.toHereAvoidFeature() }) },
        returnAttributes = if (mode.toHereTransportMode().hasTolls) {
            HereReturnAttribute.NAVIGATION_WITH_TOLLS
        } else {
            HereReturnAttribute.NAVIGATION
        },
    )

/**
 * Builds the journey request.
 *
 * An empty filter is sent as no filter at all rather than as an empty include list: upstream reads
 * an empty inclusion as "nothing is acceptable", which would answer every journey with no route.
 */
fun RouteSelection.Transit.toTransitRouteRequest(origin: GeoPoint, destination: GeoPoint): HereTransitRouteRequest =
    HereTransitRouteRequest(
        origin = origin,
        destination = destination,
        alternatives = alternatives,
        time = departureRouteTime(),
        modes = modeFilter
            .takeIf { it.isNotEmpty() }
            ?.let { HereTransitModeFilter(include = it.map { mode -> mode.toTransitMode() }) },
        changes = maxChanges,
        pedestrianSpeedMetersPerSecond = pedestrianSpeedMetersPerSecond,
        pedestrianMaxDistanceMeters = pedestrianMaxDistanceMeters,
    )

/**
 * Translates a feature by name, which holds because the app's enum is a subset of the contract's.
 *
 * A name that does not resolve is a feature added on one side and not the other, and failing here is
 * better than dropping it and answering with a route through the thing the traveller excluded.
 */
private fun RouteFeature.toHereAvoidFeature(): HereAvoidFeature =
    requireNotNull(HereAvoidFeature.entries.firstOrNull { it.name == name }) {
        "'$name' is not a feature the navigator contract can be asked to avoid"
    }

/**
 * The vehicle, in the vocabulary of HERE's road API.
 *
 * Translated back into that vocabulary by name, which is safe precisely because it was produced from
 * it: a mode reaching here that HERE's road API does not have is a mode that came from somewhere it
 * should never have crossed to, and failing loudly is the point.
 *
 * Visible to the service beside this file because the mode is not part of the body: it is the path
 * the request goes to, and that path is built where the call is made.
 */
internal fun RoutingMode.toHereTransportMode(): HereTransportMode =
    requireNotNull(HereTransportMode.entries.firstOrNull { it.name == id }) {
        "'$id' is not a HERE road mode; it belongs to another profile's vocabulary"
    }

private fun RoutingMode.toTransitMode(): TransitMode =
    requireNotNull(TransitMode.entries.firstOrNull { it.name == id }) {
        "'$id' is not a HERE transit mode; it belongs to another profile's vocabulary"
    }

/**
 * When the traveller wants to leave.
 *
 * A date and a time separately, either of which can be unset, so only both together mean anything.
 * The instant is resolved in the device's own timezone, which is the one the user was looking at
 * when they picked it.
 */
private fun RouteSelection.departureRouteTime(): RouteTime {
    val date = departureDate
    val time = departureTime

    return if (date == null || time == null) {
        RouteTime.Now
    } else {
        RouteTime.DepartAt(LocalDateTime(date, time).toInstant(TimeZone.currentSystemDefault()))
    }
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

/** Translates a road answer into the model the app draws from. */
fun RoutingRouteResponse.toDomain(): RoutingRoutes = RoutingRoutes(
    routes = routes.map { route ->
        RoutingRoute(
            summary = route.summary.toDomain(),
            sections = route.sections.map { it.toDomain() },
        )
    },
)

/**
 * Translates a journey answer into the model the app draws from.
 *
 * The `when` is exhaustive on purpose and there is no `else`: a kind of step added to the contract
 * has to be given a domain shape here before anything compiles, which is the whole reason the two
 * answers stopped being one type.
 */
fun TransitJourneyResponse.toDomain(): TransitJourneys = TransitJourneys(
    journeys = journeys.map { journey ->
        TransitJourney(
            summary = journey.summary.toDomain(),
            steps = journey.steps.map { step ->
                when (step) {
                    is TransitJourneyStep.Walk -> TransitStep.Walk(
                        summary = step.summary.toDomain(),
                        polyline = step.geometry.drawableOrNull(),
                        departure = step.departure?.time?.toTransitTime(),
                        arrival = step.arrival?.time?.toTransitTime(),
                        from = step.departure?.place?.toLocation(),
                        to = step.arrival?.place?.toLocation(),
                    )

                    is TransitJourneyStep.Ride -> TransitStep.Ride(
                        summary = step.summary.toDomain(),
                        line = step.line.toDomain(),
                        polyline = step.geometry.drawableOrNull(),
                        agency = step.agency?.let { TransitAgency(it.name, it.id, it.website) },
                        boarding = step.boarding?.toDomain(),
                        alighting = step.alighting?.toDomain(),
                        intermediateStops = step.intermediateStops.map { it.toDomain() },
                    )
                }
            },
        )
    },
)

private fun RouteSummaryDto.toDomain(): RouteSummary = RouteSummary(
    durationSeconds = durationSeconds.seconds,
    distance = Measure(distanceMeters.toDouble(), Length.meters),
)

private fun RoutingSectionDto.toDomain(): RoutingSection = RoutingSection(
    summary = summary.toDomain(),
    mode = mode.name,
    actions = actions.map { it.toDomain() },
    departure = departure?.let { RouteDeparture(it.place.toLocation(), it.time?.toDateTimeComponents()) },
    arrival = arrival?.let { RouteDeparture(it.place.toLocation(), it.time?.toDateTimeComponents()) },
    polyline = geometry.drawableOrNull(),
    tollSystems = tollSystems.map { RouteTollSystem(id = it.id, name = it.name) },
    tolls = tolls.map { it.toDomain(tollSystems.map { system -> system.id }) },
)

private fun RouteActionDto.toDomain(): RouteAction = RouteAction(
    action = action,
    durationSeconds = durationSeconds.seconds,
    distanceMeters = (distanceMeters ?: 0) * Length.meters,
    instruction = instruction,
    offset = offset,
    direction = direction,
    severity = severity,
)

private fun TransitLineDto.toDomain(): TransitLine = TransitLine(
    mode = mode.name,
    name = name,
    shortName = shortName,
    longName = longName,
    category = category,
    headsign = headsign,
    color = color,
    textColor = textColor,
    url = url,
    wheelchairAccessible = wheelchairAccessible.toDomain(),
)

private fun TransitStopDto.toDomain(): TransitStop = TransitStop(
    location = place.toLocation(),
    name = name,
    arrival = arrival?.toTransitTime(),
    departure = departure?.toTransitTime(),
    dwell = dwellSeconds?.seconds,
    offset = offset,
    url = url,
    wheelchairAccessible = wheelchairAccessible.toDomain(),
)

/**
 * Accessibility, translated by name because the app's enum is the contract's.
 *
 * Anything that does not resolve is unknown rather than a refusal, for the same reason the navigator
 * applies to a vocabulary it has not seen: it must never tell a traveller a station is unusable on
 * the strength of a word this build does not recognise.
 */
private fun WheelchairAccessDto.toDomain(): WheelchairAccess =
    WheelchairAccess.entries.firstOrNull { it.name == name } ?: WheelchairAccess.UNKNOWN

private fun GeoPoint.toLocation(): RouteLocation = RouteLocation(lat = lat, lng = lng)

/**
 * The geometry, when it is one the map layer can draw.
 *
 * The encoding travels declared, and the app only knows how to read one of them. Anything else is
 * dropped rather than handed to a decoder that would read it as nonsense.
 */
// FIXME: This is Wrong, this assert only HERE polyline is supported (currently is correct)
private fun RouteGeometry?.drawableOrNull(): String? = this?.takeIf { it.encoding == DRAWABLE_ENCODING }?.value

/**
 * A toll, in the shape the stored model still has.
 *
 * That model carries HERE's deprecated singular fields as non-null, so they are filled from the
 * first reference rather than left out. Nothing reads them today; they disappear the next time the
 * stored model is touched.
 */
private fun TollCostDto.toDomain(systemIds: List<String>): RouteTollCost = RouteTollCost(
    tollSystem = tollSystemRefs.firstOrNull()?.let { systemIds.getOrNull(it) }.orEmpty(),
    tollSystemRef = tollSystemRefs.firstOrNull() ?: 0,
    tollSystems = tollSystemRefs.ifEmpty { null },
    countryCode = countryCode,
)

/**
 * The stored model dates from when times arrived as strings, so it holds a [DateTimeComponents].
 *
 * Building one means formatting and parsing again, because that type has no public constructor: it
 * exists to be produced by a parser. The round trip is cheap and it is the only way to keep the
 * offset — which is the local one at the stop, and the only reason the field is not just an instant.
 */
private fun ZonedTime.toDateTimeComponents(): DateTimeComponents = DateTimeComponents.Formats.ISO_DATE_TIME_OFFSET
    .parse(instant.format(DateTimeComponents.Formats.ISO_DATE_TIME_OFFSET, UtcOffset(seconds = offsetSeconds)))

/** A moment on a timetable, which the journey model keeps as an instant it can subtract. */
private fun ZonedTime.toTransitTime(): TransitTime =
    TransitTime(instant = instant, offset = UtcOffset(seconds = offsetSeconds))

/** The only polyline encoding the map layer can draw today. */
private val DRAWABLE_ENCODING = PolylineEncoding.HERE_FLEXIBLE
