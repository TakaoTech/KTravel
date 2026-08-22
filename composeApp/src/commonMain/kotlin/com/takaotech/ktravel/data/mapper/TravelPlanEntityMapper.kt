package com.takaotech.ktravel.data.mapper

import com.takaotech.ktravel.data.entity.AttachmentEntity
import com.takaotech.ktravel.data.entity.PlaceEntity
import com.takaotech.ktravel.data.entity.RouteActionEntity
import com.takaotech.ktravel.data.entity.RouteEntity
import com.takaotech.ktravel.data.entity.RouteSectionEntity
import com.takaotech.ktravel.data.entity.StepEntity
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
import com.takaotech.ktravel.domain.routing.model.Route
import com.takaotech.ktravel.domain.routing.model.RouteAction
import com.takaotech.ktravel.domain.routing.model.RouteDeparture
import com.takaotech.ktravel.domain.routing.model.RouteLocation
import com.takaotech.ktravel.domain.routing.model.RouteSection
import com.takaotech.ktravel.domain.routing.model.RouteSummary
import com.takaotech.ktravel.domain.routing.model.RouteTransport
import io.nacular.measured.units.Distance
import io.nacular.measured.units.Length
import io.nacular.measured.units.Measure
import io.nacular.measured.units.times
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
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
            route = route.toEntity(),
            request = request?.toEntity(),
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

    fun Route.toEntity(): RouteEntity = RouteEntity(
        sections = sections.map { it.toEntity() },
    )

    fun RouteSection.toEntity(): RouteSectionEntity = RouteSectionEntity(
        durationSeconds = summary.durationSeconds.inWholeSeconds,
        distanceMeters = summary.distance `in` Distance.meters,
        polyline = polyline,
        transportMode = transport?.mode,
        departureLat = departure?.location?.lat,
        departureLng = departure?.location?.lng,
        arrivalLat = arrival?.location?.lat,
        arrivalLng = arrival?.location?.lng,
        actions = actions.map { it.toEntity() },
    )

    fun RouteAction.toEntity(): RouteActionEntity = RouteActionEntity(
        action = action,
        durationSeconds = durationSeconds.inWholeSeconds,
        distanceMeters = distanceMeters.amount,
        instruction = instruction,
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
            route = route.toDomain(),
            request = request?.toDomain(),
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

    fun RouteEntity.toDomain(): Route = Route(
        sections = sections.map { it.toDomain() },
    )

    fun RouteSectionEntity.toDomain(): RouteSection = RouteSection(
        summary = RouteSummary(
            durationSeconds = durationSeconds.seconds,
            distance = Measure(distanceMeters, Length.meters),
        ),
        polyline = polyline,
        transport = transportMode?.let { RouteTransport(mode = it) },
        departure = if (departureLat != null && departureLng != null) {
            RouteDeparture(location = RouteLocation(lat = departureLat, lng = departureLng))
        } else {
            null
        },
        arrival = if (arrivalLat != null && arrivalLng != null) {
            RouteDeparture(location = RouteLocation(lat = arrivalLat, lng = arrivalLng))
        } else {
            null
        },
        actions = actions.map { it.toDomain() },
    )

    fun RouteActionEntity.toDomain(): RouteAction = RouteAction(
        action = action,
        durationSeconds = durationSeconds.seconds,
        distanceMeters = distanceMeters * Length.meters,
        instruction = instruction,
        direction = direction,
        severity = severity,
    )
}
