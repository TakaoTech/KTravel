@file:OptIn(ExperimentalTime::class)

package com.takaotech.ktravel.presentation.planning

import androidx.compose.ui.text.input.TextFieldValue
import com.takaotech.ktravel.domain.model.PlaceDomain
import com.takaotech.ktravel.domain.model.StepDomain
import com.takaotech.ktravel.domain.model.TravelDayDomain
import com.takaotech.ktravel.domain.model.TravelPlanDomain
import com.takaotech.ktravel.domain.model.VisitScheduleDomain
import kotlinx.collections.immutable.toPersistentList
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlin.time.ExperimentalTime

object TravelPlanUiMapper {

    fun TravelPlanDomain.toUiState(): PlanningUiState = PlanningUiState(
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

    fun TravelDayDomain.toUiDay(): TravelDayUi = TravelDayUi(
        id = id,
        date = date,
        steps = steps.map { it.toUiStep() }.toPersistentList(),
        places = places.map { it.toUiPlace() }.toPersistentList()
    )

    fun PlaceDomain.toUiPlace(): PlaceUi = PlaceUi(
        id = id,
        name = name,
        lat = lat,
        lng = lng
    )

    private fun VisitScheduleDomain.toUiSchedule(): VisitScheduleUi = VisitScheduleUi(
        date = date,
        time = time
    )

    fun StepDomain.toUiStep(): StepUi = when (this) {
        is StepDomain.Place -> StepUi.Place(
            id = id,
            name = name,
            lat = lat,
            lng = lng,
            schedule = schedule?.toUiSchedule()
        )

        is StepDomain.Transport -> StepUi.Transport(
            id = id,
            type = type,
            route = route
        )
    }


    fun PlaceUi.toDomain(): PlaceDomain = PlaceDomain(
        id = id,
        name = name,
        lat = lat,
        lng = lng
    )

    fun StepUi.Transport.toDomainStep(): StepDomain.Transport = StepDomain.Transport(
        id = id,
        type = type,
        route = route
    )

    fun StepDomain.Place.toUiStepPlace(): StepUi.Place = StepUi.Place(
        id = id,
        name = name,
        lat = lat,
        lng = lng,
        schedule = schedule?.toUiSchedule()
    )

    fun uiFieldsToDomain(name: String, lat: Double, lng: Double): PlaceDomain = PlaceDomain(
        name = name,
        lat = lat,
        lng = lng
    )
}
