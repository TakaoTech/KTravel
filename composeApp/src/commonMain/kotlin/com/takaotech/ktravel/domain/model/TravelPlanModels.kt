@file:OptIn(ExperimentalUuidApi::class)

package com.takaotech.ktravel.domain.model

import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

private fun newId(): String = Uuid.random().toString()

data class TravelPlanSummary(
    val id: String,
    val name: String,
    val periodStart: LocalDate,
    val periodEnd: LocalDate
)

data class TravelPlan(
    val id: String = newId(),
    val name: String = "",
    val periodStart: LocalDate = LocalDate.fromEpochDays(0),
    val periodEnd: LocalDate = LocalDate.fromEpochDays(0),
    val days: List<TravelDayDomain> = emptyList(),
    val places: List<PlaceDomain> = emptyList()
)

data class TravelDayDomain(
    val id: String = newId(),
    val date: LocalDate,
    val steps: List<StepDomain> = emptyList(),
    val places: List<PlaceDomain> = emptyList()
) {
    companion object {
        val EMPTY = TravelDayDomain(
            id = "",
            date = LocalDate.fromEpochDays(0)
        )
    }
}

data class PlaceDomain(
    val id: String = newId(),
    val name: String,
    val lat: Double,
    val lng: Double,
    val schedule: VisitScheduleDomain? = null
)

data class VisitScheduleDomain(
    val date: LocalDate? = null,
    val time: LocalTime
)

sealed class StepDomain(open val id: String = newId()) {
    data class Place(
        override val id: String = newId(),
        val location: String,
        val lat: Double,
        val lng: Double
    ) : StepDomain(id)

    data class Transport(
        override val id: String = newId(),
        val type: TransportType,
        val route: com.takaotech.ktravel.domain.routing.model.Route
    ) : StepDomain(id)
}

enum class TransportType { TRAIN, BUS, CAR, FLIGHT }
