@file:OptIn(ExperimentalTime::class)

package com.takaotech.ktravel.presentation.planning

import androidx.compose.ui.text.input.TextFieldValue
import com.takaotech.ktravel.domain.model.*
import kotlinx.collections.immutable.toPersistentList
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

object TravelPlanUiMapper {

    fun TravelPlan.toUiState(): PlanningUiState = PlanningUiState(
        planHeader = PlanHeader(
            name = TextFieldValue(name),
            mPeriod = PlanHeader.Period(
                start = Instant.fromEpochMilliseconds(periodStart),
                end = Instant.fromEpochMilliseconds(periodEnd)
            )
        ),
        days = days.map { it.toUiDay() }.toPersistentList(),
        places = places.map { it.toUiPlace() }.toPersistentList()
    )

    fun TravelDayDomain.toUiDay(): TravelDay = TravelDay(
        id = id,
        date = date,
        steps = steps.map { it.toUiStep() }.toPersistentList(),
        places = places.map { it.toUiPlace() }.toPersistentList()
    )

    fun PlaceDomain.toUiPlace(): Place = Place(
        id = id,
        name = name,
        lat = lat,
        lng = lng,
        schedule = schedule?.toUiSchedule()
    )

    private fun VisitScheduleDomain.toUiSchedule(): VisitSchedule = VisitSchedule(
        date = date,
        time = time
    )

    fun StepDomain.toUiStep(): TravelDay.Step = when (this) {
        is StepDomain.Place -> TravelDay.Step.Place(
            id = id,
            location = location,
            lat = lat,
            lng = lng
        )

        is StepDomain.Transport -> TravelDay.Step.Transport(
            id = id,
            type = type.toUiTransportType(),
            route = route
        )
    }

    fun TransportType.toUiTransportType(): TravelDay.Step.Transport.Type = when (this) {
        TransportType.TRAIN -> TravelDay.Step.Transport.Type.TRAIN
        TransportType.BUS -> TravelDay.Step.Transport.Type.BUS
        TransportType.CAR -> TravelDay.Step.Transport.Type.CAR
        TransportType.FLIGHT -> TravelDay.Step.Transport.Type.FLIGHT
    }

    fun TravelDay.Step.Transport.Type.toDomain(): TransportType = when (this) {
        TravelDay.Step.Transport.Type.TRAIN -> TransportType.TRAIN
        TravelDay.Step.Transport.Type.BUS -> TransportType.BUS
        TravelDay.Step.Transport.Type.CAR -> TransportType.CAR
        TravelDay.Step.Transport.Type.FLIGHT -> TransportType.FLIGHT
    }

    fun Place.toDomain(): PlaceDomain = PlaceDomain(
        id = id,
        name = name,
        lat = lat,
        lng = lng,
        schedule = schedule?.let {
            VisitScheduleDomain(date = it.date, time = it.time)
        }
    )

    fun TravelDay.Step.Transport.toDomainStep(): StepDomain.Transport = StepDomain.Transport(
        id = id,
        type = type.toDomain(),
        route = route
    )
}
