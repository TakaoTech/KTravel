/*
 * Ported from nbvcxz (me.gosimple.nbvcxz.matching.DictionaryMatcher).
 * Copyright (c) 2017, Adam Brusselback. MIT licence — see LICENSE-nbvcxz.txt.
 */
package com.takaotech.password.matching

import com.takaotech.password.matching.match.DictionaryMatch
import com.takaotech.password.matching.match.LeetSubstitution
import com.takaotech.password.matching.match.Match
import com.takaotech.password.resources.Configuration
import com.takaotech.password.resources.Dictionary
import kotlin.math.max
import kotlin.math.min

/**
 * Finds every substring of the password that appears in one of the dictionaries, whether written
 * plainly, reversed, in leet, or close enough to be reached by a few edits.
 */
internal class DictionaryMatcher : PasswordMatcher {

    override fun match(configuration: Configuration, password: String): List<Match> {
        val matches = mutableListOf<Match>()

        // Every substring of the password is a candidate; the combiner later picks which ones win.
        for (start in password.indices) {
            for (end in start + 1..password.length) {
                val part = password.substring(start, end)
                configuration.dictionaries.forEach { dictionary ->
                    matchPart(configuration, password, dictionary, part, start, end, matches)
                }
            }
        }

        return matches
    }

    @Suppress("LongParameterList", "ReturnCount")
    private fun matchPart(
        configuration: Configuration,
        password: String,
        dictionary: Dictionary,
        part: String,
        start: Int,
        end: Int,
        matches: MutableList<Match>,
    ) {
        val lower = part.lowercase()

        dictionary.dictionary[lower]?.let { rank ->
            matches += dictionaryMatch(part, start, end, lower, rank, dictionary)
            return
        }

        val reversed = lower.reversed()
        dictionary.dictionary[reversed]?.let { rank ->
            matches += dictionaryMatch(part, start, end, reversed, rank, dictionary, reversed = true)
            return
        }

        if (dictionary.maxLength >= part.length) {
            translateLeet(configuration, lower).forEach { unleet ->
                dictionary.dictionary[unleet]?.let { rank ->
                    matches += dictionaryMatch(
                        part,
                        start,
                        end,
                        unleet,
                        rank,
                        dictionary,
                        leetSubstitution = leetSubstitutions(lower, unleet),
                    )
                    return@forEach
                }

                val reversedUnleet = unleet.reversed()
                dictionary.dictionary[reversedUnleet]?.let { rank ->
                    matches += dictionaryMatch(
                        part,
                        start,
                        end,
                        reversedUnleet,
                        rank,
                        dictionary,
                        leetSubstitution = leetSubstitutions(reversed, reversedUnleet),
                        reversed = true,
                    )
                }
            }
        }

        distanceMatch(configuration, password, dictionary, part, start, end)?.let { matches += it }
    }

    @Suppress("LongParameterList")
    private fun dictionaryMatch(
        part: String,
        start: Int,
        end: Int,
        value: String,
        rank: Int,
        dictionary: Dictionary,
        leetSubstitution: List<LeetSubstitution> = emptyList(),
        reversed: Boolean = false,
        distance: Int = 0,
    ) = DictionaryMatch(
        match = part,
        startIndex = start,
        endIndex = end - 1,
        dictionaryValue = value,
        rank = rank,
        leetSubstitution = leetSubstitution,
        excluded = dictionary.exclusion,
        reversed = reversed,
        dictionaryName = dictionary.dictionaryName,
        distance = distance,
    )

    /**
     * Finds the closest dictionary entry within an edit distance of a quarter of the password's
     * length — this is what catches `passw0rd1` from `password`.
     *
     * Only runs against the whole password: applying it to every substring would swamp the result
     * with near-misses of two-letter fragments.
     */
    @Suppress("LongParameterList", "ReturnCount")
    private fun distanceMatch(
        configuration: Configuration,
        password: String,
        dictionary: Dictionary,
        part: String,
        start: Int,
        end: Int,
    ): Match? {
        if (!configuration.distanceCalc) return null
        if (!(start == 0 && end == password.length)) return null
        // Short values produce false positives at any threshold.
        if (password.length < MIN_DISTANCE_LENGTH) return null

        val threshold = password.length / DISTANCE_THRESHOLD_DIVISOR
        val lookup = dictionary.sortedDictionaryLengthLookup
        val size = dictionary.sortedDictionary.size

        // The sorted dictionary lets us look only at entries whose length could possibly be within
        // the threshold, instead of the whole list.
        val startIndex = lookup[password.length - threshold] ?: size
        val endIndex = lookup[password.length + threshold + 1] ?: size
        if (startIndex >= endIndex) return null

        var bestDistance = Int.MAX_VALUE
        var bestValue: String? = null
        var bestRank: Int? = null

        dictionary.sortedDictionary.subList(startIndex, endIndex).forEach { key ->
            val distance = levenshtein(password, key, threshold)
            if (distance != -1) {
                val rank = dictionary.dictionary.getValue(key)
                val currentBestRank = bestRank
                if (distance <= bestDistance && (currentBestRank == null || rank <= currentBestRank)) {
                    bestDistance = distance
                    bestValue = key
                    bestRank = rank
                }
            }
        }

        val rank = bestRank ?: return null
        return dictionaryMatch(
            part,
            start,
            end,
            bestValue.orEmpty(),
            rank,
            dictionary,
            distance = bestDistance,
        )
    }

