package com.takaotech.navigation.common

/**
 * Configuration shared by every HERE API served by [com.takaotech.navigation.HereClient].
 *
 * @property apiKey HERE API key, sent as a query parameter on every request
 * @property enableLogging Enable HTTP request/response logging for debugging
 * @property routingBaseUrl Base URL of the Routing API, overridable for tests
 * @property publicTransitBaseUrl Base URL of the Public Transit API, overridable for tests
 */
data class HereClientConfig(
    val apiKey: String,
    val enableLogging: Boolean = false,
    val routingBaseUrl: String = HereEndpointUrls.ROUTING,
    val publicTransitBaseUrl: String = HereEndpointUrls.PUBLIC_TRANSIT,
)
