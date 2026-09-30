package com.takaotech.gunzou.api.search.place

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline

/**
 * A recurring span of time a place is open: it opens at [opensAt] on each of [days], and stays open
 * for [durationMinutes].
 *
 * Structured rather than text on purpose, so a client can tell whether a place is open at a given
 * time and format the hours in its own language. The times are local to the place, in
 * [PlaceDetails.timeZone].
 *
 * A period may cross midnight: a bar open from `18:00` for 480 minutes closes at `02:00` of the
 * following day. A day may have more than one period, such as a lunch and a dinner service.
 *
 * @property days The days it opens on, never empty.
 * @property opensAt Local opening time, as `HH:mm` on a 24 hour clock, such as `08:30`.
 * @property durationMinutes How long it stays open, in minutes, always positive.
 */
@Serializable
data class OpeningPeriod(
    @SerialName("days") val days: List<Weekday>,
    @SerialName("opensAt") val opensAt: String,
    @SerialName("durationMinutes") val durationMinutes: Int,
)

/**
 * A day of the week.
 *
 * A string rather than an enum for the same forward compatibility reason as every other vocabulary
 * of this contract, even though the week is unlikely to grow.
 *
 * @property value The day as it travels on the wire.
 */
@Serializable
@JvmInline
value class Weekday(val value: String) {
    override fun toString(): String = value

    /** The seven days, from Monday as ISO 8601 counts them. */
    companion object {
        /** Monday. */
        val MONDAY = Weekday("monday")

        /** Tuesday. */
        val TUESDAY = Weekday("tuesday")

        /** Wednesday. */
        val WEDNESDAY = Weekday("wednesday")

        /** Thursday. */
        val THURSDAY = Weekday("thursday")

        /** Friday. */
        val FRIDAY = Weekday("friday")

        /** Saturday. */
        val SATURDAY = Weekday("saturday")

        /** Sunday. */
        val SUNDAY = Weekday("sunday")

        /** Every day of the week, from Monday. */
        val ALL: List<Weekday> = listOf(MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY, SATURDAY, SUNDAY)
    }
}
