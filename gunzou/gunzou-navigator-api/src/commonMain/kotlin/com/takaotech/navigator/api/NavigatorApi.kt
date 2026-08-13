package com.takaotech.navigator.api

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

    /** Road routing through HERE. */
    const val HERE_CAR: String = "/$VERSION/here/car"

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
