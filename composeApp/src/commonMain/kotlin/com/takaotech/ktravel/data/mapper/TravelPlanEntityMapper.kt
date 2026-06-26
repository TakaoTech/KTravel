package com.takaotech.ktravel.data.mapper

import com.takaotech.ktravel.data.entity.PlaceEntity
import com.takaotech.ktravel.data.entity.RouteActionEntity
import com.takaotech.ktravel.data.entity.RouteEntity
import com.takaotech.ktravel.data.entity.RouteSectionEntity
import com.takaotech.ktravel.data.entity.StepEntity
import com.takaotech.ktravel.data.entity.TravelDayEntity
import com.takaotech.ktravel.data.entity.TravelPlanEntity
import com.takaotech.ktravel.data.entity.VisitScheduleEntity
import com.takaotech.ktravel.domain.model.PlaceDomain
import com.takaotech.ktravel.domain.model.StepDomain
import com.takaotech.ktravel.domain.model.TransportType
import com.takaotech.ktravel.domain.model.TravelDayDomain
import com.takaotech.ktravel.domain.model.TravelPlanDomain
import com.takaotech.ktravel.domain.model.TravelPlanSummary
import com.takaotech.ktravel.domain.model.VisitScheduleDomain
import com.takaotech.ktravel.domain.routing.model.Route
import com.takaotech.ktravel.domain.routing.model.RouteAction
import com.takaotech.ktravel.domain.routing.model.RouteDeparture
import com.takaotech.ktravel.domain.routing.model.RouteLocation
import com.takaotech.ktravel.domain.routing.model.RouteSection
import com.takaotech.ktravel.domain.routing.model.RouteSummary
import com.takaotech.ktravel.domain.routing.model.RouteTransport
import io.nacular.measured.units.Length
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
        lng = lng
    )

    fun VisitScheduleDomain.toEntity(): VisitScheduleEntity = VisitScheduleEntity(
        dateEpochDays = date?.toEpochDays()?.toInt(),
        timeHour = time.hour,
        timeMinute = time.minute
    )

    fun StepDomain.toEntity(): StepEntity = when (this) {
        is StepDomain.Place -> StepEntity.Place(
            id = id,
            name = name,
            lat = lat,
            lng = lng,
            schedule = schedule?.toEntity()
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

    fun TravelPlanEntity.toDomain(): TravelPlanDomain = TravelPlanDomain(
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
        lng = lng
    )

    fun VisitScheduleEntity.toDomain(): VisitScheduleDomain = VisitScheduleDomain(
        date = dateEpochDays?.let { LocalDate.fromEpochDays(it) },
        time = LocalTime(timeHour, timeMinute)
    )

    fun StepEntity.toDomain(): StepDomain = when (this) {
        is StepEntity.Place -> StepDomain.Place(
            id = id,
            name = name,
            lat = lat,
            lng = lng,
            schedule = schedule?.toDomain()
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
