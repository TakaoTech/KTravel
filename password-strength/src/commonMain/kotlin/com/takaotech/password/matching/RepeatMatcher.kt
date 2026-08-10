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
 */
internal class RepeatMatcher : PasswordMatcher {

    override fun match(configuration: Configuration, password: String): List<Match> {
        val matches = mutableListOf<Match>()

        // Both sequences are consumed in step, mirroring the two stateful Java matchers.
        val greedyMatches = GREEDY.findAll(password).iterator()
        val lazyMatches = LAZY.findAll(password).iterator()

        var lastIndex = 0
        while (lastIndex < password.length) {
            if (!greedyMatches.hasNext()) break
            val greedy = greedyMatches.next()
            val lazy = if (lazyMatches.hasNext()) lazyMatches.next() else null

            val useGreedy = greedy.value.length > (lazy?.value?.length ?: 0)
            val winner = if (useGreedy) greedy else lazy ?: greedy

            val baseToken: String
            val repeatCharacters: String
            if (useGreedy) {
                // Re-anchoring finds the shortest unit the greedy run is built from.
                val anchored = LAZY_ANCHORED.find(greedy.value)
                baseToken = anchored?.value ?: greedy.value
                // Upstream calls find() a second time to read this group. That call always fails on
                // an anchored pattern, so the value comes from the greedy match either way; kept
                // equivalent rather than literal.
                repeatCharacters = greedy.groupValues[1]
            } else {
                baseToken = winner.value
                repeatCharacters = winner.groupValues[1]
            }

            val startIndex = winner.range.first
            val endIndex = winner.range.last

            // A repeating unit built from more than four distinct characters is not a repeat worth
            // reporting: it is more likely a coincidence inside a longer password.
            if (repeatCharacters.toSet().size <= MAX_DISTINCT_CHARACTERS) {
                matches += RepeatMatch(baseToken, repeatCharacters, startIndex, endIndex)
            }
            lastIndex = endIndex + 1
        }

        return matches
    }

    private companion object {
        val GREEDY = Regex("""(.+)\1+""")
        val LAZY = Regex("""(.+?)\1+""")
        val LAZY_ANCHORED = Regex("""^(.+?)\1+$""")
        const val MAX_DISTINCT_CHARACTERS = 4
    }
}
