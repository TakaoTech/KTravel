package com.takaotech.navigator.client

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

/**
 * The token that says who is calling, resolved per request for the same reason as [NavigatorBaseUrl].
 *
 * Null for a navigator that accepts anyone, which is what an embedded server does: its only caller
 * is the process it runs in, and there is nobody else to tell apart.
 */
fun interface NavigatorAccessToken {
    /** The token to present, or null to present none. */
    suspend fun resolve(): String?
}

/**
 * How a [NavigatorClient] behaves.
 *
 * @property baseUrl Where to send requests.
 * @property accessToken Who to say is calling. Resolved per request, so a token edited in settings
 *   takes effect on the next call rather than on the next launch.
 * @property enableLogging Logs every request and response. Off by default: the bodies carry the
 *   caller's provider key in a header, and a log is somewhere it should not end up by accident.
 * @property requestTimeoutMillis How long to wait for a whole call. A routing request goes on to a
 *   third party API, so it is well above what a local service would need.
 */
class NavigatorClientConfig(
    val baseUrl: NavigatorBaseUrl,
    val accessToken: NavigatorAccessToken = NavigatorAccessToken { null },
    val enableLogging: Boolean = false,
    val requestTimeoutMillis: Long = DEFAULT_REQUEST_TIMEOUT_MILLIS,
) {
    /**
     * A navigator that is always at the same place, which is the remote deployment.
     */
    constructor(
        baseUrl: String,
        accessToken: String? = null,
        enableLogging: Boolean = false,
        requestTimeoutMillis: Long = DEFAULT_REQUEST_TIMEOUT_MILLIS,
    ) : this(
        baseUrl = NavigatorBaseUrl { baseUrl.trimEnd('/') },
        accessToken = NavigatorAccessToken { accessToken },
        enableLogging = enableLogging,
        requestTimeoutMillis = requestTimeoutMillis,
    )

    /** Defaults shared by both ways of building a configuration. */
    companion object {
        /** Thirty seconds: enough for a HERE call with several alternatives on a slow connection. */
        const val DEFAULT_REQUEST_TIMEOUT_MILLIS: Long = 30_000
    }
}
