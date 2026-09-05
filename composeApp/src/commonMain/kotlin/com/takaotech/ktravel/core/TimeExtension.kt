package com.takaotech.ktravel.core

import androidx.compose.runtime.Composable
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Duration
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

/**
 * The day this moment falls on in [timeZone].
 *
 * The default is UTC, so the same moment yields the same day wherever the device is; pass the zone
 * the trip is read in when the answer has to follow the traveller.
 */
@OptIn(ExperimentalTime::class)
fun Instant.toLocalDate(timeZone: TimeZone = TimeZone.UTC): LocalDate = toLocalDateTime(timeZone)
    .date

/** The day this epoch milliseconds value falls on in [timeZone], UTC by default. */
@OptIn(ExperimentalTime::class)
fun Long.toLocalDate(timeZone: TimeZone = TimeZone.UTC): LocalDate = Instant.fromEpochMilliseconds(this)
    .toLocalDate(timeZone)

@Composable
fun Duration.formatForRead() {
}
