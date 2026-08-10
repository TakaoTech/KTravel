/*
 * Ported from nbvcxz (BruteForceMatch, SeparatorMatch, YearMatch, RepeatMatch, SequenceMatch).
 * Copyright (c) 2017, Adam Brusselback. MIT licence — see LICENSE-nbvcxz.txt.
 */
package com.takaotech.password.matching.match

import com.takaotech.password.resources.BruteForceUtil
import kotlin.math.max

/** A single character nothing else explained, priced at the size of its own alphabet. */
internal class BruteForceMatch(character: Char, index: Int) : BaseMatch(character.toString(), index, index) {

    init {
        val cardinality = BruteForceUtil.bruteForceCardinality(character)
        setEntropy(max(0.0, log2(cardinality.toDouble() * token.length)))
    }
}

/** A run of the same separator character, e.g. the dashes in `a-b-c`. */
internal class SeparatorMatch(match: String, startIndex: Int, endIndex: Int) :
    BaseMatch(match, startIndex, endIndex) {

    init {
        setEntropy(LOG_10)
    }
}

/** A plausible year, priced as one of the ~129 years people actually use. */
internal class YearMatch(match: String, startIndex: Int, endIndex: Int) : BaseMatch(match, startIndex, endIndex) {

    init {
        setEntropy(LOG_129)
    }
}

/** A repeated block, e.g. `abcabcabc` — [repeatingCharacters] repeated [repeat] times. */
internal class RepeatMatch(match: String, val repeatingCharacters: String, startIndex: Int, endIndex: Int) :
    BaseMatch(match, startIndex, endIndex) {

    val repeat: Int = match.length / repeatingCharacters.length

    init {
        val cardinality = BruteForceUtil.bruteForceCardinality(repeatingCharacters)
        val guesses = if (repeat != repeatingCharacters.length) {
            cardinality.toDouble() * repeat * repeatingCharacters.length
        } else {
            cardinality.toDouble() * repeat
        }
        setEntropy(max(0.0, log2(guesses)))
    }
}

/** A run along the alphabet or the digits, e.g. `abcdef` or `4567`. */
internal class SequenceMatch(match: String, startIndex: Int, endIndex: Int) :
    BaseMatch(match, startIndex, endIndex) {

    val firstCharacter: Char = match[0]

    init {
        val baseEntropy = when {
            // Sequences starting at the very beginning of their alphabet are the first thing tried.
            firstCharacter == 'a' || firstCharacter == '1' -> 1.0

            firstCharacter.isDigit() -> LOG_10

            firstCharacter.isLowerCase() -> LOG_26

            // One extra bit for having bothered with upper case.
            else -> LOG_26 + 1.0
        }
        setEntropy(baseEntropy + log2(length.toDouble()))
    }
}
