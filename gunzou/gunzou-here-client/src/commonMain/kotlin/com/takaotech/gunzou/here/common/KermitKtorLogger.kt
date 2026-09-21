package com.takaotech.gunzou.here.common

import co.touchlab.kermit.Logger
import co.touchlab.kermit.Severity
import io.ktor.client.plugins.logging.Logger as KtorLogger

/** The tag HTTP traffic of this client carries, whoever's logger it is written through. */
private const val HERE_CLIENT_TAG = "here-client"

/** What a redacted key is written as, short enough to keep the line readable. */
private const val REDACTED = "***"

/**
 * The key as it appears in a logged URL, whatever its value: `apiKey=...` up to the next parameter.
 *
 * Matching the parameter rather than the value is what makes the redaction hold when the key is not
 * the one this logger was built with — a pooled client answering for another caller, a URL echoed
 * back inside an error payload.
 */
private val API_KEY_PARAM = Regex("${Regex.escape(HereEndpointUrls.API_KEY_PARAM)}=[^&\\s\"]*")

/**
 * Sends what Ktor's `Logging` plugin writes through Kermit, with the API key taken out of it.
 *
 * Ktor's own default logger is a different channel on every target — SLF4J on the JVM, a `println`
 * elsewhere — and none of it obeys the configuration the caller set up. Going through the caller's
 * [Logger] means its writer decides where a line ends up, and its minimum severity decides whether
 * there is a line at all: every dump is written at [severity], so a caller that stays at
 * `Severity.Info` has HTTP logging off without a flag saying so anywhere.
 *
 * What severity cannot be asked to do is keep a secret. HERE is authenticated by a query parameter,
 * so `LogLevel.ALL` puts the key in the logged URL, and this application shows that log on a
 * diagnostics screen and attaches it to an issue report. Both shapes are therefore removed here,
 * before the line reaches any writer: the `apiKey` parameter whatever its value, and the configured
 * key wherever else it turns up. Doing it in the adapter rather than in a release only switch means
 * there is no build in which the key is written.
 *
 * The equivalent adapter published as `co.touchlab:kermit-ktor` is not used because it has no JVM
 * variant, and this module builds for the desktop application and for the server.
 *
 * @param logger The caller's logger, re-tagged so its HTTP lines are recognisable. Null falls back
 *   to the Kermit singleton.
 * @param severity The severity every HTTP line is written at.
 * @param apiKey The key to redact wherever it appears. Blank when the caller has none to hide.
 */
internal class KermitKtorLogger(
    logger: Logger? = null,
    private val severity: Severity = Severity.Debug,
    private val apiKey: String = "",
) : KtorLogger {

    private val logger: Logger = (logger ?: Logger).withTag(HERE_CLIENT_TAG)

    override fun log(message: String) {
        logger.log(severity, logger.tag, null, message.redacted())
    }

    /** [message] with every trace of the key replaced by [REDACTED]. */
    private fun String.redacted(): String {
        val withoutParam = API_KEY_PARAM.replace(this, "${HereEndpointUrls.API_KEY_PARAM}=$REDACTED")

        return if (apiKey.isBlank()) withoutParam else withoutParam.replace(apiKey, REDACTED)
    }
}
