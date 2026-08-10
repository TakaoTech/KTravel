/*
 * Ported from nbvcxz (me.gosimple.nbvcxz.matching.match.DictionaryMatch).
 * Copyright (c) 2017, Adam Brusselback. MIT licence — see LICENSE-nbvcxz.txt.
 */
package com.takaotech.password.matching.match

import com.takaotech.password.resources.BruteForceUtil
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

/** A leet substitution that was undone to reach a dictionary entry: [original] written as [substituted]. */
internal data class LeetSubstitution(val original: Char, val substituted: Char)

/**
 * A token found in one of the dictionaries. The entropy is the rank of the entry plus what the user
 * did on top of it: upper casing, leet substitutions, reversing, and edit distance.
 */
internal class DictionaryMatch(
    match: String,
    startIndex: Int,
    endIndex: Int,
    val dictionaryValue: String,
    val rank: Int,
    val leetSubstitution: List<LeetSubstitution>,
    val excluded: Boolean,
    val reversed: Boolean,
    val dictionaryName: String,
    val distance: Int,
) : BaseMatch(match, startIndex, endIndex) {

    val isLeet: Boolean get() = leetSubstitution.isNotEmpty()

    init {
        setEntropy(
            if (excluded) {
                // An entry in an exclusion dictionary is worth nothing by definition.
                0.0
            } else {
                log2(rank.toDouble()) + uppercaseEntropy() + leetEntropy() +
                    reversedEntropy() + distanceEntropy()
            },
        )
    }

    private fun distanceEntropy(): Double {
        if (distance == 0) return 0.0

        val lengthDifference = token.length - dictionaryValue.length
        val characterShift = distance - abs(lengthDifference)

        return if (lengthDifference + characterShift <= 0) {
            // Shortening the word barely helps.
            1.0
        } else {
            log2(
                BruteForceUtil.bruteForceCardinality(token).toDouble() *
                    (lengthDifference + characterShift),
            )
        }
    }

    @Suppress("ReturnCount")
    private fun uppercaseEntropy(): Double {
        // The common patterns are cheap: all lower, all upper, first upper, last upper.
        if (token.lowercase() == token) return 0.0
        if (token.uppercase() == token) return 1.0

        val withoutFirst = token.substring(1)
        if (token[0].isUpperCase() && withoutFirst.lowercase() == withoutFirst) return 1.0

        // Upstream drops the last *two* characters here rather than one. Kept as is: correcting it
        // would shift the entropy of every last-upper password away from the reference values.
        val withoutLast = token.substring(0, token.length - 2)
        if (token[token.length - 1].isUpperCase() && withoutLast.lowercase() == withoutLast) {
            return 1.0
        }

        val upperCount = token.count(Char::isUpperCase)
        val lowerCount = token.count(Char::isLowerCase)

        // Anything else costs the number of ways to pick which characters got capitalised.
        var possibilities = 0L
        val totalCase = upperCount + lowerCount
        for (i in 0..min(upperCount, lowerCount)) {
            possibilities += nCk(totalCase, i)
        }

        return max(log2(possibilities.toDouble()), 1.0)
    }

    private fun leetEntropy(): Double {
        if (!isLeet) return 0.0

        var possibilities = 0L
        leetSubstitution.forEach { substitution ->
            val substituted = token.count { it == substitution.substituted }
            val untouched = token.count { it == substitution.original }
            val total = substituted + untouched
            for (i in 0..min(substituted, untouched)) {
                possibilities += nCk(total, i)
            }
        }

        // Floor of one bit, so a single substitution such as `4pple` is not free.
        return max(1.0, log2(possibilities.toDouble()))
    }

    /** One extra bit: an attacker checking reversed words doubles their key space. */
    private fun reversedEntropy(): Double = if (reversed) 1.0 else 0.0
}
