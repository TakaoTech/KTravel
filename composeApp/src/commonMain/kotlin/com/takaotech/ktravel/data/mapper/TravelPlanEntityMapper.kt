package com.takaotech.ktravel.data.mapper

import com.takaotech.ktravel.data.entity.AttachmentEntity
import com.takaotech.ktravel.data.entity.PlaceEntity
import com.takaotech.ktravel.data.entity.RouteActionEntity
import com.takaotech.ktravel.data.entity.RouteDepartureEntity
import com.takaotech.ktravel.data.entity.RouteLocationEntity
import com.takaotech.ktravel.data.entity.RouteSummaryEntity
import com.takaotech.ktravel.data.entity.RoutingRouteEntity
import com.takaotech.ktravel.data.entity.RoutingSectionEntity
import com.takaotech.ktravel.data.entity.StepEntity
import com.takaotech.ktravel.data.entity.TransitAgencyEntity
import com.takaotech.ktravel.data.entity.TransitJourneyEntity
import com.takaotech.ktravel.data.entity.TransitLineEntity
import com.takaotech.ktravel.data.entity.TransitStepEntity
import com.takaotech.ktravel.data.entity.TransitStopEntity
import com.takaotech.ktravel.data.entity.TransportAnswerEntity
import com.takaotech.ktravel.data.entity.TransportRequestEntity
import com.takaotech.ktravel.data.entity.TravelDayEntity
import com.takaotech.ktravel.data.entity.TravelPlanEntity
import com.takaotech.ktravel.data.entity.TravelSettingsEntity
import com.takaotech.ktravel.data.entity.VisitScheduleEntity
import com.takaotech.ktravel.domain.model.AttachmentDomain
import com.takaotech.ktravel.domain.model.PlaceDomain
import com.takaotech.ktravel.domain.model.StepDomain
import com.takaotech.ktravel.domain.model.TransportType
import com.takaotech.ktravel.domain.model.TravelDayDomain
import com.takaotech.ktravel.domain.model.TravelPlanDomain
import com.takaotech.ktravel.domain.model.TravelPlanSummary
import com.takaotech.ktravel.domain.model.TravelSettingsDomain
import com.takaotech.ktravel.domain.model.VisitScheduleDomain
import com.takaotech.ktravel.domain.navigator.NavigatorKind
import com.takaotech.ktravel.domain.routing.RouteFeature
import com.takaotech.ktravel.domain.routing.RouteSelection
import com.takaotech.ktravel.domain.routing.RoutingMode
import com.takaotech.ktravel.domain.routing.RoutingProfileId
import com.takaotech.ktravel.domain.routing.model.RouteAction
import com.takaotech.ktravel.domain.routing.model.RouteDeparture
import com.takaotech.ktravel.domain.routing.model.RouteLocation
import com.takaotech.ktravel.domain.routing.model.RouteSummary
import com.takaotech.ktravel.domain.routing.model.RoutingRoute
import com.takaotech.ktravel.domain.routing.model.RoutingSection
import com.takaotech.ktravel.domain.routing.model.TransitAgency
import com.takaotech.ktravel.domain.routing.model.TransitJourney
import com.takaotech.ktravel.domain.routing.model.TransitLine
import com.takaotech.ktravel.domain.routing.model.TransitStep
import com.takaotech.ktravel.domain.routing.model.TransitStop
import com.takaotech.ktravel.domain.routing.model.TransitTime
import com.takaotech.ktravel.domain.routing.model.TransportAnswer
import com.takaotech.ktravel.domain.routing.model.WheelchairAccess
import io.nacular.measured.units.Distance
import io.nacular.measured.units.Length
import io.nacular.measured.units.Measure
import io.nacular.measured.units.times
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.datetime.format
import kotlinx.datetime.format.DateTimeComponents
import kotlin.time.Duration.Companion.seconds

