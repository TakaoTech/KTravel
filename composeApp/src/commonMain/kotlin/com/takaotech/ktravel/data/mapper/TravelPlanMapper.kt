package com.takaotech.ktravel.data.mapper

import com.takaotech.ktravel.data.entity.*
import com.takaotech.ktravel.domain.model.*
import com.takaotech.ktravel.domain.routing.model.*
import io.nacular.measured.units.Length
import io.nacular.measured.units.times
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlin.time.Duration.Companion.seconds

object TravelPlanMapper {

    // ── Domain → Entity ───────────────────────────────────────────────────────

    fun TravelPlan.toEntity(id: String): TravelPlanEntity = TravelPlanEntity(
        id = id,
        name = name,
        periodStart = periodStart,
        periodEnd = periodEnd,
        days = days.map { it.toEntity() },
        places = places.map { it.toEntity() },
    )

    fun TravelDayDomain.toEntity(): TravelDayEntity = TravelDayEntity(
        id = id,
        date = date,
        steps = steps.map { it.toEntity() },
        places = places.map { it.toEntity() }
    )

    fun PlaceDomain.toEntity(): PlaceEntity = PlaceEntity(
        id = id,
        name = name,
        lat = lat,
        lng = lng,
        schedule = schedule?.toEntity()
    )

    fun VisitScheduleDomain.toEntity(): VisitScheduleEntity = VisitScheduleEntity(
        dateEpochDays = date?.toEpochDays()?.toInt(),
        timeHour = time.hour,
        timeMinute = time.minute
    )

    fun StepDomain.toEntity(): StepEntity = when (this) {
        is StepDomain.Place -> StepEntity.Place(
            id = id,
            location = location,
            lat = lat,
            lng = lng
        )

        is StepDomain.Transport -> StepEntity.Transport(
            id = id,
            transportType = type.name,
            route = route.toEntity()
        )
    }

    fun Route.toEntity(): RouteEntity = RouteEntity(
        sections = sections.map { it.toEntity() }
    )

    fun RouteSection.toEntity(): RouteSectionEntity = RouteSectionEntity(
        durationSeconds = summary.durationSeconds.inWholeSeconds,
        distanceMeters = summary.distanceMeters.toDouble(),
        polyline = polyline,
        transportMode = transport?.mode,
        departureLat = departure?.location?.lat,
        departureLng = departure?.location?.lng,
        arrivalLat = arrival?.location?.lat,
        arrivalLng = arrival?.location?.lng,
        actions = actions.map { it.toEntity() }
    )

    fun RouteAction.toEntity(): RouteActionEntity = RouteActionEntity(
        action = action,
        durationSeconds = durationSeconds.inWholeSeconds,
        distanceMeters = distanceMeters.amount,
        instruction = instruction,
        direction = direction,
        severity = severity
    )

    // ── Entity → Domain ───────────────────────────────────────────────────────

    fun TravelPlanEntity.toSummary(): TravelPlanSummary = TravelPlanSummary(
        id = id,
        name = name,
        periodStart = periodStart,
        periodEnd = periodEnd
    )

    fun TravelPlanEntity.toDomain(): TravelPlan = TravelPlan(
        id = id,
        name = name,
        periodStart = periodStart,
        periodEnd = periodEnd,
        days = days.map { it.toDomain() },
        places = places.map { it.toDomain() }
    )

    fun TravelDayEntity.toDomain(): TravelDayDomain = TravelDayDomain(
        id = id,
        date = date,
        steps = steps.map { it.toDomain() },
        places = places.map { it.toDomain() }
    )

    fun PlaceEntity.toDomain(): PlaceDomain = PlaceDomain(
        id = id,
        name = name,
        lat = lat,
        lng = lng,
        schedule = schedule?.toDomain()
    )

    fun VisitScheduleEntity.toDomain(): VisitScheduleDomain = VisitScheduleDomain(
        date = dateEpochDays?.let { LocalDate.fromEpochDays(it) },
        time = LocalTime(timeHour, timeMinute)
    )

    fun StepEntity.toDomain(): StepDomain = when (this) {
        is StepEntity.Place -> StepDomain.Place(
            id = id,
            location = location,
            lat = lat,
            lng = lng
        )

        is StepEntity.Transport -> StepDomain.Transport(
            id = id,
            type = TransportType.valueOf(transportType),
            route = route.toDomain()
        )
    }

    fun RouteEntity.toDomain(): Route = Route(
        sections = sections.map { it.toDomain() }
    )

    fun RouteSectionEntity.toDomain(): RouteSection = RouteSection(
        summary = RouteSummary(
            durationSeconds = durationSeconds.seconds,
            distanceMeters = distanceMeters.toInt()
        ),
        polyline = polyline,
        transport = transportMode?.let { RouteTransport(mode = it) },
        departure = if (departureLat != null && departureLng != null) {
            RouteDeparture(location = RouteLocation(lat = departureLat, lng = departureLng))
        } else null,
        arrival = if (arrivalLat != null && arrivalLng != null) {
            RouteDeparture(location = RouteLocation(lat = arrivalLat, lng = arrivalLng))
        } else null,
        actions = actions.map { it.toDomain() }
    )

    fun RouteActionEntity.toDomain(): RouteAction = RouteAction(
        action = action,
        durationSeconds = durationSeconds.seconds,
        distanceMeters = distanceMeters * Length.meters,
        instruction = instruction,
        direction = direction,
        severity = severity
    )
}
