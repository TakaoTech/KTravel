package com.takaotech.gunzou.client

import co.touchlab.kermit.Logger
import co.touchlab.kermit.Severity
import io.ktor.client.plugins.logging.Logger as KtorLogger

/** The tag HTTP traffic of this client carries, whoever's logger it is written through. */
private const val NAVIGATOR_CLIENT_TAG = "navigator-client"

/**
 * Sends what Ktor's `Logging` plugin writes through Kermit.
 *
 * Same adapter, and for the same reasons, as the one in `:gunzou-here-client`: Ktor's default logger
 * is a different channel on every target and obeys none of the configuration the caller set up,
 * while the caller's minimum severity is the single switch that decides whether an HTTP line exists.
 * Copied rather than shared because the two modules have no common dependency, and the adapter
 * published as `co.touchlab:kermit-ktor` has no JVM variant.
 *
 * @param logger The caller's logger, re-tagged so its HTTP lines are recognisable. Null falls back
 *   to the Kermit singleton.
 * @param severity The severity every HTTP line is written at.
 */
internal class KermitKtorLogger(logger: Logger? = null, private val severity: Severity = Severity.Debug) : KtorLogger {

    private val logger: Logger = (logger ?: Logger).withTag(NAVIGATOR_CLIENT_TAG)

    override fun log(message: String) {
        logger.log(severity, logger.tag, null, message)
    }
}
