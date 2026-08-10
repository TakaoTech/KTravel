/*
 * Ported from nbvcxz (me.gosimple.nbvcxz.matching.PasswordMatcher).
 * Copyright (c) 2017, Adam Brusselback. MIT licence — see LICENSE-nbvcxz.txt.
 */
package com.takaotech.password.matching

import com.takaotech.password.matching.match.Match
import com.takaotech.password.resources.Configuration

/** Finds every occurrence of one kind of pattern in a password. */
internal interface PasswordMatcher {
    fun match(configuration: Configuration, password: String): List<Match>
}
