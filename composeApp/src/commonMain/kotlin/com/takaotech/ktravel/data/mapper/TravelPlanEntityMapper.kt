package com.takaotech.ktravel.data.mapper

import com.takaotech.ktravel.data.entity.AttachmentEntity
import com.takaotech.ktravel.data.entity.PlaceEntity
import com.takaotech.ktravel.data.entity.RouteActionEntity
import com.takaotech.ktravel.data.entity.RouteDepartureEntity
import com.takaotech.ktravel.data.entity.RouteEntity
import com.takaotech.ktravel.data.entity.RouteLocationEntity
import com.takaotech.ktravel.data.entity.RouteSectionEntity
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
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

object TravelPlanEntityMapper {

    // ── Domain → Entity ───────────────────────────────────────────────────────

    fun TravelPlanDomain.toEntity(id: String): TravelPlanEntity = TravelPlanEntity(
        id = id,
        name = name,
        periodStart = periodStart,
        periodEnd = periodEnd,
        days = days.map { it.toEntity() },
        places = places.map { it.toEntity() },
        settings = settings.toEntity(),
    )

    fun TravelSettingsDomain.toEntity(): TravelSettingsEntity = TravelSettingsEntity(
        hereApiKey = hereApiKey,
        navigatorPreference = navigatorPreference.name,
        navigatorRemoteBaseUrl = navigatorRemoteBaseUrl,
    )

    fun TravelDayDomain.toEntity(): TravelDayEntity = TravelDayEntity(
        id = id,
        date = date,
        steps = steps.map { it.toEntity() },
        places = places.map { it.toEntity() },
    )

    fun PlaceDomain.toEntity(): PlaceEntity = PlaceEntity(
        id = id,
        name = name,
        lat = lat,
        lng = lng,
    )

    fun VisitScheduleDomain.toEntity(): VisitScheduleEntity = VisitScheduleEntity(
        dateEpochDays = date?.toEpochDays()?.toInt(),
        startTimeHour = startTime?.hour,
        startTimeMinute = startTime?.minute,
        endTimeHour = endTime?.hour,
        endTimeMinute = endTime?.minute,
    )

    fun AttachmentDomain.toEntity(): AttachmentEntity = AttachmentEntity(
        id = id,
        relativePath = relativePath,
        originalName = originalName,
        mimeType = mimeType,
        sizeBytes = sizeBytes,
    )

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

