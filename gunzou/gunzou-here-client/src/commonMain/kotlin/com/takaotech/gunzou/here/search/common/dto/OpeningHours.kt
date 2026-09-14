package com.takaotech.gunzou.here.search.common.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * One set of opening hours of a place. A day it does not cover is a day the place is closed.
 *
 * @property text Human-readable hours, such as `Mon-Sat: 10:00 - 20:00`
 * @property structured The same hours as iCalendar-based rules
 * @property isOpen Whether the place is open at the time of the request
 * @property categories Place categories these hours apply to, when not the whole place
 */
@Serializable
data class OpeningHours(
    @SerialName("text") val text: List<String> = emptyList(),
    @SerialName("structured") val structured: List<StructuredOpeningHours> = emptyList(),
    @SerialName("isOpen") val isOpen: Boolean? = null,
    @SerialName("categories") val categories: List<CategoryRef>? = null,
)

/**
 * An opening period as HERE encodes it, close to iCalendar (RFC 5545).
 *
 * The values are kept as HERE sends them: they are not standard enough for a library to parse.
 *
 * @property start Start time without a date, such as `T132000` for 13:20
 * @property duration iCalendar duration, such as `PT07H00M`. A closed day is `PT00:00M`.
 * @property recurrence iCalendar RECUR rule with `:` instead of `=`, such as
 *   `FREQ:DAILY;BYDAY:MO,TU,WE,TH,FR`
 */
@Serializable
data class StructuredOpeningHours(
    @SerialName("start") val start: String,
    @SerialName("duration") val duration: String,
    @SerialName("recurrence") val recurrence: String,
)
