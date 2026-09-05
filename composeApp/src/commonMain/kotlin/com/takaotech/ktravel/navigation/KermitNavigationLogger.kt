package com.takaotech.ktravel.navigation

import co.touchlab.kermit.Logger
import com.slack.circuitx.navigation.intercepting.NavigationLogger

/**
 * Sends CircuitX's navigation trace to the application logger.
 *
 * CircuitX asks for a sink of plain strings rather than for a logger of its own, so this is the
 * whole adapter: what it records is every movement of the back stack, which is the trace worth
 * having when a screen turns out to have been reached from somewhere unexpected.
 *
 * @param logger The application logger, tagged so navigation is greppable on its own.
 */
internal class KermitNavigationLogger(logger: Logger) : NavigationLogger {

    private val logger = logger.withTag("Navigation")

    override fun log(message: String) = logger.d { message }
}
