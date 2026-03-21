package com.takaotech.ktravel.core.data.time

import kotlinx.datetime.TimeZone
import kotlin.time.Instant

data class ZonedDateTime(
    val instant: Instant,
    val timeZone: TimeZone
)