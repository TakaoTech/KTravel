package com.takaotech.navigator.client

import co.touchlab.kermit.Logger

/**
 * Where the navigator is, resolved per request rather than once.
 *
 * It has to be a function and not a string. An embedded server binds whatever port the operating
 * system hands it, and it binds a different one every time it is restarted — which on iOS is every
 * time the app comes back to the foreground, because the socket does not survive suspension. A
 * client holding a base URL captured at construction would keep calling a port nobody is listening
 * on, and the only symptom would be a connection refused the user cannot explain.
 *
 * Resolving per request also makes the remote and embedded cases the same code path, which is what
 * lets one be swapped for the other at runtime.
 */
fun interface NavigatorBaseUrl {
    /**
     * The origin to send the next request to, such as `http://127.0.0.1:54213`, with no trailing
     * slash. Suspending because an embedded host may have to start the server first.
     */
    suspend fun resolve(): String
}

// AUTH DISABLED: the navigator authenticates nobody, so there is no token to say who is calling.
// Uncomment this interface together with every other `AUTH DISABLED` marker to send bearer tokens
// again.
// /**
//  * The token that says who is calling, resolved per request for the same reason as
//  * [NavigatorBaseUrl].
//  *
//  * Null for a navigator that accepts anyone, which is what an embedded server does: its only
//  * caller is the process it runs in, and there is nobody else to tell apart.
//  */
// fun interface NavigatorAccessToken {
//     /** The token to present, or null to present none. */
//     suspend fun resolve(): String?
// }

/**
 * One navigator, named for a single call.
 *
 * The configuration answers "where do requests go" once for the lifetime of a client, which is right
 * while there is one answer. A caller that has to reach two navigators in the same breath — asking an
 * embedded server and a remote one which profiles they serve, so the user can choose between them —
 * has two, and building a second client for it would mean a second connection pool and a second set
 * of threads for a question that takes one request.
 *
 * AUTH DISABLED: a token used to travel with the address, in one object, and that was the point of
 * the type. They were not two independent overrides: a token is issued by a particular deployment,
 * so naming a different host while keeping the configured token would present a credential to a
 * server it was never meant for. A target with no token therefore meant *no token*, not *fall back
 * to the configured one*.
 *
 * @property baseUrl The origin to call, with or without a trailing slash.
 */
data class NavigatorTarget(
    val baseUrl: String,
    // AUTH DISABLED: the token this deployment accepts, or null when it accepts anyone.
    // val accessToken: String? = null,
)

/**
 * How a [NavigatorClient] behaves.
 *
 * There is no logging flag: every request and response is written through [logger] at
 * `Severity.Debug`, and what decides whether those lines exist is that logger's minimum severity —
 * one decision, taken where the logger is built, instead of a boolean each call site has to
 * remember. The bodies carry the caller's provider key, so a build that logs at debug is a build
 * that logs the key.
 *
 * @property baseUrl Where to send requests.
 * @property logger Where HTTP traffic is written. Null falls back to the Kermit singleton, which is
 *   what a caller with no logger of its own — a test, a script — ends up on.
 * @property requestTimeoutMillis How long to wait for a whole call. A routing request goes on to a
 *   third party API, so it is well above what a local service would need.
 */
class NavigatorClientConfig(
    val baseUrl: NavigatorBaseUrl,
    // AUTH DISABLED: who to say is calling. Resolved per request, so a token edited in settings took
    // effect on the next call rather than on the next launch.
    // val accessToken: NavigatorAccessToken = NavigatorAccessToken { null },
    val logger: Logger? = null,
    val requestTimeoutMillis: Long = DEFAULT_REQUEST_TIMEOUT_MILLIS,
) {
    /**
     * A navigator that is always at the same place, which is the remote deployment.
     */
    constructor(
        baseUrl: String,
        // AUTH DISABLED: accessToken: String? = null,
        logger: Logger? = null,
        requestTimeoutMillis: Long = DEFAULT_REQUEST_TIMEOUT_MILLIS,
    ) : this(
        baseUrl = NavigatorBaseUrl { baseUrl.trimEnd('/') },
        // AUTH DISABLED: accessToken = NavigatorAccessToken { accessToken },
        logger = logger,
        requestTimeoutMillis = requestTimeoutMillis,
    )

    /** Defaults shared by both ways of building a configuration. */
    companion object {
        /** Thirty seconds: enough for a HERE call with several alternatives on a slow connection. */
        const val DEFAULT_REQUEST_TIMEOUT_MILLIS: Long = 30_000
    }
}
