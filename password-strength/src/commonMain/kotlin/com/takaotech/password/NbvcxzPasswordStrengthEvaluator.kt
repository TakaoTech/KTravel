/*
 * Ported from nbvcxz (me.gosimple.nbvcxz.Nbvcxz).
 * Copyright (c) 2017, Adam Brusselback. MIT licence — see LICENSE-nbvcxz.txt.
 */
package com.takaotech.password

import com.takaotech.password.matching.DateMatcher
import com.takaotech.password.matching.DictionaryMatcher
import com.takaotech.password.matching.RepeatMatcher
import com.takaotech.password.matching.SeparatorMatcher
import com.takaotech.password.matching.SequenceMatcher
import com.takaotech.password.matching.SpacialMatcher
import com.takaotech.password.matching.YearMatcher
import com.takaotech.password.matching.match.BruteForceMatch
import com.takaotech.password.matching.match.Match
import com.takaotech.password.resources.Configuration
import com.takaotech.password.resources.ConfigurationDefaults
import com.takaotech.password.resources.Dictionaries
import com.takaotech.password.scoring.Result
import kotlin.time.Duration
import kotlin.time.TimeSource

/**
 * Kotlin Multiplatform port of [nbvcxz](https://github.com/GoSimpleLLC/nbvcxz).
 *
 * The password is decomposed into the cheapest set of recognisable patterns that covers it, and the
 * entropy of that decomposition is the estimate. Anything no matcher explains is priced at brute
 * force.
 *
 * The first call materialises roughly 170k dictionary entries, so run it off the main thread. The
 * result is cached for the lifetime of the process, and instances are safe to share.
 */
