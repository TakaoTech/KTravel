/*
 * Ported from nbvcxz (me.gosimple.nbvcxz.matching.match.Match and BaseMatch).
 * Copyright (c) 2017, Adam Brusselback. MIT licence — see LICENSE-nbvcxz.txt.
 */
package com.takaotech.password.matching.match

import kotlin.math.ln
import kotlin.math.max

/**
 * A portion of the password recognised by a matcher, together with the entropy it contributes.
 *
 * The upstream `getDetails()` is not ported: it built a human-readable dump out of a `ResourceBundle`
 * for the command line tool, which this module has no equivalent of.
 */
internal interface Match {
    val token: String
    val startIndex: Int
    val endIndex: Int

    val length: Int get() = token.length

    /** Entropy of this match in bits, never negative. */
    fun calculateEntropy(): Double

    fun averageEntropy(): Double = calculateEntropy() / length
}

/** Shared plumbing for [Match] implementations. */
internal abstract class BaseMatch(
    final override val token: String,
    final override val startIndex: Int,
    final override val endIndex: Int,
) : Match {

    init {
        require(token.isNotEmpty()) { "Empty token" }
    }

    private var entropy: Double = 0.0

    protected fun setEntropy(value: Double) {
        entropy = value
    }

    final override fun calculateEntropy(): Double = max(0.0, entropy)

    internal companion object {
        val LOG_10: Double = log2(10.0)
        val LOG_26: Double = log2(26.0)
        val LOG_129: Double = log2(129.0)
        val LOG_37200: Double = log2(37200.0)
        val LOG_47988: Double = log2(47988.0)

        private const val LOG_2: Double = 0.6931471805599453

        fun log2(value: Double): Double = ln(value) / LOG_2

        /**
         * Binomial coefficient, "choose k among n".
         *
         * Kept as the upstream interleaved multiply/divide rather than a factorial: it is what keeps
         * the running value small enough not to overflow for the sizes reached here.
         */
        fun nCk(n: Int, k: Int): Long {
            if (k > n) return 0
            var result = 1L
            var remaining = n
            for (i in 1..k) {
                result *= remaining--
                result /= i
            }
            return result
        }
    }
}