/**
 * Translation between the plan as it is stored and the plan the rest of the application works
 * with, in both directions.
 *
 * The two directions are deliberately not symmetrical. Writing is exact: the domain value has
 * already been validated, so enums go out as their `name`, measured values as plain numbers in a
 * fixed unit, and times as ISO 8601 text. Reading is lenient: a document may come from an older
 * build, from a newer one, or from a hand-edited archive, so a name or a time this build cannot
 * make sense of degrades to a default instead of taking the whole plan down with it. Every such
 * decision is recorded on the function that makes it.
 *
 * The conversions are extensions on the entity and domain types, so a call site reads as
 * `plan.toEntity(id)`; callers import the members they need or open the object with `with`.
 */
internal object TravelPlanEntityMapper {

    // ── Domain → Entity ───────────────────────────────────────────────────────

    /**
     * The plan as it is stored, as the document [id].
     *
     * The identifier comes from the caller and takes precedence over the one the plan carries, so
     * the result always describes the document being written.
     */
    fun TravelPlanDomain.toEntity(id: String): TravelPlanEntity = TravelPlanEntity(
        id = id,
        name = name,
        periodStart = periodStart,
        periodEnd = periodEnd,
        days = days.map { it.toEntity() },
        places = places.map { it.toEntity() },
        settings = settings.toEntity(),
    )

    /** The stored form of the plan's preferences, the navigator preference written as its name. */
    private fun TravelSettingsDomain.toEntity(): TravelSettingsEntity = TravelSettingsEntity(
        hereApiKey = hereApiKey,
        navigatorPreference = navigatorPreference.name,
        navigatorRemoteBaseUrl = navigatorRemoteBaseUrl,
    )

    /** The stored form of a day: its itinerary and the places still waiting to enter it. */
    private fun TravelDayDomain.toEntity(): TravelDayEntity = TravelDayEntity(
        id = id,
        date = date,
        steps = steps.map { it.toEntity() },
        places = places.map { it.toEntity() },
    )

    /** The stored form of a backlog place, note and attachments included. */
    private fun PlaceDomain.toEntity(): PlaceEntity = PlaceEntity(
        id = id,
        name = name,
        lat = lat,
        lng = lng,
        note = note,
        attachments = attachments.map { it.toEntity() },
    )

    /**
     * The stored form of a visit time: the date as epoch days, each time split in hour and minute.
     *
     * Reading them back is [localTimeOrNull], which needs both halves of a time to rebuild it.
     */
    private fun VisitScheduleDomain.toEntity(): VisitScheduleEntity = VisitScheduleEntity(
        dateEpochDays = date?.toEpochDays()?.toInt(),
        startTimeHour = startTime?.hour,
        startTimeMinute = startTime?.minute,
        endTimeHour = endTime?.hour,
        endTimeMinute = endTime?.minute,
    )

    /** The stored form of an attachment: its path under the attachments root and its metadata. */
    private fun AttachmentDomain.toEntity(): AttachmentEntity = AttachmentEntity(
        id = id,
        relativePath = relativePath,
        originalName = originalName,
        mimeType = mimeType,
        sizeBytes = sizeBytes,
    )

    /**
     * The stored form of an itinerary step, place or transport.
     *
     * A transport keeps the answer in the shape it was answered in and the request that produced
     * it, so a saved leg can be shown again without being recomputed.
     */
    fun StepDomain.toEntity(): StepEntity = when (this) {
        is StepDomain.Place -> StepEntity.Place(
            id = id,
            name = name,
            lat = lat,
            lng = lng,
            schedule = schedule?.toEntity(),
            note = note,
            attachments = attachments.map { it.toEntity() },
        )

        is StepDomain.Transport -> StepEntity.Transport(
            id = id,
            transportType = type.name,
            answer = answer.toEntity(),
            request = request?.toEntity(),
            note = note,
            attachments = attachments.map { it.toEntity() },
            calculatedAt = calculatedAt,
        )
    }

