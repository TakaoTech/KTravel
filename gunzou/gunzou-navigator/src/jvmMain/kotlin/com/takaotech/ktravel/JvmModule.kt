package com.takaotech.ktravel

import io.ktor.server.application.Application
import io.ktor.server.config.ApplicationConfig
import io.ktor.server.config.tryGetString

private const val DEFAULT_RATE_LIMIT_REQUESTS = 60
private const val DEFAULT_RATE_LIMIT_REFILL_SECONDS = 60L

/**
 * The single module `application.conf` refers to by name. Reflection based module loading only works
 * on the JVM, so Android and iOS call [startServer], which installs [module] directly.
 */
fun Application.jvmModule() {
    module(environment.config.toNavigatorServerConfig())
    configureHttpJvm()
    configureMonitoring()
}

/**
 * Reads what this deployment requires of its callers.
 *
 * Everything comes from the environment rather than from a file in the image, so the same artefact
 * runs in every environment and no secret is ever built into one. `application.conf` maps the
 * variables onto these keys; an unset variable leaves the key absent, and absent means "not
 * required" throughout.
 *
 * That default matters: a server started with nothing set is open, and open is right for exactly one
 * deployment — the one inside the app, on a loopback socket. Anything reachable from outside the
 * machine has to be given tokens, and the startup log says so when it has none.
 */
internal fun ApplicationConfig.toNavigatorServerConfig(): NavigatorServerConfig {
    val tokens = tryGetString("navigator.accessTokens")
        ?.split(',')
        ?.map { it.trim() }
        ?.filter { it.isNotEmpty() }
        ?.toSet()
        .orEmpty()

    val config = NavigatorServerConfig(
        accessTokens = tokens,
        providerApiKey = tryGetString("navigator.providerApiKey")?.takeIf { it.isNotBlank() },
        rateLimit = readRateLimit(),
        version = tryGetString("navigator.version")?.takeIf { it.isNotBlank() } ?: NAVIGATOR_VERSION,
    )

    appLog.i {
        "gunzo-navigator ${config.version}: " +
            (if (config.requiresAccessToken) "${tokens.size} access token(s)" else "OPEN TO ANY CALLER") +
            ", provider key " + (if (config.providerApiKey != null) "configured" else "expected from the caller") +
            ", " + (config.rateLimit?.let { "${it.requests} requests / ${it.refillPeriodSeconds}s" } ?: "no rate limit")
    }

    return config
}

/**
 * The limit, or none when it is switched off explicitly.
 *
 * A limit applies by default. A deployment that wants none has to say `navigator.rateLimit.enabled =
 * false`, because the version of this that is easy to get wrong is the one where forgetting a
 * variable leaves a public endpoint unmetered.
 */
private fun ApplicationConfig.readRateLimit(): NavigatorRateLimit? {
    if (tryGetString("navigator.rateLimit.enabled")?.lowercase() == "false") return null

    return NavigatorRateLimit(
        requests = tryGetString("navigator.rateLimit.requests")?.toIntOrNull() ?: DEFAULT_RATE_LIMIT_REQUESTS,
        refillPeriodSeconds = tryGetString("navigator.rateLimit.refillSeconds")?.toLongOrNull()
            ?: DEFAULT_RATE_LIMIT_REFILL_SECONDS,
    )
}
