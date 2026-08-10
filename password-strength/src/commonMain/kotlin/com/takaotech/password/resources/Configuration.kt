/*
 * Ported from nbvcxz (me.gosimple.nbvcxz.resources.Configuration and ConfigurationBuilder).
 * Copyright (c) 2017, Adam Brusselback. MIT licence — see LICENSE-nbvcxz.txt.
 */
package com.takaotech.password.resources

import com.takaotech.password.matching.PasswordMatcher
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

/**
 * Everything the estimation is parameterised on.
 *
 * The upstream `Locale`, `ResourceBundle` and guess-type fields are not ported: feedback is symbolic
 * here (see `PasswordWarning`), and crack-time display is the caller's business.
 *
 * Nothing here depends on the current date. Upstream reads the clock in exactly two places — the
 * combination-algorithm timeout, which becomes a monotonic measurement, and the Moore's-law scaling
 * of the guess types, which is not ported. The date matcher works off fixed year ranges.
 */
internal class Configuration(
    val passwordMatchers: List<PasswordMatcher>,
    val dictionaries: List<Dictionary>,
    val adjacencyGraphs: List<AdjacencyGraph>,
    val leetTable: Map<Char, List<Char>>,
    val yearPattern: Regex,
    val minimumEntropy: Double,
    val maxLength: Int,
    val distanceCalc: Boolean,
    val combinationAlgorithmTimeout: Duration,
)

internal object ConfigurationDefaults {

    const val MINIMUM_ENTROPY: Double = 35.0
    const val MAX_LENGTH: Int = 256
    const val DISTANCE_CALC: Boolean = true
    val COMBINATION_ALGORITHM_TIMEOUT: Duration = 500.milliseconds

    /** Years 1900-2029. */
    val YEAR_PATTERN: Regex = Regex("""19\d\d|200\d|201\d|202\d""")

    /** Common English leet substitutions: the character typed, and what it stands in for. */
    val LEET_TABLE: Map<Char, List<Char>> = mapOf(
        '4' to listOf('a'),
        '@' to listOf('a'),
        '8' to listOf('b'),
        '(' to listOf('c'),
        '{' to listOf('c'),
        '[' to listOf('c'),
        '<' to listOf('c'),
        '3' to listOf('e'),
        '9' to listOf('g'),
        '6' to listOf('g'),
        '&' to listOf('g'),
        '#' to listOf('h'),
        '!' to listOf('i', 'l'),
        '1' to listOf('i', 'l'),
        '|' to listOf('i', 'l'),
        '0' to listOf('o'),
        '$' to listOf('s'),
        '5' to listOf('s'),
        '+' to listOf('t'),
        '7' to listOf('t', 'l'),
        '%' to listOf('x'),
        '2' to listOf('z'),
    )

    /** QWERTY, standard keypad and Mac keypad — the set upstream enables by default. */
    val ADJACENCY_GRAPHS: List<AdjacencyGraph> = KeyboardGraphs.defaults
}
