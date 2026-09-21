package com.takaotech.ktravel.core.logging.slf4j

import co.touchlab.kermit.Logger
import com.takaotech.ktravel.core.logging.slf4j.KermitSlf4jBridge.install
import kotlin.concurrent.Volatile

/**
 * Where the SLF4J lines of this application go.
 *
 * Ktor, Couchbase and the coroutines integration all log through SLF4J. In the desktop and Android
 * builds this application is the only SLF4J backend on the classpath, so those lines arrive here and
 * from here they reach whatever the application's Kermit logger is configured with: the console, the
 * diagnostics buffer, the log file, and telemetry once the user has consented.
 *
 * The standalone `:gunzou-server-app` is a different process with a different classpath: there
 * logback is the backend and this object does not exist.
 */
object KermitSlf4jBridge {

    /**
     * The logger the lines are written to.
     *
     * Starts as the Kermit singleton because SLF4J can be asked for a logger before the dependency
     * graph is built — a library logging during class initialisation — and those lines should not be
     * lost. [install] replaces it with the application's own as soon as there is one.
     */
    @Volatile
    var target: Logger = Logger
        private set

    /**
     * Points the binding at the application's logger.
     *
     * @param logger The one logger of the process, built by `createAppLogger`.
     */
    fun install(logger: Logger) {
        target = logger
    }

    /**
     * True while this thread is already inside the binding.
     *
     * The guard against the one cycle this direction can create: a Kermit writer that forwards to
     * SLF4J — `:gunzou-server`'s `Slf4jLogWriter`, which `installLogging()` can put on the *global*
     * Kermit logger — would send the line straight back here, and the second lap would never end.
     * The application configures its own logger with no such writer, so this never fires in a normal
     * run; it fires in a test that let the server configure logging for itself, and it is the
     * difference between a dropped line and a StackOverflowError.
     */
    private val insideBridge = ThreadLocal.withInitial { false }

    /**
     * Runs [write] unless this thread is already writing a line through the bridge.
     *
     * @param write What to write, skipped entirely when it would re-enter.
     */
    internal inline fun guarded(write: () -> Unit) {
        if (insideBridge.get()) return

        insideBridge.set(true)
        try {
            write()
        } finally {
            insideBridge.set(false)
        }
    }
}