public class NbvcxzPasswordStrengthEvaluator(
    private val minimumEntropy: Double = ConfigurationDefaults.MINIMUM_ENTROPY,
    private val maxLength: Int = ConfigurationDefaults.MAX_LENGTH,
    private val distanceCalc: Boolean = ConfigurationDefaults.DISTANCE_CALC,
    private val combinationAlgorithmTimeout: Duration =
        ConfigurationDefaults.COMBINATION_ALGORITHM_TIMEOUT,
) : PasswordStrengthEvaluator {

    override fun evaluate(password: String, userInputs: List<String>): PasswordStrength {
        val configuration = configurationFor(userInputs)
        val truncated = password.take(maxLength)
        return Result(configuration, truncated, bestCombination(configuration, truncated))
            .toPasswordStrength()
    }

    private fun configurationFor(userInputs: List<String>): Configuration {
        val dictionaries = if (userInputs.isEmpty()) {
            Dictionaries.defaults
        } else {
            Dictionaries.defaults + Dictionaries.userInputs(userInputs)
        }

        return Configuration(
            passwordMatchers = MATCHERS,
            dictionaries = dictionaries,
            adjacencyGraphs = ConfigurationDefaults.ADJACENCY_GRAPHS,
            leetTable = ConfigurationDefaults.LEET_TABLE,
            yearPattern = ConfigurationDefaults.YEAR_PATTERN,
            minimumEntropy = minimumEntropy,
            maxLength = maxLength,
            distanceCalc = distanceCalc,
            combinationAlgorithmTimeout = combinationAlgorithmTimeout,
        )
    }

    /**
     * The cheap pass runs first. If it says the password looks random, or if the exhaustive search
     * runs out of time, its answer stands — the exhaustive one is exponential in the number of
     * matches and only pays off on passwords that are actually made of words.
     */
    private fun bestCombination(configuration: Configuration, password: String): List<Match> {
        if (password.isEmpty()) return emptyList()

        val allMatches = allMatches(configuration, password).toMutableList()
        val bruteForceMatches = password.indices.associateWith { index ->
            BruteForceMatch(password[index], index) as Match
        }

        val goodEnough = findGoodEnoughCombination(password, allMatches, bruteForceMatches)

        if (allMatches.isEmpty() || isRandom(password, goodEnough)) {
            val matches = mutableListOf<Match>()
            backfillBruteForce(password, bruteForceMatches, matches)
            return matches.sortedWith(START_INDEX_ORDER)
        }

        allMatches.sortWith(START_INDEX_ORDER)

        return findBestCombination(configuration, password, allMatches, bruteForceMatches)
            ?: goodEnough
    }

    private fun allMatches(configuration: Configuration, password: String): List<Match> {
        val matches = configuration.passwordMatchers.flatMap { it.match(configuration, password) }
        return keepLowestMatches(matches)
    }

    /** Where several matchers claim the same span, only the cheapest explanation survives. */
    private fun keepLowestMatches(matches: List<Match>): List<Match> = matches.filterNot { match ->
        matches.any { other ->
            match.startIndex == other.startIndex &&
                match.endIndex == other.endIndex &&
                match.averageEntropy() > other.averageEntropy()
        }
    }

    /**
     * The original, linear algorithm: for each position keep the match ending there with the lowest
     * entropy per character, then walk backwards stitching them together.
     *
     * It can miss the optimum, which is why it is only a fallback — but it is fast enough to run
     * unconditionally, and its output is what decides whether the expensive pass is worth starting.
     */
    private fun findGoodEnoughCombination(
        password: String,
        allMatches: List<Match>,
        bruteForceMatches: Map<Int, Match>,
    ): List<Match> {
        val matchAtIndex = arrayOfNulls<Match>(password.length)

        allMatches.forEach { match ->
            val existing = matchAtIndex[match.endIndex]
            if (existing == null || existing.averageEntropy() > match.averageEntropy()) {
                matchAtIndex[match.endIndex] = match
            }
        }

        val matchList = mutableListOf<Match>()
        var index = password.length - 1
        while (index >= 0) {
            val match = matchAtIndex[index]
            if (match == null) {
                matchList += bruteForceMatches.getValue(index)
                index--
                continue
            }
            matchList += match
            index = match.startIndex - 1
        }

        return matchList.asReversed()
    }

    /**
     * Builds every combination of non-overlapping matches and keeps the best.
     *
     * Returns null when the time budget runs out, which is a normal outcome for long passwords with
     * many matches — the caller falls back to the linear result.
     */
    private fun findBestCombination(
        configuration: Configuration,
        password: String,
        allMatches: List<Match>,
        bruteForceMatches: Map<Int, Match>,
    ): List<Match>? {
        if (configuration.combinationAlgorithmTimeout <= Duration.ZERO) return null

        val deadline = TimeSource.Monotonic.markNow() + configuration.combinationAlgorithmTimeout
        val nonIntersecting = nonIntersectingMatches(allMatches)

        // A match reachable from another one is never a starting point: the search would only
        // rediscover the same tails.
        val reachable = nonIntersecting.values.flatten().toIdentitySet()
        val seeds = allMatches.filterNot { match -> reachable.any { it === match } }
            .sortedWith(START_INDEX_ORDER)

        val best = BestMatches()
        seeds.forEach { seed ->
            val completed = generateMatches(
                deadline,
                password,
                seed,
                nonIntersecting,
                bruteForceMatches,
                mutableListOf(),
                0,
                best,
            )
            if (!completed) return null
        }

        return best.matches.sortedWith(START_INDEX_ORDER)
    }

    /** For each match, the matches that could follow it without overlapping. */
    private fun nonIntersectingMatches(allMatches: List<Match>): Map<Match, List<Match>> {
        val result = mutableMapOf<Match, List<Match>>()

        allMatches.forEachIndexed { index, match ->
            val forward = mutableListOf<Match>()
            for (next in index + 1 until allMatches.size) {
                val candidate = allMatches[next]
                if (candidate.startIndex <= match.endIndex) continue
                // Only the matches that start before every candidate already collected are kept, so
                // the recursion advances one step at a time instead of skipping over positions.
                if (forward.none { candidate.startIndex > it.endIndex }) {
                    forward += candidate
                }
            }
            result[match] = forward.sortedWith(START_INDEX_ORDER)
        }

        return result
    }

    /** Returns false when the deadline passed, which aborts the whole search. */
    @Suppress("LongParameterList")
    private fun generateMatches(
        deadline: TimeSource.Monotonic.ValueTimeMark,
        password: String,
        match: Match,
        nonIntersecting: Map<Match, List<Match>>,
        bruteForceMatches: Map<Int, Match>,
        matches: MutableList<Match>,
        matchesLength: Int,
        best: BestMatches,
    ): Boolean {
        if (deadline.hasPassedNow()) return false

        val index = matches.size
        matches += match
        val length = matchesLength + match.length

        var foundNext = false
        nonIntersecting[match].orEmpty().forEach { next ->
            if (matches.size > 1) {
                val previous = matches[matches.size - 2]
                if (next.startIndex < previous.endIndex && previous.startIndex < next.endIndex) {
                    return@forEach
                }
            }
            if (!generateMatches(
                    deadline,
                    password,
                    next,
                    nonIntersecting,
                    bruteForceMatches,
                    matches,
                    length,
                    best,
                )
            ) {
                return false
            }
            foundNext = true
        }

        if (!foundNext) {
            // The most complete cover wins, and among equally complete ones the cheapest per
            // character does.
            val current = best.matches
            if (current.isEmpty() ||
                (
                    length >= best.matchLength &&
                        entropyOf(matches) / length < entropyOf(current) / best.matchLength
                    )
            ) {
                val chosen = matches.toMutableList()
                backfillBruteForce(password, bruteForceMatches, chosen)
                best.matches = chosen
                best.matchLength = length
            }
        }

        matches.removeAt(index)
        return true
    }

    /**
     * A password counts as random when the matchers explain less than half of it, or less than 80%
     * with no single match covering more than a quarter — at that point the pattern search is noise
     * and brute force is the honest estimate.
     */
    private fun isRandom(password: String, matches: List<Match>): Boolean {
        val explained = matches.filterNot { it is BruteForceMatch }
        val matchedLength = explained.sumOf { it.length }
        val longestMatch = explained.maxOfOrNull { it.length } ?: 0

        return matchedLength < password.length * HALF ||
            (matchedLength < password.length * MOSTLY && password.length * QUARTER > longestMatch)
    }

    /** Brute force entropy is excluded: it is the baseline every combination shares. */
    private fun entropyOf(matches: List<Match>): Double =
        matches.filterNot { it is BruteForceMatch }.sumOf { it.calculateEntropy() }

    /** Fills every position no match covers with its brute force match. */
    private fun backfillBruteForce(password: String, bruteForceMatches: Map<Int, Match>, matches: MutableList<Match>) {
        val missing = password.indices
            .filterNot { index -> matches.any { index >= it.startIndex && index <= it.endIndex } }
            .map { bruteForceMatches.getValue(it) }
            .toIdentitySet()

        matches += missing
    }

    /**
     * Matches carry no equality of their own, so membership has to be by identity — two matches with
     * the same token and span are still distinct results from distinct matchers.
     */
    private fun List<Match>.toIdentitySet(): List<Match> {
        val seen = mutableListOf<Match>()
        forEach { candidate ->
            if (seen.none { it === candidate }) seen += candidate
        }
        return seen
    }

    private class BestMatches {
        var matches: List<Match> = emptyList()
        var matchLength: Int = 0
    }

    private companion object {
        val MATCHERS = listOf(
            DateMatcher(),
            YearMatcher(),
            RepeatMatcher(),
            SequenceMatcher(),
            SpacialMatcher(),
            DictionaryMatcher(),
            SeparatorMatcher(),
        )

        /** Start index first, then shorter tokens first. */
        val START_INDEX_ORDER: Comparator<Match> =
            compareBy({ it.startIndex }, { it.token.length })

        const val HALF = 0.5
        const val MOSTLY = 0.8
        const val QUARTER = 0.25
    }
}