    fun RouteSelection.toEntity(): TransportRequestEntity = when (this) {
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

    fun TransportAnswer.toEntity(): TransportAnswerEntity = when (this) {
        is TransportAnswer.Routing -> TransportAnswerEntity.Routing(route.toEntity())
        is TransportAnswer.Transit -> TransportAnswerEntity.Transit(journey.toEntity())
    }

    fun RoutingRoute.toEntity(): RoutingRouteEntity = RoutingRouteEntity(
        summary = summary.toEntity(),
        sections = sections.map { it.toEntity() },
    )

    fun RoutingSection.toEntity(): RoutingSectionEntity = RoutingSectionEntity(
        summary = summary.toEntity(),
        mode = mode,
        actions = actions.map { it.toEntity() },
        departure = departure?.toEntity(),
        arrival = arrival?.toEntity(),
        polyline = polyline,
    )

    fun TransitJourney.toEntity(): TransitJourneyEntity = TransitJourneyEntity(
        summary = summary.toEntity(),
        steps = steps.map { it.toEntity() },
    )

    fun TransitStep.toEntity(): TransitStepEntity = when (this) {
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

    fun TransitLine.toEntity(): TransitLineEntity = TransitLineEntity(
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

    fun TransitAgency.toEntity(): TransitAgencyEntity = TransitAgencyEntity(
        name = name,
        id = id,
        website = website,
    )

    fun TransitStop.toEntity(): TransitStopEntity = TransitStopEntity(
        location = location.toEntity(),
        name = name,
        arrivalTime = arrival?.formatIso(),
        departureTime = departure?.formatIso(),
        dwellSeconds = dwell?.inWholeSeconds,
        offset = offset,
        url = url,
        wheelchairAccessible = wheelchairAccessible.name,
    )

    fun RouteSummary.toEntity(): RouteSummaryEntity = RouteSummaryEntity(
        durationSeconds = durationSeconds.inWholeSeconds,
        distanceMeters = distance `in` Distance.meters,
    )

    fun RouteLocation.toEntity(): RouteLocationEntity = RouteLocationEntity(lat = lat, lng = lng)

    fun RouteDeparture.toEntity(): RouteDepartureEntity = RouteDepartureEntity(
        location = location.toEntity(),
        time = time?.formatIso(),
    )

    fun RouteAction.toEntity(): RouteActionEntity = RouteActionEntity(
        action = action,
        durationSeconds = durationSeconds.inWholeSeconds,
        distanceMeters = distanceMeters.amount,
        instruction = instruction,
        offset = offset,
        direction = direction,
        severity = severity,
    )

    // ── Entity → Domain ───────────────────────────────────────────────────────

    fun TravelPlanEntity.toSummary(): TravelPlanSummary = TravelPlanSummary(
        id = id,
        name = name,
        periodStart = periodStart,
        periodEnd = periodEnd,
    )

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
     * Converts the domain on the receiver [TravelSettingsEntity].
     * [navigatorPreference] Lenient by design: a plan stored before the preference existed carries an empty string,
     * and one stored by a newer build may carry a name this build does not know. Both mean the
     * embedded server, which is the choice that always works.
     * @return the travel settings domain
     */
    fun TravelSettingsEntity.toDomain(): TravelSettingsDomain = TravelSettingsDomain(
        hereApiKey = hereApiKey,
        navigatorPreference = NavigatorKind.ofOrEmbedded(navigatorPreference),
        navigatorRemoteBaseUrl = navigatorRemoteBaseUrl,
    )

    fun TravelDayEntity.toDomain(): TravelDayDomain = TravelDayDomain(
        id = id,
        date = date,
        steps = steps.map { it.toDomain() },
        places = places.map { it.toDomain() },
    )

    fun PlaceEntity.toDomain(): PlaceDomain = PlaceDomain(
        id = id,
        name = name,
        lat = lat,
        lng = lng,
    )

    fun VisitScheduleEntity.toDomain(): VisitScheduleDomain = VisitScheduleDomain(
        date = dateEpochDays?.let { LocalDate.fromEpochDays(it) },
        startTime = localTimeOrNull(startTimeHour, startTimeMinute),
        endTime = localTimeOrNull(endTimeHour, endTimeMinute),
    )

    private fun localTimeOrNull(hour: Int?, minute: Int?): LocalTime? =
        if (hour != null && minute != null) LocalTime(hour, minute) else null

    fun AttachmentEntity.toDomain(): AttachmentDomain = AttachmentDomain(
        id = id,
        relativePath = relativePath,
        originalName = originalName,
        mimeType = mimeType,
        sizeBytes = sizeBytes,
    )

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
            answer = savedAnswer(),
            request = request?.toDomain(),
            note = note,
            attachments = attachments.map { it.toDomain() },
            calculatedAt = calculatedAt,
        )
    }

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

/**
     * The answer a saved transport carries, whichever build wrote it.
     *
     * `answer` when the document has one; otherwise the flat `route` of the earlier builds, read as
     * best it can be. A step that has neither — which no real document is — comes back as an empty
     * road route rather than failing the whole plan.
     */
    private fun StepEntity.Transport.savedAnswer(): TransportAnswer =
        answer?.toDomain() ?: route?.toLegacyAnswer() ?: TransportAnswer.Routing(
            RoutingRoute(summary = RouteSummary(Duration.ZERO, 0.0 * Length.meters), sections = emptyList()),
        )

    fun TransportAnswerEntity.toDomain(): TransportAnswer = when (this) {
        is TransportAnswerEntity.Routing -> TransportAnswer.Routing(route.toDomain())
        is TransportAnswerEntity.Transit -> TransportAnswer.Transit(journey.toDomain())
    }

    fun RoutingRouteEntity.toDomain(): RoutingRoute = RoutingRoute(
        summary = summary.toDomain(),
        sections = sections.map { it.toDomain() },
    )

    fun RoutingSectionEntity.toDomain(): RoutingSection = RoutingSection(
        summary = summary.toDomain(),
        mode = mode,
        actions = actions.map { it.toDomain() },
        departure = departure?.toDomain(),
        arrival = arrival?.toDomain(),
        polyline = polyline,
    )

    fun TransitJourneyEntity.toDomain(): TransitJourney = TransitJourney(
        summary = summary.toDomain(),
        steps = steps.map { it.toDomain() },
    )

    fun TransitStepEntity.toDomain(): TransitStep = when (this) {
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

    fun TransitLineEntity.toDomain(): TransitLine = TransitLine(
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

    fun TransitAgencyEntity.toDomain(): TransitAgency = TransitAgency(name = name, id = id, website = website)

    fun TransitStopEntity.toDomain(): TransitStop = TransitStop(
        location = location.toDomain(),
        name = name,
        arrival = arrivalTime?.toTransitTimeOrNull(),
        departure = departureTime?.toTransitTimeOrNull(),
        dwell = dwellSeconds?.seconds,
        offset = offset,
        url = url,
        wheelchairAccessible = wheelchairAccessible.toWheelchairAccess(),
    )

    fun RouteSummaryEntity.toDomain(): RouteSummary = RouteSummary(
        durationSeconds = durationSeconds.seconds,
        distance = Measure(distanceMeters, Length.meters),
    )

    fun RouteLocationEntity.toDomain(): RouteLocation = RouteLocation(lat = lat, lng = lng)

    fun RouteDepartureEntity.toDomain(): RouteDeparture = RouteDeparture(
        location = location.toDomain(),
        time = time?.parseIsoOrNull(),
    )

    fun RouteActionEntity.toDomain(): RouteAction = RouteAction(
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
     * Unknown wording — an older document, or a newer build's vocabulary — reads as [
     * WheelchairAccess.UNKNOWN] and never as `NO`: "not asked" and "not possible" are not the same
     * thing to say to someone planning a trip.
     */
    private fun String?.toWheelchairAccess(): WheelchairAccess =
        WheelchairAccess.entries.firstOrNull { it.name == this } ?: WheelchairAccess.UNKNOWN

    /** A saved timetable moment, keeping the offset in force at the stop. */
    private fun String.toTransitTimeOrNull(): TransitTime? = parseIsoOrNull()?.let { components ->
        runCatching { TransitTime(components.toInstantUsingOffset(), components.toUtcOffset()) }.getOrNull()
    }

    /**
     * The flat route of the earlier builds, read as the closest thing it can be.
     *
     * Sections carrying manoeuvres are a road route. Anything else was a journey, and comes back as
     * the little that shape kept of one: modes, times and geometry, no lines and no stop names. They
     * were never written, so there is nothing to recover — recomputing the leg is what fills them in.
     */
    private fun RouteEntity.toLegacyAnswer(): TransportAnswer = if (sections.any { it.actions.isNotEmpty() }) {
        TransportAnswer.Routing(
            RoutingRoute(
                summary = sections.legacySummary(),
                sections = sections.map { section ->
                    RoutingSection(
                        summary = section.legacySummary(),
                        mode = section.transportMode.orEmpty(),
                        actions = section.actions.map { it.toDomain() },
                        departure = section.legacyDeparture(),
                        arrival = section.legacyArrival(),
                        polyline = section.polyline,
                    )
                },
            ),
        )
    } else {
        TransportAnswer.Transit(
            TransitJourney(
                summary = sections.legacySummary(),
                steps = sections.map { section ->
                    val mode = section.transportMode
                    if (mode == null || mode.equals(LEGACY_PEDESTRIAN_MODE, ignoreCase = true)) {
                        TransitStep.Walk(
                            summary = section.legacySummary(),
                            polyline = section.polyline,
                            departure = section.departureTime?.toTransitTimeOrNull(),
                            arrival = section.arrivalTime?.toTransitTimeOrNull(),
                            from = section.legacyDeparture()?.location,
                            to = section.legacyArrival()?.location,
                        )
                    } else {
                        TransitStep.Ride(
                            summary = section.legacySummary(),
                            line = TransitLine(mode = mode),
                            polyline = section.polyline,
                            boarding = section.legacyDeparture()?.let {
                                TransitStop(
                                    location = it.location,
                                    departure = section.departureTime?.toTransitTimeOrNull(),
                                )
                            },
                            alighting = section.legacyArrival()?.let {
                                TransitStop(
                                    location = it.location,
                                    arrival = section.arrivalTime?.toTransitTimeOrNull(),
                                )
                            },
                        )
                    }
                },
            ),
        )
    }

    private fun RouteSectionEntity.legacySummary(): RouteSummary = RouteSummary(
        durationSeconds = durationSeconds.seconds,
        distance = Measure(distanceMeters, Length.meters),
    )

    private fun List<RouteSectionEntity>.legacySummary(): RouteSummary = RouteSummary(
        durationSeconds = sumOf { it.durationSeconds }.seconds,
        distance = Measure(sumOf { it.distanceMeters }, Length.meters),
    )

    private fun RouteSectionEntity.legacyDeparture(): RouteDeparture? =
        if (departureLat != null && departureLng != null) {
            RouteDeparture(RouteLocation(departureLat, departureLng), departureTime?.parseIsoOrNull())
        } else {
            null
        }

    private fun RouteSectionEntity.legacyArrival(): RouteDeparture? = if (arrivalLat != null && arrivalLng != null) {
        RouteDeparture(RouteLocation(arrivalLat, arrivalLng), arrivalTime?.parseIsoOrNull())
    } else {
        null
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

/** How a step on foot was filed by the builds that flattened every answer into one shape. */
private const val LEGACY_PEDESTRIAN_MODE = "PEDESTRIAN"
