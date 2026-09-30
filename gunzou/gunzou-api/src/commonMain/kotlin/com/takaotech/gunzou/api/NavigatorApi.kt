package com.takaotech.gunzou.api

import com.takaotech.gunzou.api.NavigatorApi.HERE_ROUTING_TEMPLATE
import com.takaotech.gunzou.api.NavigatorApi.HERE_SEARCH_PLACE_TEMPLATE
import com.takaotech.gunzou.api.NavigatorApi.PROFILES
import com.takaotech.gunzou.api.NavigatorApi.SEARCH_LANGUAGE_PARAMETER
import com.takaotech.gunzou.api.NavigatorApi.hereRouting
import com.takaotech.gunzou.api.here.HereTransportMode
import kotlinx.serialization.json.Json

/**
 * Where the endpoints live and what the server is called through.
 *
 * Server and client both read these constants, so a path can never drift between the route that
 * serves it and the call that reaches it.
 */
object NavigatorApi {
    /**
     * The version prefix of every path.
     *
     * Bumping it is how a breaking change ships: the old routes keep answering old clients while the
     * new ones exist beside them. Adding an optional field or a new endpoint is not a breaking
     * change and does not touch this.
     */
    const val VERSION: String = "v1"

    /**
     * Road routing through HERE, without the mode: the paths served under it are
     * [HERE_ROUTING_TEMPLATE], one per mode of transport.
     */
    const val HERE_ROUTING: String = "/$VERSION/here/routing"

    /** The path segment of [HERE_ROUTING_TEMPLATE] that names the mode. */
    const val HERE_ROUTING_TRANSPORT_MODE_PARAMETER: String = "transportMode"

    /**
     * Road routing through HERE, as a template.
     *
     * This is how the path is written where a mode has not been chosen yet — the routing tree that
     * serves it, the OpenAPI document that describes it, the catalog entry that advertises it. A
     * caller that has a mode builds the real path with [hereRouting].
     */
    const val HERE_ROUTING_TEMPLATE: String = "$HERE_ROUTING/{$HERE_ROUTING_TRANSPORT_MODE_PARAMETER}"

    /** Public transport routing through HERE. */
    const val HERE_TRANSIT: String = "/$VERSION/here/transit"

    /**
     * Place autocomplete through HERE, answered in the provider neutral
     * [com.takaotech.gunzou.api.search.autocomplete.AutocompleteResponse].
     *
     * Every search path follows the same `/{provider}/search/{service}` shape, so a second provider
     * of the same service is a new path taking the same body, not a new body.
     */
    const val HERE_SEARCH_AUTOCOMPLETE: String = "/$VERSION/here/search/autocomplete"

    /** The path segment of [HERE_SEARCH_PLACE_TEMPLATE] that carries the place identifier. */
    const val HERE_SEARCH_PLACE_ID_PARAMETER: String = "id"

    /**
     * The details of one place through HERE, as a template, answered in the provider neutral
     * [com.takaotech.gunzou.api.search.place.PlaceDetails].
     *
     * A `GET`, unlike the other search paths: it reads one resource by its identifier, which is the
     * `id` a search of the same provider returned. That identifier is opaque and may contain
     * characters such as `:`, so a caller writes it as an encoded path segment.
     *
     * The language to answer in is the [SEARCH_LANGUAGE_PARAMETER] query parameter, and it is
     * required.
     */
    const val HERE_SEARCH_PLACE_TEMPLATE: String =
        "/$VERSION/here/search/id/{$HERE_SEARCH_PLACE_ID_PARAMETER}"

    /**
     * The query parameter naming the language a `GET` search path answers in: an IETF BCP 47 tag,
     * such as `it-IT`.
     */
    const val SEARCH_LANGUAGE_PARAMETER: String = "language"

    /** What this server can route with. */
    const val PROFILES: String = "/$VERSION/profiles"

    /**
     * What this server can search with.
     *
     * A catalog of its own rather than more entries in [PROFILES]: a search service shares none of
     * the limits a routing profile publishes — alternatives, via waypoints, tolls — and folding the
     * two together would give every entry of one a set of fields that mean nothing for it.
     */
    const val SEARCH_PROFILES: String = "/$VERSION/search/profiles"

    /** Liveness. */
    const val HEALTH: String = "/$VERSION/health"

    /**
     * Header carrying the caller's credentials for the provider behind the requested profile.
     *
     * The key belongs to the caller, not to the server: embedded, the server runs inside the app
     * that owns it, and it stays that way when the same app talks to a remote deployment instead.
     */
    const val PROVIDER_KEY_HEADER: String = "X-Gunzo-Provider-Key"

    /**
     * Where a road route for [mode] is asked for, such as `/v1/here/routing/pedestrian`.
     *
     * The mode is in the path and not in the body, so a request cannot ask one path for another
     * vehicle, and a proxy or a log in front of this server can tell a walk from a truck without
     * reading a payload.
     */
    fun hereRouting(mode: HereTransportMode): String = "$HERE_ROUTING/${mode.pathSegment}"
}

/**
 * The one [Json] that reads and writes this contract, used by both sides.
 *
 * The settings are what make the contract survive its own evolution, and none of them is a matter
 * of taste:
 *
 * - `ignoreUnknownKeys` so a server that grows a field does not break every client older than it.
 *   Without it, deploying a new server would require shipping an app update first.
 * - `explicitNulls = false` so an absent optional is left out instead of written as `null`, which
 *   keeps a payload with many nullable fields readable and lets an older client omit what it does
 *   not know about.
 * - `encodeDefaults` so a request says what it means. A default that is not written is a default the
 *   *server's* copy of the contract gets to pick, and the two copies are not always the same build.
 */
val NavigatorJson: Json = Json {
    ignoreUnknownKeys = true
    explicitNulls = false
    encodeDefaults = true
}
