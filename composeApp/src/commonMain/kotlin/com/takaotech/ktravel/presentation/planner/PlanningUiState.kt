@file:OptIn(ExperimentalTime::class)

package com.takaotech.ktravel.presentation.planner

import androidx.compose.ui.text.input.TextFieldValue
import com.takaotech.ktravel.core.toLocalDate
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.datetime.LocalDate
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

data class PlanningUiState(
    val planHeader: PlanHeader = PlanHeader(),
    val days: ImmutableList<TravelDay> = persistentListOf()
) {
    fun setPeriod(
        start: Instant,
        end: Instant
    ): PlanningUiState {
        //TODO Check if start and end is same

        return (start.toLocalDate()..end.toLocalDate()).map {
            TravelDay(date = it)
        }.let {
            copy(
                planHeader = planHeader.copy(
                    mPeriod = PlanHeader.Period(
                        start = start,
                        end = end,
                    )
                ),
                days = it.toImmutableList()
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
    val date: LocalDate
)