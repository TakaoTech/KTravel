package com.takaotech.ktravel.core

import androidx.compose.runtime.Composable
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Duration
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@OptIn(ExperimentalTime::class)
fun Instant.toLocalDate(timeZone: TimeZone = TimeZone.UTC): LocalDate = toLocalDateTime(timeZone)
    .date

@OptIn(ExperimentalTime::class)
fun Long.toLocalDate(timeZone: TimeZone = TimeZone.UTC): LocalDate = Instant.fromEpochMilliseconds(this)
    .toLocalDate(timeZone)

@Composable
fun Duration.formatForRead() {
}
