package com.takaotech.navigation.common

import co.touchlab.kermit.Logger

/**
 * Configuration shared by every HERE API served by [com.takaotech.navigation.HereClient].
 *
 * There is no logging flag. Requests and responses are always written, at `Severity.Debug`, through
 * [logger]: what decides whether they exist is the minimum severity of the logger the caller passed,
 * and a caller that builds its logger through a dependency graph decides it in one place for the
 * whole application. Worth knowing what that decision means here: the API key travels in the URL as
 * a query parameter, so a logger that keeps debug lines is a logger that keeps the key.
 *
 * @property apiKey HERE API key, sent as a query parameter on every request
 * @property logger Where HTTP traffic is written. Null falls back to the Kermit singleton, which is
 *   what a caller with no logger of its own — a test, a script — ends up on.
 * @property routingBaseUrl Base URL of the Routing API, overridable for tests
 * @property publicTransitBaseUrl Base URL of the Public Transit API, overridable for tests
 */
data class HereClientConfig(
    val apiKey: String,
    val logger: Logger? = null,
    val routingBaseUrl: String = HereEndpointUrls.ROUTING,
    val publicTransitBaseUrl: String = HereEndpointUrls.PUBLIC_TRANSIT,
)
