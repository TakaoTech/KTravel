package com.takaotech.ktravel

import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.auth.Authentication
import io.ktor.server.auth.bearer
import io.ktor.server.plugins.ratelimit.RateLimit
import io.ktor.server.plugins.ratelimit.RateLimitName
import io.ktor.server.request.ApplicationRequest
import io.ktor.server.request.header
import kotlin.time.Duration.Companion.seconds

/** The authentication provider the routing paths are mounted under. */
const val NAVIGATOR_AUTH: String = "navigator-access-token"

/** The rate limiter the routing paths are mounted under. */
val NAVIGATOR_ROUTING_LIMIT: RateLimitName = RateLimitName("navigator-routing")

/**
 * Who a caller turned out to be.
 *
 * @property isKnown Whether the token they presented is one this server accepts.
 */
data class NavigatorCaller(val isKnown: Boolean)

/**
 * Who may call this server, and how much.
 *
 * Both are installed unconditionally and both are inert when [NavigatorServerConfig] does not ask
 * for them. That is deliberate: a plugin that is only installed in one deployment is a plugin whose
 * interaction with everything else is only ever exercised in one deployment.
 */
fun Application.configureSecurity(config: NavigatorServerConfig) {
    install(Authentication) {
        // The description is what `ktor-server-routing-openapi` publishes as the security scheme of
        // every authenticated operation. It belongs here rather than in the OpenAPI document: the
        // scheme is inferred from this provider, so describing it anywhere else would be describing
        // a copy that nothing keeps in step with the provider it claims to document.
        bearer(
            name = NAVIGATOR_AUTH,
            description = "Who may call this server. Configured through `NAVIGATOR_ACCESS_TOKENS`; a deployment " +
                "with none configured accepts anyone, which is only ever right for a server on a loopback socket " +
                "inside the application that started it.",
        ) {
            realm = "gunzo-navigator"

            // Always a principal, never null, and the verdict travels inside it. Returning null is
            // what makes the plugin issue its own challenge — an empty 401 a client would have to
            // special case — and it does so even on an `optional` route, which only waives the
            // challenge for a caller who presented nothing at all. Answering here and refusing in
            // the route keeps every 401 carrying the contract's error body.
            authenticate { credential -> NavigatorCaller(isKnown = config.accepts(credential.token)) }
        }
    }

    install(RateLimit) {
        register(NAVIGATOR_ROUTING_LIMIT) {
            val limit = config.rateLimit

            rateLimiter(
                limit = limit?.requests ?: UNLIMITED_REQUESTS,
                refillPeriod = (limit?.refillPeriodSeconds ?: UNLIMITED_REFILL_SECONDS).seconds,
            )

            // Keyed by the caller's token where there is one, and by the remote host otherwise.
            // Without a key every caller shares one bucket, and the first client in a retry loop
            // would lock out everybody else — which is the failure the limit exists to prevent,
            // arrived at from the other direction.
            requestKey { call -> call.request.rateLimitKey() }
        }
    }
}

/**
 * What a caller is counted as.
 *
 * The token is what identifies a caller on a deployment that has any, and it is already a secret the
 * server holds, so using it as a key adds no exposure. Falling back to the remote host is weaker —
 * everyone behind one NAT counts as one caller — but it is the only thing left to count by, and an
 * anonymous deployment is one where that trade is acceptable by definition.
 */
private fun ApplicationRequest.rateLimitKey(): String = header("Authorization") ?: local.remoteHost

private const val UNLIMITED_REQUESTS = Int.MAX_VALUE
private const val UNLIMITED_REFILL_SECONDS = 1L
