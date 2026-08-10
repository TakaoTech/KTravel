/*
 * Ported from nbvcxz (me.gosimple.nbvcxz.matching.match.DateMatch).
 * Copyright (c) 2017, Adam Brusselback. MIT licence — see LICENSE-nbvcxz.txt.
 */
package com.takaotech.password.matching.match

/** A date, with or without separators: `12/03/1985`, `120385`. */
internal class DateMatch(
    match: String,
    val day: Int,
    val month: Int,
    val year: Int,
    val separator: String,
    startIndex: Int,
    endIndex: Int,
) : BaseMatch(match, startIndex, endIndex) {

    init {
        // 31 * 12 * 100 for a two-digit year, 31 * 12 * 129 for a four-digit one.
        var entropy = if (year < TWO_DIGIT_YEAR_CEILING) LOG_37200 else LOG_47988
        // Two bits for having picked a separator at all.
        if (separator.isNotEmpty()) entropy += 2
        setEntropy(entropy)
    }

    private companion object {
        const val TWO_DIGIT_YEAR_CEILING = 100
    }
}
