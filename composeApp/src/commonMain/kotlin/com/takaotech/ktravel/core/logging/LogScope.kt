package com.takaotech.ktravel.core.logging

import kotlin.concurrent.Volatile

/**
 * The trip a log line belongs to, when there is one.
 *
 * A log line does not carry the trip it was written for: the code that logs is spread across
 * repositories, clients and the embedded navigator, and passing a trip id down to all of them to
 * label a log line would be the wrong thing to change. What actually knows is navigation, which
 * opens and closes a trip's object graph as the user enters and leaves it — see
 * `navigation/interceptor/PlanningGraphInterceptor`, which is the only writer of [travelId].
 *
 * Volatile rather than locked: it is written on the main thread when navigation happens and read on
 * whatever thread logs, and a line labelled with the trip that was open a moment ago is a better
 * outcome than a lock on every log call.
 */
class LogScope {

    /** The trip currently open, or null when the user is not inside one. */
    @Volatile
    var travelId: String? = null
}
