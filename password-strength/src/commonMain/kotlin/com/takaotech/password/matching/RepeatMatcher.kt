/*
 * Ported from nbvcxz (me.gosimple.nbvcxz.matching.RepeatMatcher).
 * Copyright (c) 2017, Adam Brusselback. MIT licence — see LICENSE-nbvcxz.txt.
 */
package com.takaotech.password.matching

import com.takaotech.password.matching.match.Match
import com.takaotech.password.matching.match.RepeatMatch
import com.takaotech.password.resources.Configuration

/**
 * Finds repeated blocks: `aaa`, `abcabcabc`.
 *
 * Two passes run side by side, greedy and lazy, and the longer find wins — that is how `aabaabaab`
 * is reported as `aab` repeated rather than `a` repeated.
 *
 * Upstream expresses both passes as the back referencing patterns `(.+)\1+` and `(.+?)\1+`. They are
 * searched by hand here instead: the regex engine Kotlin ships for the native and Wasm targets
 * resolves a back reference to a match whose repetition never happened — on `hello world this is a
 * long passphrase` the greedy pattern reports the whole tail as a single "repeat" — so the estimate
 * came out different there than on the JVM, which is the platform the reference values come from.
 */
internal class RepeatMatcher : PasswordMatcher {

    override fun match(configuration: Configuration, password: String): List<Match> {
        val matches = mutableListOf<Match>()

        // Both sequences are consumed in step, mirroring the two stateful Java matchers.
        val greedyMatches = findRepeats(password, longestUnitFirst = true).iterator()
        val lazyMatches = findRepeats(password, longestUnitFirst = false).iterator()

        var lastIndex = 0
        while (lastIndex < password.length) {
            if (!greedyMatches.hasNext()) break
            val greedy = greedyMatches.next()
            val lazy = if (lazyMatches.hasNext()) lazyMatches.next() else null

            val useGreedy = greedy.token.length > (lazy?.token?.length ?: 0)
            val winner = if (useGreedy) greedy else lazy ?: greedy

            // A repeating unit built from more than four distinct characters is not a repeat worth
            // reporting: it is more likely a coincidence inside a longer password.
            if (winner.repeatingCharacters.toSet().size <= MAX_DISTINCT_CHARACTERS) {
                matches += RepeatMatch(
                    winner.token,
                    winner.repeatingCharacters,
                    winner.startIndex,
                    winner.endIndex,
                )
            }
            lastIndex = winner.endIndex + 1
        }

        return matches
    }

    /**
     * Every repeat in [password], left to right and without overlapping, exactly as `findAll` would
     * return them.
     *
     * [longestUnitFirst] is what tells the greedy pass from the lazy one: at a given position both
     * report the same repeat when there is only one, and pick opposite ends of the candidate units
     * when there are several — `aabaabaab` is one `aab` repeated for the greedy pass and one `a`
     * repeated for the lazy one.
     */
    private fun findRepeats(password: String, longestUnitFirst: Boolean): List<Repeat> {
        val repeats = mutableListOf<Repeat>()

        var start = 0
        while (start < password.length) {
            val repeat = repeatAt(password, start, longestUnitFirst)
            if (repeat == null) {
                start++
            } else {
                repeats += repeat
                start = repeat.endIndex + 1
            }
        }

        return repeats
    }

    /**
     * The repeat starting exactly at [start], or null when nothing there repeats.
     *
     * Whichever unit length is tried first and holds is the one kept — that is the group of the
     * corresponding pattern — and the repetitions after it are always taken as far as they go, which
     * is what the unconditionally greedy `+` of both patterns does.
     */
    private fun repeatAt(password: String, start: Int, longestUnitFirst: Boolean): Repeat? {
        // A repeat needs the unit twice over, so nothing longer than half of what is left can be one.
        val longestUnit = (password.length - start) / 2
        val unitLengths = if (longestUnitFirst) longestUnit downTo 1 else 1..longestUnit

        val unitLength = unitLengths.firstOrNull { length ->
            !containsLineTerminator(password, start, length) &&
                password.regionMatches(start, password, start + length, length)
        } ?: return null

        var end = start + 2 * unitLength
        while (end + unitLength <= password.length && password.regionMatches(start, password, end, unitLength)) {
            end += unitLength
        }

        return Repeat(
            token = password.substring(start, end),
            repeatingCharacters = password.substring(start, start + unitLength),
            startIndex = start,
            endIndex = end - 1,
        )
    }

    /**
     * The `.` of both patterns does not match a line terminator, so neither does a unit here: a
     * password holding one is split around it rather than repeated across it.
     */
    private fun containsLineTerminator(password: String, start: Int, length: Int): Boolean =
        (start until start + length).any { index -> password[index] in LINE_TERMINATORS }

    /** A unit and the run it makes up, the shape both patterns yield a match in. */
    private class Repeat(val token: String, val repeatingCharacters: String, val startIndex: Int, val endIndex: Int)

    private companion object {
        const val MAX_DISTINCT_CHARACTERS = 4

        /** What `Pattern` treats as a line terminator, which is what upstream runs on. */
        val LINE_TERMINATORS = charArrayOf('\n', '\r', '\u0085', '\u2028', '\u2029')
    }
}
