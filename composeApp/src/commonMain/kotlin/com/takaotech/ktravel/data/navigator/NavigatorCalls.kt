package com.takaotech.ktravel.data.navigator

import com.takaotech.gunzou.client.NavigatorResult
import com.takaotech.gunzou.client.NavigatorTarget
import com.takaotech.ktravel.domain.navigator.NavigatorKind

/**
 * Runs a call, and gives an embedded server exactly one second chance.
 *
 * The embedded server does not survive the app being suspended: on iOS the socket is gone when
 * the app returns to the foreground, and the only symptom is a connection refused. Restarting it
 * and asking again turns that into nothing the traveller ever sees. A remote navigator gets no
 * such retry — it did not answer, and asking the same host again in the same breath is a second
 * failure rather than a recovery.
 */
// TODO Check this
internal suspend fun <T : Any> NavigatorTargetResolver.callWithRecovery(
    kind: NavigatorKind,
    call: suspend (NavigatorTarget) -> NavigatorResult<T>,
): NavigatorResult<T> {
    val first = call(resolve(kind))
    if (first !is NavigatorResult.TransportError || kind != NavigatorKind.EMBEDDED) return first

    return call(recover(kind))
}

/** A transport failure, said in a way that describes the situation rather than the exception class. */
internal fun Throwable.reachabilityMessage(): String = message?.takeIf { it.isNotBlank() } ?: "No answer"