    /** The stored form of a route request, road options and service options kept apart. */
    private fun RouteSelection.toEntity(): TransportRequestEntity = when (this) {
        is RouteSelection.Routing -> TransportRequestEntity.Routing(
            provider = profileId.provider,
            profile = profileId.profile,
            alternatives = alternatives,
            mode = mode.id,
            avoid = avoid.map { it.name },
            shortestDistance = shortestDistance,
        )

        is RouteSelection.Transit -> TransportRequestEntity.Transit(
            provider = profileId.provider,
            profile = profileId.profile,
            alternatives = alternatives,
            modeFilter = modeFilter.map { it.id },
            maxChanges = maxChanges,
            pedestrianSpeedMetersPerSecond = pedestrianSpeedMetersPerSecond,
            pedestrianMaxDistanceMeters = pedestrianMaxDistanceMeters,
        )
    }

    /** The stored form of a calculated answer: a road route, or a journey on scheduled services. */
    private fun TransportAnswer.toEntity(): TransportAnswerEntity = when (this) {
        is TransportAnswer.Routing -> TransportAnswerEntity.Routing(route.toEntity())
        is TransportAnswer.Transit -> TransportAnswerEntity.Transit(journey.toEntity())
    }

    /** The stored form of a road route: its summary and its sections. */
    private fun RoutingRoute.toEntity(): RoutingRouteEntity = RoutingRouteEntity(
        summary = summary.toEntity(),
        sections = sections.map { it.toEntity() },
    )

    /** The stored form of a section of a road route, geometry and turn-by-turn actions included. */
    private fun RoutingSection.toEntity(): RoutingSectionEntity = RoutingSectionEntity(
        summary = summary.toEntity(),
        mode = mode,
        actions = actions.map { it.toEntity() },
        departure = departure?.toEntity(),
        arrival = arrival?.toEntity(),
        polyline = polyline,
    )

    /** The stored form of a journey on scheduled services: its summary and its steps. */
    private fun TransitJourney.toEntity(): TransitJourneyEntity = TransitJourneyEntity(
        summary = summary.toEntity(),
        steps = steps.map { it.toEntity() },
    )

    /** The stored form of a journey step, on foot or aboard a service. Times go out as ISO 8601. */
    private fun TransitStep.toEntity(): TransitStepEntity = when (this) {
        is TransitStep.Walk -> TransitStepEntity.Walk(
            summary = summary.toEntity(),
            polyline = polyline,
            departureTime = departure?.formatIso(),
            arrivalTime = arrival?.formatIso(),
            from = from?.toEntity(),
            to = to?.toEntity(),
        )

        is TransitStep.Ride -> TransitStepEntity.Ride(
            summary = summary.toEntity(),
            line = line.toEntity(),
            polyline = polyline,
            agency = agency?.toEntity(),
            boarding = boarding?.toEntity(),
            alighting = alighting?.toEntity(),
            intermediateStops = intermediateStops.map { it.toEntity() },
        )
    }

    /** The stored form of a service line, the wheelchair access written as its name. */
    private fun TransitLine.toEntity(): TransitLineEntity = TransitLineEntity(
        mode = mode,
        name = name,
        shortName = shortName,
        longName = longName,
        category = category,
        headsign = headsign,
        color = color,
        textColor = textColor,
        url = url,
        wheelchairAccessible = wheelchairAccessible.name,
    )

    /** The stored form of the operator running a line. */
    private fun TransitAgency.toEntity(): TransitAgencyEntity = TransitAgencyEntity(
        name = name,
        id = id,
        website = website,
    )

    /** The stored form of a stop call: its location, its times and the dwell reduced to seconds. */
    private fun TransitStop.toEntity(): TransitStopEntity = TransitStopEntity(
        location = location.toEntity(),
        name = name,
        arrivalTime = arrival?.formatIso(),
        departureTime = departure?.formatIso(),
        dwellSeconds = dwell?.inWholeSeconds,
        offset = offset,
        url = url,
        wheelchairAccessible = wheelchairAccessible.name,
    )

