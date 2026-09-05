package com.takaotech.gunzou.here.common

/**
 * Where the HERE APIs live, and how they expect to be authenticated.
 */
object HereEndpointUrls {
    /** Base URL of the Routing API */
    const val ROUTING = "https://router.hereapi.com/v8/"

    /** Base URL of the Public Transit API */
    const val PUBLIC_TRANSIT = "https://transit.hereapi.com/v8/"

    /** Query parameter carrying the API key. */
    const val API_KEY_PARAM = "apiKey"
}
