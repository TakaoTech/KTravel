package com.takaotech.gunzou.here.common

/**
 * Where the HERE APIs live, and how they expect to be authenticated.
 */
object HereEndpointUrls {
    /** Base URL of the Routing API */
    const val ROUTING = "https://router.hereapi.com/v8/"

    /** Base URL of the Public Transit API */
    const val PUBLIC_TRANSIT = "https://transit.hereapi.com/v8/"

    /** Base URL of the Browse endpoint of the Geocoding and Search API */
    const val BROWSE = "https://browse.search.hereapi.com/v1/"

    /** Base URL of the Autocomplete endpoint of the Geocoding and Search API */
    const val AUTOCOMPLETE = "https://autocomplete.search.hereapi.com/v1/"

    /** Base URL of the Autosuggest endpoint of the Geocoding and Search API */
    const val AUTOSUGGEST = "https://autosuggest.search.hereapi.com/v1/"

    /** Base URL of the Reverse Geocode endpoint of the Geocoding and Search API */
    const val REVGEOCODE = "https://revgeocode.search.hereapi.com/v1/"

    /** Query parameter carrying the API key. */
    const val API_KEY_PARAM = "apiKey"
}
