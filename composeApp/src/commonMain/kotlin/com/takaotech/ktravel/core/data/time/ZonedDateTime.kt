package com.takaotech.ktravel.core.data.time

import kotlinx.datetime.TimeZone
import kotlin.time.Instant

/**
 * A moment together with the zone it is read in.
 *
 * The zone is the region (`Europe/Rome`) and not the offset in force at [instant]: it is what still
 * gives the right wall clock time after a daylight saving change, which an offset on its own cannot
 * do.
 *
 * @property instant The moment itself, the same everywhere on Earth.
 * @property timeZone The zone the moment is told in.
 */
data class ZonedDateTime(val instant: Instant, val timeZone: TimeZone)
