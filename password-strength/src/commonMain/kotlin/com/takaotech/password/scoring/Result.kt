/*
 * Ported from nbvcxz (me.gosimple.nbvcxz.scoring.Result and resources.FeedbackUtil).
 * Copyright (c) 2017, Adam Brusselback. MIT licence — see LICENSE-nbvcxz.txt.
 */
package com.takaotech.password.scoring

import com.takaotech.password.PasswordStrength
import com.takaotech.password.PasswordSuggestion
import com.takaotech.password.PasswordWarning
import com.takaotech.password.matching.match.DateMatch
import com.takaotech.password.matching.match.DictionaryMatch
import com.takaotech.password.matching.match.Match
import com.takaotech.password.matching.match.RepeatMatch
import com.takaotech.password.matching.match.SequenceMatch
import com.takaotech.password.matching.match.SpacialMatch
import com.takaotech.password.matching.match.YearMatch
import com.takaotech.password.resources.CharacterCaseUtil
import com.takaotech.password.resources.Configuration
import kotlin.math.pow

/** The outcome of an estimation: the matches the password decomposes into, and what they cost. */
internal class Result(private val configuration: Configuration, val password: String, val matches: List<Match>) {

    init {
        check(password == matches.joinToString("") { it.token }) {
            "The matches put together do not equal the original password."
        }
    }

    val entropy: Double get() = matches.sumOf { it.calculateEntropy() }

    /** Number of guesses an attacker would need, saturated at [Double.MAX_VALUE]. */
    val guesses: Double
        get() = 2.0.pow(entropy).let { if (it.isInfinite()) Double.MAX_VALUE else it }

    val isMinimumEntropyMet: Boolean get() = entropy >= configuration.minimumEntropy

    /** 0 to 4, using the same thresholds as zxcvbn. */
    val basicScore: Int
        get() = when {
            guesses < SCORE_1_THRESHOLD -> 0
            guesses < SCORE_2_THRESHOLD -> 1
            guesses < SCORE_3_THRESHOLD -> 2
            guesses < SCORE_4_THRESHOLD -> 3
            else -> 4
        }

    fun toPasswordStrength(): PasswordStrength {
        val feedback = feedback()
        return PasswordStrength(
            score = basicScore,
            entropy = entropy,
            warning = feedback.warning,
            suggestions = feedback.suggestions,
        )
    }

    /**
     * Feedback is driven by the longest match, which is the part of the password doing the most
     * damage. Upstream returns resource-bundle keys here; this port returns the enums instead.
     */
    private fun feedback(): Feedback {
        if (isMinimumEntropyMet) return Feedback(null, emptyList())
        if (password.isEmpty()) return DEFAULT_FEEDBACK

        val longest = matches.maxByOrNull { it.length } ?: return DEFAULT_FEEDBACK
        return matchFeedback(longest)
    }

    @Suppress("ReturnCount")
    private fun matchFeedback(match: Match): Feedback = when (match) {
        is DateMatch -> Feedback(
            PasswordWarning.DATES,
            listOf(PasswordSuggestion.AVOID_DATES, PasswordSuggestion.ADD_ANOTHER_WORD),
        )

        is YearMatch -> Feedback(
            PasswordWarning.RECENT_YEARS,
            listOf(PasswordSuggestion.AVOID_YEARS, PasswordSuggestion.ADD_ANOTHER_WORD),
        )

        is RepeatMatch -> Feedback(
            if (match.repeatingCharacters.length == 1) {
                PasswordWarning.REPEATED_CHARACTER
            } else {
                PasswordWarning.REPEATED_PATTERN
            },
            listOf(PasswordSuggestion.AVOID_REPEATED_WORDS, PasswordSuggestion.ADD_ANOTHER_WORD),
        )

        is SequenceMatch -> Feedback(
            PasswordWarning.SEQUENCE,
            listOf(PasswordSuggestion.AVOID_SEQUENCES, PasswordSuggestion.ADD_ANOTHER_WORD),
        )

        is SpacialMatch -> Feedback(
            if (match.turns > 0) {
                PasswordWarning.SHORT_KEYBOARD_PATTERN
            } else {
                PasswordWarning.STRAIGHT_ROW_OF_KEYS
            },
            listOf(
                PasswordSuggestion.USE_LONGER_KEYBOARD_PATTERN,
                PasswordSuggestion.ADD_ANOTHER_WORD,
            ),
        )

        is DictionaryMatch -> dictionaryFeedback(match)

        else -> DEFAULT_FEEDBACK
    }

    private fun dictionaryFeedback(match: DictionaryMatch): Feedback {
        val fromExclusionDictionary = configuration.dictionaries
            .any { it.dictionaryName == match.dictionaryName && it.exclusion }
        if (fromExclusionDictionary) {
            return Feedback(
                PasswordWarning.NOT_ALLOWED,
                listOf(PasswordSuggestion.PASSWORD_NOT_ALLOWED),
            )
        }

        val warning = when {
            match.rank <= TOP_10 -> PasswordWarning.TOP_10_PASSWORD
            match.rank <= TOP_100 -> PasswordWarning.TOP_100_PASSWORD
            else -> PasswordWarning.VERY_COMMON_PASSWORD
        }

        val suggestions = mutableListOf(PasswordSuggestion.ADD_ANOTHER_WORD)
        if (match.reversed) suggestions += PasswordSuggestion.AVOID_REVERSED_WORDS
        if (match.isLeet) suggestions += PasswordSuggestion.AVOID_PREDICTABLE_LETTER_SUBSTITUTIONS

        val capitalisation = CharacterCaseUtil.fractionOfStringUppercase(password)
        when {
            capitalisation > MOSTLY_UPPERCASE -> suggestions += PasswordSuggestion.AVOID_ALL_UPPERCASE

            capitalisation > 0.0 && capitalisation <= BARELY_UPPERCASE ->
                suggestions += PasswordSuggestion.CAPITALIZATION_DOES_NOT_HELP
        }

        return Feedback(warning, suggestions)
    }

    private class Feedback(val warning: PasswordWarning?, val suggestions: List<PasswordSuggestion>)

    private companion object {
        val DEFAULT_FEEDBACK = Feedback(
            null,
            listOf(PasswordSuggestion.USE_FEWER_WORDS, PasswordSuggestion.NO_NEED_FOR_SYMBOLS),
        )

        const val SCORE_1_THRESHOLD = 1e3
        const val SCORE_2_THRESHOLD = 1e6
        const val SCORE_3_THRESHOLD = 1e8
        const val SCORE_4_THRESHOLD = 1e10

        const val TOP_10 = 10
        const val TOP_100 = 100
        const val MOSTLY_UPPERCASE = 0.8
        const val BARELY_UPPERCASE = 0.2
    }
}
