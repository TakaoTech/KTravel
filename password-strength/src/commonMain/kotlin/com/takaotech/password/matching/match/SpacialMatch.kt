/*
 * Ported from nbvcxz (me.gosimple.nbvcxz.matching.match.SpacialMatch).
 * Copyright (c) 2017, Adam Brusselback. MIT licence — see LICENSE-nbvcxz.txt.
 */
package com.takaotech.password.matching.match

import com.takaotech.password.resources.AdjacencyGraph
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow

/** A walk across the keyboard, e.g. `qwerty` or `1qaz2wsx`. */
internal class SpacialMatch(
    match: String,
    startIndex: Int,
    endIndex: Int,
    val adjacencyGraph: AdjacencyGraph,
    val turns: Int,
    val shiftedCount: Int,
) : BaseMatch(match, startIndex, endIndex) {

    init {
        setEntropy(patternEntropy() + shiftEntropy())
    }

    /** Number of distinct paths of this length with at most this many turns, on this keyboard. */
    private fun patternEntropy(): Double {
        val size = adjacencyGraph.keyMap.size
        val averageDegree = adjacencyGraph.averageDegree

        var possibilities = 0L
        for (i in 2..length) {
            val possibleTurns = min(turns, i - 1)
            for (j in 1..possibleTurns) {
                possibilities += (nCk(i - 1, j - 1) * size * averageDegree.pow(j)).toLong()
            }
        }
        return max(0.0, log2(possibilities.toDouble()))
    }

    /** Extra cost for choosing which of the keys along the way were shifted. */
    private fun shiftEntropy(): Double {
        var possibilities = 0L
        if (shiftedCount > 0) {
            val unshiftedCount = length - shiftedCount
            for (i in 0..min(shiftedCount, unshiftedCount)) {
                possibilities += nCk(length, i)
            }
        }
        return max(0.0, log2(possibilities.toDouble()))
    }
}
