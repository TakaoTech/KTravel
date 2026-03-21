@file:OptIn(ExperimentalTime::class)

package com.takaotech.ktravel.presentation.planning

import androidx.compose.ui.text.input.TextFieldValue
import com.takaotech.ktravel.domain.model.*
import kotlinx.collections.immutable.toPersistentList
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlin.time.ExperimentalTime

object TravelPlanUiMapper {

    fun TravelPlan.toUiState(): PlanningUiState = PlanningUiState(
        planHeader = PlanHeader(
            name = TextFieldValue(name),
            mPeriod = PlanHeader.Period(
                start = periodStart.atStartOfDayIn(TimeZone.UTC).toEpochMilliseconds(),
                end = periodEnd.atStartOfDayIn(TimeZone.UTC).toEpochMilliseconds(),
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
            type = type,
            route = route
        )
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
        type = type,
        route = route
    )

    fun StepDomain.Place.toUiStepPlace(): TravelDay.Step.Place = TravelDay.Step.Place(
        id = id,
        location = location,
        lat = lat,
        lng = lng
    )

    fun uiFieldsToDomain(name: String, lat: Double, lng: Double): PlaceDomain = PlaceDomain(
        name = name,
        lat = lat,
        lng = lng
    )
}
