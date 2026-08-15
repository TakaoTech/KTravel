package com.takaotech.ktravel

import io.ktor.server.application.Application
import io.ktor.server.application.install
// AUTH DISABLED: authentication is switched off; uncomment this import together with every other
// `AUTH DISABLED` marker to bring the bearer provider back.
// import io.ktor.server.auth.Authentication
// import io.ktor.server.auth.bearer
import io.ktor.server.plugins.ratelimit.RateLimit
import io.ktor.server.plugins.ratelimit.RateLimitName
// AUTH DISABLED: only the commented out rate limit key reads the request headers.
// import io.ktor.server.request.ApplicationRequest
// import io.ktor.server.request.header
import kotlin.time.Duration.Companion.seconds

// AUTH DISABLED: the provider the routing paths used to be mounted under.
// /** The authentication provider the routing paths are mounted under. */
// const val NAVIGATOR_AUTH: String = "navigator-access-token"

/** The rate limiter the routing paths are mounted under. */
val NAVIGATOR_ROUTING_LIMIT: RateLimitName = RateLimitName("navigator-routing")

// AUTH DISABLED: nothing produces a principal any more, so nothing reads one either.
// /**
//  * Who a caller turned out to be.
//  *
//  * @property isKnown Whether the token they presented is one this server accepts.
//  */
// data class NavigatorCaller(val isKnown: Boolean)

/**
 * How much any one caller may ask for.
 *
 * Authentication is switched off: this server answers anyone, and the only credential left in the
 * contract is the routing provider key, which [configureRouting] resolves per request. The bearer
 * provider is kept below as commented out code rather than deleted, so switching it back on is
 * uncommenting rather than rewriting.
 *
 * The rate limit is installed unconditionally and is inert when [NavigatorServerConfig] does not ask
 * for one. That is deliberate: a plugin that is only installed in one deployment is a plugin whose
 * interaction with everything else is only ever exercised in one deployment.
 */
fun Application.configureSecurity(config: NavigatorServerConfig) {
    // AUTH DISABLED: the whole bearer provider. The description below is what
    // `ktor-server-routing-openapi` published as the security scheme of every authenticated
    // operation — it belongs here rather than in the OpenAPI document, because the scheme is
    // inferred from this provider.
    // install(Authentication) {
    //     bearer(
    //         name = NAVIGATOR_AUTH,
    //         description = "Who may call this server. Configured through `NAVIGATOR_ACCESS_TOKENS`; a deployment " +
    //             "with none configured accepts anyone, which is only ever right for a server on a loopback socket " +
    //             "inside the application that started it.",
    //     ) {
    //         realm = "gunzo-navigator"
    //
    //         // Always a principal, never null, and the verdict travels inside it. Returning null is
    //         // what makes the plugin issue its own challenge — an empty 401 a client would have to
    //         // special case — and it does so even on an `optional` route, which only waives the
    //         // challenge for a caller who presented nothing at all. Answering here and refusing in
    //         // the route keeps every 401 carrying the contract's error body.
    //         authenticate { credential -> NavigatorCaller(isKnown = config.accepts(credential.token)) }
    //     }
    // }

    install(RateLimit) {
        register(NAVIGATOR_ROUTING_LIMIT) {
            val limit = config.rateLimit

            rateLimiter(
                limit = limit?.requests ?: UNLIMITED_REQUESTS,
                refillPeriod = (limit?.refillPeriodSeconds ?: UNLIMITED_REFILL_SECONDS).seconds,
            )

            // Keyed by the remote host. Without a key every caller shares one bucket, and the first
            // client in a retry loop would lock out everybody else — which is the failure the limit
            // exists to prevent, arrived at from the other direction.
            //
            // The host is weaker than a token — everyone behind one NAT counts as one caller — but
            // with authentication switched off there is nothing else to count by: an `Authorization`
            // header nobody verifies identifies whoever chose to send it, which is a bucket a noisy
            // client can leave at will.
            // AUTH DISABLED: the key that preferred the caller's token.
            // requestKey { call -> call.request.rateLimitKey() }
            requestKey { call -> call.request.local.remoteHost }
        }
    }
}

// AUTH DISABLED: what a caller was counted as while tokens were verified. The token is what
// identifies a caller on a deployment that has any, and it is already a secret the server holds, so
// using it as a key added no exposure.
// private fun ApplicationRequest.rateLimitKey(): String = header("Authorization") ?: local.remoteHost

private const val UNLIMITED_REQUESTS = Int.MAX_VALUE
private const val UNLIMITED_REFILL_SECONDS = 1L
