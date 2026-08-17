package com.takaotech.navigation.common

import co.touchlab.kermit.Logger
import co.touchlab.kermit.Severity
import io.ktor.client.plugins.logging.Logger as KtorLogger

/** The tag HTTP traffic of this client carries, whoever's logger it is written through. */
private const val HERE_CLIENT_TAG = "here-client"

/**
 * Sends what Ktor's `Logging` plugin writes through Kermit.
 *
 * Ktor's own default logger is a different channel on every target — SLF4J on the JVM, a `println`
 * elsewhere — and none of it obeys the configuration the caller set up. Going through the caller's
 * [Logger] means its writer decides where a line ends up, and its minimum severity decides whether
 * there is a line at all: every dump is written at [severity], so a caller that stays at
 * `Severity.Info` has HTTP logging off without a flag saying so anywhere.
 *
 * The equivalent adapter published as `co.touchlab:kermit-ktor` is not used because it has no JVM
 * variant, and this module builds for the desktop application and for the server.
 *
 * @param logger The caller's logger, re-tagged so its HTTP lines are recognisable. Null falls back
 *   to the Kermit singleton.
 * @param severity The severity every HTTP line is written at.
 */
internal class KermitKtorLogger(logger: Logger? = null, private val severity: Severity = Severity.Debug) : KtorLogger {

    private val logger: Logger = (logger ?: Logger).withTag(HERE_CLIENT_TAG)

    override fun log(message: String) {
        logger.log(severity, logger.tag, null, message)
    }
}
