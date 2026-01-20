package com.takaotech.navigation.routing.dto.request

import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.UtcOffset
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Instant

/**
 * Represents the departure time parameter for the HERE Routing API.
 *
 * The departure time can be specified as:
 * - A [LocalDateTime] (date-time without timezone offset, assumed to be local time at origin)
 * - A [LocalDateTime] with a [UtcOffset] (date-time with timezone offset)
 * - The special value "any" to indicate that time should not be taken into account during routing
 *
 * Format follows RFC 3339, section 5.6:
 * - `date-time`: e.g., `2019-06-24T01:23:45`
 * - `date-time` with offset: e.g., `2019-06-24T01:23:45+02:00`
 *
 * If neither `departureTime` nor `arrivalTime` are specified, current time at departure place will be used.
 *
 * **Note**: Only long-term traffic incidents will be used if `departureTime=any` and
 * `traffic[mode]=default` or no `traffic[mode]` are specified.
 */
sealed class DepartureTime {

    /**
     * Converts the departure time to the HERE API query string format.
     */
    abstract fun toQueryString(): String

    /**
     * Special value indicating that time should not be taken into account during routing.
     *
     * When using `any`:
     * - Only long-term traffic incidents will be considered
     * - Useful when the exact departure time is unknown or flexible
     */
    data object Any : DepartureTime() {
        override fun toQueryString(): String = "any"
    }

    /**
     * Departure time specified as a local date-time without timezone offset.
     *
     * The time is assumed to be local time at the origin location.
     * Format: `YYYY-MM-DDTHH:mm:ss` (e.g., `2019-06-24T01:23:45`)
     *
     * @property dateTime The local date and time of departure
     */
    data class Local(val dateTime: LocalDateTime) : DepartureTime() {
        override fun toQueryString(): String {
            // Format: 2019-06-24T01:23:45
            return dateTime.toString()
        }
    }

    /**
     * Departure time specified as a local date-time with a UTC offset.
     *
     * Format: `YYYY-MM-DDTHH:mm:ss±HH:mm` (e.g., `2019-06-24T01:23:45+02:00`)
     *
     * @property dateTime The local date and time of departure
     * @property offset The UTC offset for the time
     */
    data class WithOffset(
        val dateTime: LocalDateTime,
        val offset: UtcOffset
    ) : DepartureTime() {
        override fun toQueryString(): String {
            // Format with offset: 2019-06-24T01:23:45+02:00
            return "${dateTime}${formatOffset(offset)}"
        }

        private fun formatOffset(offset: UtcOffset): String {
            val totalSeconds = offset.totalSeconds
            if (totalSeconds == 0) return "Z"

            val sign = if (totalSeconds >= 0) "+" else "-"
            val absSeconds = kotlin.math.abs(totalSeconds)
            val hours = absSeconds / 3600
            val minutes = (absSeconds % 3600) / 60

            return "$sign${hours.toString().padStart(2, '0')}:${minutes.toString().padStart(2, '0')}"
        }
    }

    companion object {
        /**
         * Creates a DepartureTime with the special value "any".
         */
        fun any(): DepartureTime = Any

        /**
         * Creates a DepartureTime from a LocalDateTime (without timezone offset).
         *
         * @param dateTime The local date and time of departure
         */
        fun fromLocalDateTime(dateTime: LocalDateTime): DepartureTime =
            Local(dateTime)

        /**
         * Creates a DepartureTime from a LocalDateTime with a UTC offset.
         *
         * @param dateTime The local date and time of departure
         * @param offset The UTC offset
         */
        fun fromLocalDateTime(
            dateTime: LocalDateTime,
            offset: UtcOffset
        ): DepartureTime =
            WithOffset(dateTime, offset)

        /**
         * Creates a DepartureTime from an Instant with a timezone.
         *
         * The instant will be converted to local date-time in the given timezone.
         *
         * @param instant The instant of departure
         * @param timeZone The timezone
         */
        fun fromInstant(
            instant: Instant,
            timeZone: TimeZone
        ): DepartureTime {
            val localDateTime = instant.toLocalDateTime(timeZone)
            // For simplicity, we return without offset since the local time is already converted
            return Local(localDateTime)
        }

        /**
         * Creates a DepartureTime from an Instant using UTC timezone.
         *
         * @param instant The instant of departure
         */
        fun fromInstantUtc(instant: Instant): DepartureTime {
            val localDateTime = instant.toLocalDateTime(TimeZone.UTC)
            return WithOffset(localDateTime, UtcOffset.ZERO)
        }
    }
}
