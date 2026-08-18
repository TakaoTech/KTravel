package com.takaotech.navigator.api

import com.takaotech.navigator.api.here.HereTransportMode
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

    /** What this server can route with. */
    const val PROFILES: String = "/$VERSION/profiles"

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
