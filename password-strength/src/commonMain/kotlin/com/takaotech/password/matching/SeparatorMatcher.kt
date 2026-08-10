/*
 * Ported from nbvcxz (me.gosimple.nbvcxz.matching.SeparatorMatcher and YearMatcher).
 * Copyright (c) 2017, Adam Brusselback. MIT licence — see LICENSE-nbvcxz.txt.
 */
package com.takaotech.password.matching

import com.takaotech.password.matching.match.Match
import com.takaotech.password.matching.match.SeparatorMatch
import com.takaotech.password.matching.match.YearMatch
import com.takaotech.password.resources.Configuration

/**
 * Finds the character the user picked to separate the parts of their password.
 *
 * Only the most frequent non-alphanumeric qualifies, and only where it sits inside the password:
 * one at either end is decoration, not structure.
 */
internal class SeparatorMatcher : PasswordMatcher {

    override fun match(configuration: Configuration, password: String): List<Match> {
        if (password.length <= MIN_LENGTH) return emptyList()

        val occurrences = NON_ALPHA_NUMERIC.findAll(password)
            .filter { it.isInterior(password) }
            .toList()

        val separator = occurrences
            .groupingBy { it.value }
            .eachCount()
            .maxByOrNull { it.value }
            ?.key
            ?: return emptyList()

        return occurrences
            .filter { it.value == separator }
            .map { SeparatorMatch(it.value, it.range.first, it.range.last) }
    }

    /** Upstream compares the exclusive end against `length - 1`, so the final two positions count as edges. */
    private fun MatchResult.isInterior(password: String): Boolean =
        range.first != 0 && range.last + 1 != password.length - 1

    private companion object {
        val NON_ALPHA_NUMERIC = Regex("""[^a-zA-Z\d]""")
        const val MIN_LENGTH = 5
    }
}

/** Finds four-digit years that people actually pick, per the configured pattern. */
internal class YearMatcher : PasswordMatcher {

    override fun match(configuration: Configuration, password: String): List<Match> =
        configuration.yearPattern.findAll(password)
            .map { YearMatch(it.value, it.range.first, it.range.last) }
            .toList()
}
