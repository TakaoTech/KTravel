/*
 * Ported from nbvcxz (me.gosimple.nbvcxz.resources.BruteForceUtil).
 * Copyright (c) 2017, Adam Brusselback. MIT licence — see LICENSE-nbvcxz.txt.
 */
package com.takaotech.password.resources

/**
 * Size of the alphabet a brute force attack would have to walk to produce the given text: the sum of
 * the classes actually present in it.
 */
internal object BruteForceUtil {

    private const val DIGITS = 10
    private const val LETTERS = 26
    private const val SYMBOLS = 33
    private const val UNICODE = 100

    fun bruteForceCardinality(password: String): Int {
        var lower = false
        var upper = false
        var digits = false
        var symbols = false
        var unicode = false

        password.forEach { character ->
            when {
                character in '0'..'9' -> digits = true
                character in 'A'..'Z' -> upper = true
                character in 'a'..'z' -> lower = true
                character.code <= 0x7f -> symbols = true
                else -> unicode = true
            }
        }

        return cardinalityOf(lower, upper, digits, symbols, unicode)
    }

    fun bruteForceCardinality(character: Char): Int = cardinalityOf(
        lower = character in 'a'..'z',
        upper = character in 'A'..'Z',
        digits = character in '0'..'9',
        symbols = character.code <= 0x7f && character !in 'a'..'z' &&
            character !in 'A'..'Z' && character !in '0'..'9',
        unicode = character.code > 0x7f,
    )

    private fun cardinalityOf(
        lower: Boolean,
        upper: Boolean,
        digits: Boolean,
        symbols: Boolean,
        unicode: Boolean,
    ): Int {
        var cardinality = 0
        if (digits) cardinality += DIGITS
        if (upper) cardinality += LETTERS
        if (lower) cardinality += LETTERS
        if (symbols) cardinality += SYMBOLS
        if (unicode) cardinality += UNICODE
        return cardinality
    }
}
