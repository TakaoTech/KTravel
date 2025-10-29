@file:OptIn(ExperimentalTime::class)

package com.takaotech.ktravel.presentation.planner

import androidx.compose.ui.text.input.TextFieldValue
import com.takaotech.ktravel.core.toLocalDate
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toPersistentList
import kotlinx.datetime.LocalDate
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.time.Instant
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

data class PlanningUiState(
    val planHeader: PlanHeader = PlanHeader(),
    val days: PersistentList<TravelDay> = persistentListOf()
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

    fun addTravelStep(day: LocalDate, name: String): PlanningUiState {
        val dayIndex = days.indexOfFirst { it.date == day }
        return days[dayIndex].let {
            it.copy(
                steps = it.steps.add(
                    TravelDay.Step(
                        location = name
                    )
                )
            )
        }.let {
            days.set(dayIndex, it)
        }.let {
            copy(
                days = it,
            )
        }
    }
}

data class PlanHeader(
    val name: TextFieldValue = TextFieldValue(""),
    private val mPeriod: Period = Period()
) {
    val period: Period = mPeriod

    data class Period(
        val start: Instant = Clock.System.now(),
        val end: Instant = start
    )
}

data class TravelDay(
    val date: LocalDate,
    val steps: PersistentList<Step> = persistentListOf()
) {
    data class Step @OptIn(ExperimentalUuidApi::class) constructor(
        val id: String = Uuid.random().toString(),
        val location: String
    )
}