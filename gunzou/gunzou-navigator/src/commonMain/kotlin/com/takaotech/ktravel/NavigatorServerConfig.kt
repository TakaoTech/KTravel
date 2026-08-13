package com.takaotech.ktravel

/**
 * Everything that differs between running this server inside an app and running it as a service.
 *
 * One binary, two deployments, and the difference is data rather than code: nothing in the endpoints
 * asks which one it is in. That is what keeps the two honest — the embedded server is not a reduced
 * variant that skips checks, it is the same server configured to need none, and a bug in the
 * authenticated path is a bug in the path everyone runs.
 *
 * [EMBEDDED] is the default and holds nothing: no caller to authenticate, because the only caller is
 * the process it is running in, and no key of its own, because the app that started it has one.
 *
 * @property accessTokens Who may call this server. Empty means anyone, which is only ever right for
 *   a server on a loopback socket inside the app that started it.
 * @property providerApiKey The key this server uses to reach a routing provider when the caller
 *   sends none. Null means the caller must always bring their own.
 * @property rateLimit How much any one caller may ask for. Null means no limit.
 * @property version The build being served, reported by `GET /v1/health`.
 */
data class NavigatorServerConfig(
    val accessTokens: Set<String> = emptySet(),
    val providerApiKey: String? = null,
    val rateLimit: NavigatorRateLimit? = null,
    val version: String = NAVIGATOR_VERSION,
) {
    /**
     * Whether a caller has to say who they are.
     *
     * Derived rather than configured, so a deployment cannot end up demanding a token it has none of,
     * or holding tokens it never checks.
     */
    val requiresAccessToken: Boolean get() = accessTokens.isNotEmpty()

    /** Whether [token] is one this server accepts. */
    fun accepts(token: String): Boolean = token in accessTokens

    /** The configuration a server embedded in the app that started it runs under. */
    companion object {
        /** No caller to authenticate, no key of its own, no limit worth enforcing on one process. */
        val EMBEDDED: NavigatorServerConfig = NavigatorServerConfig()
    }
}

/**
 * How many requests a caller gets, and how often the allowance comes back.
 *
 * A ceiling on what one caller can cost, not a quota: the point is that a client stuck in a retry
 * loop cannot exhaust the provider quota that everyone else is sharing, and that a routing endpoint
 * on the public internet is not free to hammer.
 *
 * @property requests How many requests are allowed per [refillPeriodSeconds].
 * @property refillPeriodSeconds How long the window is.
 */
data class NavigatorRateLimit(val requests: Int, val refillPeriodSeconds: Long) {
    init {
        require(requests > 0) { "A rate limit of $requests requests would refuse everything" }
        require(refillPeriodSeconds > 0) { "A refill period of $refillPeriodSeconds seconds never refills" }
    }
}