    /** The stored form of a summary: seconds and meters, so storage depends on no unit type. */
    private fun RouteSummary.toEntity(): RouteSummaryEntity = RouteSummaryEntity(
        durationSeconds = durationSeconds.inWholeSeconds,
        distanceMeters = distance `in` Distance.meters,
    )

    /** The stored form of a coordinate. */
    private fun RouteLocation.toEntity(): RouteLocationEntity = RouteLocationEntity(lat = lat, lng = lng)

    /** The stored form of an end of a section: where it is and, when it is known, when. */
    private fun RouteDeparture.toEntity(): RouteDepartureEntity = RouteDepartureEntity(
        location = location.toEntity(),
        time = time?.formatIso(),
    )

    /** The stored form of a manoeuvre, its duration in seconds and its distance in meters. */
    private fun RouteAction.toEntity(): RouteActionEntity = RouteActionEntity(
        action = action,
        durationSeconds = durationSeconds.inWholeSeconds,
        distanceMeters = distanceMeters `in` Distance.meters,
        instruction = instruction,
        offset = offset,
        direction = direction,
        severity = severity,
    )

    // ── Entity → Domain ───────────────────────────────────────────────────────

    /**
     * The plan reduced to what a list of plans shows: its identity, its name and its dates.
     *
     * It reads the stored document directly, so listing plans never pays for decoding their days.
     */
    fun TravelPlanEntity.toSummary(): TravelPlanSummary = TravelPlanSummary(
        id = id,
        name = name,
        periodStart = periodStart,
        periodEnd = periodEnd,
    )

    /** The stored plan back as the domain tree the application edits. */
    fun TravelPlanEntity.toDomain(): TravelPlanDomain = TravelPlanDomain(
        id = id,
        name = name,
        periodStart = periodStart,
        periodEnd = periodEnd,
        days = days.map { it.toDomain() },
        places = places.map { it.toDomain() },
        settings = settings.toDomain(),
    )

    /**
     * The stored preferences back in domain form.
     *
     * The navigator preference is read leniently: a plan stored before the preference existed
     * carries an empty string, and one stored by a newer build may carry a name this build does not
     * know. Both mean the embedded server, which is the choice that always works.
     */
    private fun TravelSettingsEntity.toDomain(): TravelSettingsDomain = TravelSettingsDomain(
        hereApiKey = hereApiKey,
        navigatorPreference = NavigatorKind.ofOrEmbedded(navigatorPreference),
        navigatorRemoteBaseUrl = navigatorRemoteBaseUrl,
    )

    /** A stored day back in domain form. */
    private fun TravelDayEntity.toDomain(): TravelDayDomain = TravelDayDomain(
        id = id,
        date = date,
        steps = steps.map { it.toDomain() },
        places = places.map { it.toDomain() },
    )

    /** A stored backlog place back in domain form. */
    private fun PlaceEntity.toDomain(): PlaceDomain = PlaceDomain(
        id = id,
        name = name,
        lat = lat,
        lng = lng,
        note = note,
        attachments = attachments.map { it.toDomain() },
    )

    /** A stored visit time back in domain form; a half-written time reads as no time at all. */
    private fun VisitScheduleEntity.toDomain(): VisitScheduleDomain = VisitScheduleDomain(
        date = dateEpochDays?.let { LocalDate.fromEpochDays(it) },
        startTime = localTimeOrNull(startTimeHour, startTimeMinute),
        endTime = localTimeOrNull(endTimeHour, endTimeMinute),
    )

    /** The time these two halves make, or null when either of them is missing. */
    private fun localTimeOrNull(hour: Int?, minute: Int?): LocalTime? =
        if (hour != null && minute != null) LocalTime(hour, minute) else null

    /** A stored attachment back in domain form. */
    fun AttachmentEntity.toDomain(): AttachmentDomain = AttachmentDomain(
        id = id,
        relativePath = relativePath,
        originalName = originalName,
        mimeType = mimeType,
        sizeBytes = sizeBytes,
    )

