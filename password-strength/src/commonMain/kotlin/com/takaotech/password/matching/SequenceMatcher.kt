/*
 * Ported from nbvcxz (me.gosimple.nbvcxz.matching.SequenceMatcher).
 * Copyright (c) 2017, Adam Brusselback. MIT licence — see LICENSE-nbvcxz.txt.
 */
package com.takaotech.password.matching

import com.takaotech.password.matching.match.Match
import com.takaotech.password.matching.match.SequenceMatch
import com.takaotech.password.resources.Configuration

/**
 * Finds runs along the alphabet or the digits, forwards or backwards: `abcd`, `4321`, `aBcD`.
 *
 * Letters are compared with a 32-point offset applied both ways, which is the gap between the upper
 * and lower case blocks in ASCII — that is what lets a run survive a change of case mid-way.
 */
internal class SequenceMatcher : PasswordMatcher {

    override fun match(configuration: Configuration, password: String): List<Match> {
        val matches = mutableListOf<Match>()
        val run = StringBuilder()

        password.forEachIndexed { index, character ->
            if (index + 1 < password.length && continuesSequence(character, password[index + 1])) {
                run.append(character)
                return@forEachIndexed
            }

            if (run.isNotEmpty()) {
                run.append(character)
                // Two characters in a row are not a sequence, they are a coincidence.
                if (run.length > MIN_SEQUENCE_LENGTH) {
                    matches += SequenceMatch(run.toString(), index - run.length + 1, index)
                }
                run.setLength(0)
            }
        }

        return matches
    }

    private fun continuesSequence(current: Char, next: Char): Boolean {
        val currentCode = current.code
        val nextCode = next.code

        // Adjacent either way, with the case offset applied in whichever direction the next
        // character's block calls for: that is what carries a run across a change of case.
        return when (next) {
            in 'A'..'Z' -> nextCode.isAdjacentTo(currentCode, offset = CASE_OFFSET)
            in 'a'..'z' -> nextCode.isAdjacentTo(currentCode, offset = -CASE_OFFSET)
            in '0'..'9' -> nextCode == currentCode + 1 || nextCode == currentCode - 1
            else -> false
        }
    }

    private fun Int.isAdjacentTo(currentCode: Int, offset: Int): Boolean {
        val forward = currentCode + 1
        val backward = currentCode - 1
        return this == forward || this + offset == forward ||
            this == backward || this + offset == backward
    }

    private companion object {
        const val CASE_OFFSET = 32
        const val MIN_SEQUENCE_LENGTH = 2
    }
}
