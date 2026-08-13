package com.takaotech.navigator.api.common

import kotlinx.serialization.Serializable
import kotlin.time.Instant

/**
 * A point in time together with the offset that was in force where it happens.
 *
 * Both halves are needed and neither is redundant. The [instant] is what durations and comparisons
 * are computed on; the [offsetSeconds] is what turns it back into the wall clock a traveller reads
 * on the departure board, which is the local time at that stop and not the one on the device.
 *
 * @property instant The absolute point in time.
 * @property offsetSeconds Offset from UTC at the place the time refers to, in seconds.
 */
@Serializable
data class ZonedTime(val instant: Instant, val offsetSeconds: Int)
