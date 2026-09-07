package com.takaotech.ktravel.core.logging

import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.minus

/** How many days of log are kept when the user has not chosen otherwise. */
const val DEFAULT_LOG_RETENTION_DAYS: Int = 3

/** The fewest days that can be kept: today's file, which is the one a bug report needs. */
const val MIN_LOG_RETENTION_DAYS: Int = 1

/** The most days that can be kept, so a forgotten setting cannot fill the device. */
const val MAX_LOG_RETENTION_DAYS: Int = 30

/**
 * How long the log files live.
 *
 * Pure on purpose: deciding what is expired is the part worth testing, and it says nothing about
 * files. [LogFileSink] does the deleting, with the days this returns.
 */
object LogRetention {

    /**
     * [days] brought inside the allowed range, so a stored or typed value can never widen it.
     *
     * @param days What the user or the stored settings asked for.
     */
    fun coerce(days: Int): Int = days.coerceIn(MIN_LOG_RETENTION_DAYS, MAX_LOG_RETENTION_DAYS)

    /**
     * Which of [days] are past their keeping.
     *
     * A retention of three days keeps today and the two days before it. Days in the future are kept:
     * they only happen when the device clock moves backwards, and deleting them would throw away the
     * log of the session that is running.
     *
     * @param days The days that have a file.
     * @param today The day the device is on.
     * @param retentionDays How many days are kept, already coerced.
     */
    fun expired(days: List<LocalDate>, today: LocalDate, retentionDays: Int): List<LocalDate> {
        val oldestKept = today.minus(DatePeriod(days = coerce(retentionDays) - 1))

        return days.filter { it < oldestKept }
    }
}
