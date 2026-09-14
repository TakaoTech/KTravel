package com.takaotech.gunzou.here.search.common.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Time zone of a place, only with `show=tz`.
 *
 * @property name Name in the tz database, such as `Europe/Rome`
 * @property utcOffset Offset from UTC at the time of the request, such as `+02:00`
 */
@Serializable
data class TimeZoneInfo(@SerialName("name") val name: String, @SerialName("utcOffset") val utcOffset: String)
