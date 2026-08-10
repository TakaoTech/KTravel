/*
 * Ported from nbvcxz (me.gosimple.nbvcxz.matching.DateMatcher).
 * Copyright (c) 2017, Adam Brusselback. MIT licence — see LICENSE-nbvcxz.txt.
 */
package com.takaotech.password.matching

import com.takaotech.password.matching.match.DateMatch
import com.takaotech.password.matching.match.Match
import com.takaotech.password.resources.Configuration

/** Finds dates, written either with separators (`12/03/1985`) or without (`120385`). */
internal class DateMatcher : PasswordMatcher {

    override fun match(configuration: Configuration, password: String): List<Match> =
        matchDatesWithoutSeparator(password) + matchDatesWithSeparator(password)

    /**
     * A run of six to eight digits can be a date in several ways at once — `121234` reads as
     * `12/12/34` and as `12/1234` — so every split is tried and each valid one becomes a match.
     */
    private fun matchDatesWithoutSeparator(password: String): List<DateMatch> {
        val matches = mutableListOf<DateMatch>()

        for (start in password.indices) {
            for (end in start + MIN_BARE_DATE_LENGTH..password.length) {
                val chunk = password.substring(start, end)
                if (!DATE_WITHOUT_SEPARATOR.matches(chunk)) continue

                partialSplits(chunk).flatMap(::fullSplits).forEach { (day, month, year) ->
                    validate(day, month, year)?.let { valid ->
                        matches += DateMatch(
                            match = chunk,
                            day = valid.day,
                            month = valid.month,
                            year = valid.year,
                            separator = "",
                            startIndex = start,
                            endIndex = end - 1,
                        )
                    }
                }
            }
        }

        return matches
    }

    /** Splits a digit run into day-and-month plus year, with the year at either end. */
    private fun partialSplits(chunk: String): List<PartialSplit> {
        val splits = mutableListOf<PartialSplit>()
        val length = chunk.length

        if (length <= SIX) {
            splits += PartialSplit(chunk.substring(2), chunk.substring(0, 2))
            splits += PartialSplit(chunk.substring(0, length - 2), chunk.substring(length - 2))
        }
        if (length >= SIX) {
            splits += PartialSplit(chunk.substring(4), chunk.substring(0, 4))
            splits += PartialSplit(chunk.substring(0, length - 4), chunk.substring(length - 4))
        }

        return splits
    }

    /** Splits the day-and-month part every way it can be read, e.g. `123` as `1/23` or `12/3`. */
    private fun fullSplits(split: PartialSplit): List<FullSplit> {
        val dayAndMonth = split.dayAndMonth
        return when (dayAndMonth.length) {
            2 -> listOf(FullSplit(dayAndMonth.substring(0, 1), dayAndMonth.substring(1, 2), split.year))

            3 -> listOf(
                FullSplit(dayAndMonth.substring(0, 1), dayAndMonth.substring(1, 3), split.year),
                FullSplit(dayAndMonth.substring(0, 2), dayAndMonth.substring(2, 3), split.year),
            )

            4 -> listOf(FullSplit(dayAndMonth.substring(0, 2), dayAndMonth.substring(2, 4), split.year))

            else -> emptyList()
        }
    }

    private fun matchDatesWithSeparator(password: String): List<DateMatch> {
        val matches = mutableListOf<DateMatch>()

        for (start in password.indices) {
            for (end in start + MIN_SEPARATED_DATE_LENGTH..password.length) {
                val chunk = password.substring(start, end)

                DATE_WITH_SEPARATOR_YEAR_SUFFIX.matchEntire(chunk)?.let { result ->
                    val (day, separator, month, year) = result.destructured
                    validate(day, month, year)?.let { valid ->
                        matches += DateMatch(chunk, valid.day, valid.month, valid.year, separator, start, end - 1)
                    }
                }

                DATE_WITH_SEPARATOR_YEAR_PREFIX.matchEntire(chunk)?.let { result ->
                    val (year, separator, dayOrMonth, monthOrDay) = result.destructured
                    validate(monthOrDay, dayOrMonth, year)?.let { valid ->
                        matches += DateMatch(chunk, valid.day, valid.month, valid.year, separator, start, end - 1)
                    }
                }
            }
        }

        return matches
    }

    /** Years must be two digits, or four digits inside the range upstream considers plausible. */
    private fun validate(day: String, month: String, year: String): ValidDate? {
        val dayValue = day.toIntOrNull() ?: return null
        val monthValue = month.toIntOrNull() ?: return null
        val yearValue = year.toIntOrNull() ?: return null

        val invalid = dayValue <= 0 || dayValue > MAX_DAY ||
            monthValue <= 0 || monthValue > MAX_MONTH ||
            yearValue <= 0 ||
            (yearValue >= FOUR_DIGIT_YEAR && (yearValue < MIN_YEAR || yearValue > MAX_YEAR))

        return if (invalid) null else ValidDate(dayValue, monthValue, yearValue)
    }

    private data class PartialSplit(val dayAndMonth: String, val year: String)
    private data class FullSplit(val day: String, val month: String, val year: String)
    private data class ValidDate(val day: Int, val month: Int, val year: Int)

    private companion object {
        val DATE_WITHOUT_SEPARATOR = Regex("""^\d{6,8}$""")

        // The back-reference forces the same separator on both sides, so `12/03-85` is not a date.
        val DATE_WITH_SEPARATOR_YEAR_SUFFIX =
            Regex("""^(\d{1,2})(\s|-|/|\\|_|\.)(\d{1,2})\2(19\d{2}|200\d|201\d|\d{2})$""")
        val DATE_WITH_SEPARATOR_YEAR_PREFIX =
            Regex("""^(19\d{2}|200\d|201\d|\d{2})(\s|-|/|\\|_|\.)(\d{1,2})\2(\d{1,2})$""")

        const val MIN_BARE_DATE_LENGTH = 4
        const val MIN_SEPARATED_DATE_LENGTH = 6
        const val SIX = 6
        const val MAX_DAY = 31
        const val MAX_MONTH = 12
        const val FOUR_DIGIT_YEAR = 100
        const val MIN_YEAR = 1900
        const val MAX_YEAR = 2019
    }
}
