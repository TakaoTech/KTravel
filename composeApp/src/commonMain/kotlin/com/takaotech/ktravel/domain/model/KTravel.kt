package com.takaotech.ktravel.domain.model

import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime

data class KTravel(
    val name: String,
    val period: Period? = null,
    val days: List<TStep> = listOf(),
    val todos: List<TPlace> = listOf(),
) {
    //TODO Field for JSON Versioning
//    val version: String

    data class Period(
        val startDate: LocalDate?,
        val endDate: LocalDate?,
    )
}

sealed class TStep {
    data class TDay(
        val date: LocalDate,
        val tNav: List<TNav> = listOf(),
    ) : TStep()

    data class TLink(val transport: Transport) : TStep(), Transport by transport
}