    /**
     * A stored step back in domain form.
     *
     * The transport type is read strictly, unlike the options around it: it names what the step is,
     * so an unknown name is a document that cannot be shown rather than one missing an option.
     */
    fun StepEntity.toDomain(): StepDomain = when (this) {
        is StepEntity.Place -> StepDomain.Place(
            id = id,
            name = name,
            lat = lat,
            lng = lng,
            schedule = schedule?.toDomain(),
            note = note,
            attachments = attachments.map { it.toDomain() },
        )

        is StepEntity.Transport -> StepDomain.Transport(
            id = id,
            type = TransportType.valueOf(transportType),
            answer = answer.toDomain(),
            request = request?.toDomain(),
            note = note,
            attachments = attachments.map { it.toDomain() },
            calculatedAt = calculatedAt,
        )
    }

    /** A stored route request back in domain form, avoid options this build cannot read dropped. */
    fun TransportRequestEntity.toDomain(): RouteSelection = when (this) {
        is TransportRequestEntity.Routing -> RouteSelection.Routing(
            profileId = RoutingProfileId(provider = provider, profile = profile),
            mode = RoutingMode(mode),
            alternatives = alternatives,
            avoid = avoid.mapNotNull { it.toRouteFeatureOrNull() }.toSet(),
            shortestDistance = shortestDistance,
        )

        is TransportRequestEntity.Transit -> RouteSelection.Transit(
            profileId = RoutingProfileId(provider = provider, profile = profile),
            modeFilter = modeFilter.map { RoutingMode(it) }.toSet(),
            alternatives = alternatives,
            maxChanges = maxChanges,
            pedestrianSpeedMetersPerSecond = pedestrianSpeedMetersPerSecond,
            pedestrianMaxDistanceMeters = pedestrianMaxDistanceMeters,
        )
    }

    /**
     * The feature this name stands for, or null when this build has never heard of it.
     *
     * Lenient rather than `valueOf`, because a document written by a newer build may name a feature
     * that does not exist here yet, and one unknown option is not a reason to fail the whole plan:
     * dropping it leaves a request that still computes, only without that option.
     */
    private fun String.toRouteFeatureOrNull(): RouteFeature? = RouteFeature.entries.firstOrNull { it.name == this }

    /** A stored answer back in domain form. */
    private fun TransportAnswerEntity.toDomain(): TransportAnswer = when (this) {
        is TransportAnswerEntity.Routing -> TransportAnswer.Routing(route.toDomain())
        is TransportAnswerEntity.Transit -> TransportAnswer.Transit(journey.toDomain())
    }

    /** A stored road route back in domain form. */
    private fun RoutingRouteEntity.toDomain(): RoutingRoute = RoutingRoute(
        summary = summary.toDomain(),
        sections = sections.map { it.toDomain() },
    )

    /** A stored section back in domain form. */
    private fun RoutingSectionEntity.toDomain(): RoutingSection = RoutingSection(
        summary = summary.toDomain(),
        mode = mode,
        actions = actions.map { it.toDomain() },
        departure = departure?.toDomain(),
        arrival = arrival?.toDomain(),
        polyline = polyline,
    )

    /** A stored journey back in domain form. */
    private fun TransitJourneyEntity.toDomain(): TransitJourney = TransitJourney(
        summary = summary.toDomain(),
        steps = steps.map { it.toDomain() },
    )

    /** A stored journey step back in domain form; a time it cannot read leaves the step none. */
    private fun TransitStepEntity.toDomain(): TransitStep = when (this) {
        is TransitStepEntity.Walk -> TransitStep.Walk(
            summary = summary.toDomain(),
            polyline = polyline,
            departure = departureTime?.toTransitTimeOrNull(),
            arrival = arrivalTime?.toTransitTimeOrNull(),
            from = from?.toDomain(),
            to = to?.toDomain(),
        )

        is TransitStepEntity.Ride -> TransitStep.Ride(
            summary = summary.toDomain(),
            line = line.toDomain(),
            polyline = polyline,
            agency = agency?.toDomain(),
            boarding = boarding?.toDomain(),
            alighting = alighting?.toDomain(),
            intermediateStops = intermediateStops.map { it.toDomain() },
        )
    }

