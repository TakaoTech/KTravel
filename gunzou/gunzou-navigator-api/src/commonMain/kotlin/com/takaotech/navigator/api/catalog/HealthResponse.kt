package com.takaotech.navigator.api.catalog

import kotlinx.serialization.Serializable

/**
 * Body of `GET /v1/health`.
 *
 * Liveness only: it says the server is up and answering, not that any provider behind it is
 * reachable. Checking the providers would turn a health probe into a call to a paid third party on
 * every poll, and an embedded host polls it every time it comes back to the foreground.
 *
 * @property status `ok` while the server is serving.
 * @property version The build the server is running, so a bug report names the right one.
 */
@Serializable
data class HealthResponse(val status: String = STATUS_OK, val version: String) {
    /** The only status this endpoint ever reports; anything else is a failure to answer at all. */
    companion object {
        /** Value of [status] while the server is serving. */
        const val STATUS_OK: String = "ok"
    }
}