    /**
     * Every way the password could read once leet substitutions are undone.
     *
     * A password made entirely of substitutable characters is skipped: it would explode into
     * combinations without ever being a word.
     */
    private fun translateLeet(configuration: Configuration, password: String): List<String> {
        val replacements = password.mapIndexedNotNull { index, character ->
            configuration.leetTable[character]?.let { index to it }
        }

        if (replacements.isEmpty() || replacements.size == password.length) return emptyList()

        val translations = mutableListOf<String>()
        replaceAtIndex(replacements, 0, password.toCharArray(), translations)
        return translations
    }

    /** Walks the substitutable positions in order, branching on each candidate character. */
    private fun replaceAtIndex(
        replacements: List<Pair<Int, List<Char>>>,
        current: Int,
        password: CharArray,
        translations: MutableList<String>,
    ) {
        val (index, candidates) = replacements[current]
        candidates.forEach { replacement ->
            password[index] = replacement
            when {
                current == replacements.lastIndex -> translations += password.concatToString()

                // Upstream bails out after a hundred variants rather than exploring the whole tree.
                translations.size > MAX_LEET_TRANSLATIONS -> return

                else -> replaceAtIndex(replacements, current + 1, password, translations)
            }
        }
    }

    /** The positions where the password and its un-leeted form differ. */
    private fun leetSubstitutions(password: String, unleet: String): List<LeetSubstitution> = unleet.indices
        .filter { password[it] != unleet[it] }
        .map { LeetSubstitution(original = password[it], substituted = unleet[it]) }

    /**
     * Levenshtein distance, giving up as soon as it exceeds [threshold] and returning -1.
     *
     * Only a diagonal stripe of width `2 * threshold + 1` of the cost table is computed, which turns
     * the O(nm) algorithm into O(threshold * m) — the reason this can run against a 100k-entry
     * dictionary at all. Two rows are kept and swapped instead of the full matrix.
     */
    @Suppress("ReturnCount", "NestedBlockDepth")
    private fun levenshtein(left: String, right: String, threshold: Int): Int {
        var shorter = left
        var longer = right
        var n = shorter.length
        var m = longer.length

        if (n == 0) return if (m <= threshold) m else -1
        if (m == 0) return if (n <= threshold) n else -1

        if (n > m) {
            val swap = shorter
            shorter = longer
            longer = swap
            n = shorter.length
            m = longer.length
        }

        var previous = IntArray(n + 1) { Int.MAX_VALUE }
        var current = IntArray(n + 1) { Int.MAX_VALUE }

        // Entries beyond the stripe stay at MAX_VALUE so they are ignored when compared against.
        val boundary = min(n, threshold) + 1
        for (i in 0 until boundary) {
            previous[i] = i
        }

        for (j in 1..m) {
            val rightCharacter = longer[j - 1]
            current[0] = j

            val minIndex = max(1, j - threshold)
            val maxIndex = min(n, j + threshold)

            // The stripe has run off the table: the strings are too far apart in length.
            if (minIndex > maxIndex) return -1

            if (minIndex > 1) {
                current[minIndex - 1] = Int.MAX_VALUE
            }

            for (i in minIndex..maxIndex) {
                current[i] = if (shorter[i - 1] == rightCharacter) {
                    previous[i - 1]
                } else {
                    1 + minOf(current[i - 1], previous[i], previous[i - 1])
                }
            }

            val swap = previous
            previous = current
            current = swap
        }

        return if (previous[n] <= threshold) previous[n] else -1
    }

    private companion object {
        const val MAX_LEET_TRANSLATIONS = 100
        const val MIN_DISTANCE_LENGTH = 3
        const val DISTANCE_THRESHOLD_DIVISOR = 4
    }
}