    /** A stored line back in domain form. */
    private fun TransitLineEntity.toDomain(): TransitLine = TransitLine(
        mode = mode,
        name = name,
        shortName = shortName,
        longName = longName,
        category = category,
        headsign = headsign,
        color = color,
        textColor = textColor,
        url = url,
        wheelchairAccessible = wheelchairAccessible.toWheelchairAccess(),
    )

    /** A stored operator back in domain form. */
    private fun TransitAgencyEntity.toDomain(): TransitAgency = TransitAgency(name = name, id = id, website = website)

    /** A stored stop call back in domain form, the dwell read back as a duration. */
    private fun TransitStopEntity.toDomain(): TransitStop = TransitStop(
        location = location.toDomain(),
        name = name,
        arrival = arrivalTime?.toTransitTimeOrNull(),
        departure = departureTime?.toTransitTimeOrNull(),
        dwell = dwellSeconds?.seconds,
        offset = offset,
        url = url,
        wheelchairAccessible = wheelchairAccessible.toWheelchairAccess(),
    )

    /** A stored summary back in domain form, the distance measured in meters again. */
    private fun RouteSummaryEntity.toDomain(): RouteSummary = RouteSummary(
        durationSeconds = durationSeconds.seconds,
        distance = Measure(distanceMeters, Length.meters),
    )

    /** A stored coordinate back in domain form. */
    private fun RouteLocationEntity.toDomain(): RouteLocation = RouteLocation(lat = lat, lng = lng)

    /** A stored end of a section back in domain form. */
    private fun RouteDepartureEntity.toDomain(): RouteDeparture = RouteDeparture(
        location = location.toDomain(),
        time = time?.parseIsoOrNull(),
    )

    /** A stored manoeuvre back in domain form. */
    private fun RouteActionEntity.toDomain(): RouteAction = RouteAction(
        action = action,
        durationSeconds = durationSeconds.seconds,
        distanceMeters = distanceMeters * Length.meters,
        instruction = instruction,
        offset = offset,
        direction = direction,
        severity = severity,
    )

    /**
     * How well a saved vehicle or stop serves a traveller in a wheelchair.
     *
     * Unknown wording — an older document, or a newer build's vocabulary — reads as
     * [WheelchairAccess.UNKNOWN] and never as `NO`: "not asked" and "not possible" are not the same
     * thing to say to someone planning a trip.
     */
    private fun String?.toWheelchairAccess(): WheelchairAccess =
        WheelchairAccess.entries.firstOrNull { it.name == this } ?: WheelchairAccess.UNKNOWN

    /** A saved timetable moment, keeping the offset in force at the stop. */
    private fun String.toTransitTimeOrNull(): TransitTime? = parseIsoOrNull()?.let { components ->
        runCatching { TransitTime(components.toInstantUsingOffset(), components.toUtcOffset()) }.getOrNull()
    }

    /** The stored form of a section time: ISO 8601 keeping the offset in force where it happens. */
    private fun DateTimeComponents.formatIso(): String = DateTimeComponents.Formats.ISO_DATE_TIME_OFFSET.format(this)

    /**
     * The stored form of a timetable moment.
     *
     * The offset travels with it, and is the one at the stop: it is what turns the instant back into
     * the time written on the departure board rather than the time where the device happens to be.
     */
    private fun TransitTime.formatIso(): String =
        instant.format(DateTimeComponents.Formats.ISO_DATE_TIME_OFFSET, offset)

    /**
     * A stored time back into components, or null when it cannot be read.
     *
     * A document written by hand or by an older build is not worth failing the whole plan over: a
     * leg without a time still draws, one that throws takes the trip with it.
     */
    private fun String.parseIsoOrNull(): DateTimeComponents? =
        runCatching { DateTimeComponents.Formats.ISO_DATE_TIME_OFFSET.parse(this) }.getOrNull()
}
