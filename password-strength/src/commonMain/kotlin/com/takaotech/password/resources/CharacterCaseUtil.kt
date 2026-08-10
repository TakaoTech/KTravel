/*
 * Ported from nbvcxz (me.gosimple.nbvcxz.resources.CharacterCaseUtil).
 * Copyright (c) 2017, Adam Brusselback. MIT licence — see LICENSE-nbvcxz.txt.
 */
package com.takaotech.password.resources

internal object CharacterCaseUtil {

    /**
     * Of the characters that have a distinct upper case form, the fraction already upper cased:
     * `0.0` when none are, `1.0` when all are.
     *
     * Characters whose upper and lower forms coincide (digits, punctuation) carry no case and are
     * left out of both counts.
     */
    fun fractionOfStringUppercase(input: String): Double {
        var uppercasable = 0.0
        var uppercase = 0.0

        input.forEach { character ->
            val upper = character.uppercaseChar()
            val lower = character.lowercaseChar()
            if (character == upper && character == lower) return@forEach

            uppercasable++
            if (character == upper) uppercase++
        }

        return if (uppercasable == 0.0) 0.0 else uppercase / uppercasable
    }
}
