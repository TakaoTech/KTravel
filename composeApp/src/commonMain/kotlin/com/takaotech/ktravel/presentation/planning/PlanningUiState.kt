@file:OptIn(ExperimentalTime::class, ExperimentalUuidApi::class)

package com.takaotech.ktravel.presentation.planning

import androidx.compose.runtime.Stable
import androidx.compose.ui.text.input.TextFieldValue
import com.takaotech.ktravel.core.toLocalDate
import com.takaotech.ktravel.domain.routing.model.Route
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toPersistentList
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.time.Instant
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@Stable
data class PlanningUiState(
    val planHeader: PlanHeader = PlanHeader(),
    val days: PersistentList<TravelDay> = persistentListOf(),
    val places: PersistentList<Place> = persistentListOf()
) {
    fun setPeriod(
        start: Instant,
        end: Instant
    ): PlanningUiState {
        return (start.toLocalDate()..end.toLocalDate()).map { newDate ->
            days.firstOrNull { it.date == newDate } ?: TravelDay(date = newDate)
        }.let {
            copy(
                planHeader = planHeader.copy(
                    mPeriod = PlanHeader.Period(
                        start = start,
                        end = end,
                    )
                ),
                days = it.toPersistentList()
            )
        }
    }
}

@Stable
data class PlanHeader(
    val name: TextFieldValue = TextFieldValue(""),
    private val mPeriod: Period = Period()
) {
    val period: Period = mPeriod

    @Stable
    data class Period(
        val start: Instant = Clock.System.now(),
        val end: Instant = start
    )
}

@Stable
data class TravelDay(
    val id: String = Uuid.random().toString(),
    val date: LocalDate,
    val steps: PersistentList<Step> = persistentListOf(),
    val places: PersistentList<Place> = persistentListOf()
) {
    companion object {
        val EMPTY = TravelDay(
            id = "",
            date = LocalDate.fromEpochDays(0)
        )
    }

    sealed class Step(open val id: String = Uuid.random().toString()) {
        @Stable
        data class Place(
            override val id: String = Uuid.random().toString(),
            val location: String,
            val lat: Double,
            val lng: Double
        ) : Step(id)

        @Stable
        data class Transport(
            override val id: String = Uuid.random().toString(),
            val type: Type,
            val route: Route
        ) : Step(id) {
            enum class Type { TRAIN, BUS, CAR, FLIGHT }
        }
    }
}

@Stable
data class VisitSchedule(
    val date: LocalDate? = null,
    val time: LocalTime
)

@Stable
data class Place(
    val id: String = Uuid.random().toString(),
    val name: String,
    val lat: Double,
    val lng: Double,
    val schedule: VisitSchedule? = null
)