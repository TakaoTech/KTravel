package com.takaotech.ktravel.presentation.settings

import com.takaotech.gunzou.client.NavigatorClient
import com.takaotech.gunzou.client.NavigatorResult
import com.takaotech.gunzou.client.NavigatorTarget

/**
 * Whether a remote navigator answers, as a settings screen shows it.
 *
 * Only reachability, and that is all there is to know: the app presents no credential of its own to
 * a navigator. The one key that travels is the trip's provider key, which the routing call carries
 * and this probe has no business holding.
 */
sealed interface NavigatorReachability {

    /** Nothing has been checked yet. */
    data object Unknown : NavigatorReachability

    /** A check is running. */
    data object Checking : NavigatorReachability

    /** It answered, in [latencyMillis], reporting [version]. */
    data class Reachable(val version: String, val latencyMillis: Long) : NavigatorReachability

    /** It did not answer. */
    data object Unreachable : NavigatorReachability

    /** There is no address to check. */
    data object NotConfigured : NavigatorReachability
}

/** Asks the navigator at [baseUrl] whether it is there. */
suspend fun NavigatorClient.checkReachability(baseUrl: String): NavigatorReachability {
    val trimmed = baseUrl.trim()
    if (trimmed.isBlank()) return NavigatorReachability.NotConfigured

    val started = kotlin.time.TimeSource.Monotonic.markNow()
    val response = health(NavigatorTarget(baseUrl = trimmed))
    val elapsed = started.elapsedNow().inWholeMilliseconds

    return when (response) {
        is NavigatorResult.Success -> NavigatorReachability.Reachable(
            version = response.value.version.ifBlank { "unknown" },
            latencyMillis = elapsed,
        )

        // A navigator that answers with an error is still a navigator that is there; anything that
        // could not be reached at all is not.
        is NavigatorResult.ServerError -> NavigatorReachability.Reachable(
            version = "unknown",
            latencyMillis = elapsed,
        )

        is NavigatorResult.TransportError -> NavigatorReachability.Unreachable
    }
}
