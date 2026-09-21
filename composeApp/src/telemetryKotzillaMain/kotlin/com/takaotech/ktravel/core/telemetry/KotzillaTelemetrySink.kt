package com.takaotech.ktravel.core.telemetry

import com.takaotech.ktravel.core.KTravelBuildInfo
import com.takaotech.ktravel.core.logging.LogLevel
import com.takaotech.ktravel.core.telemetry.KotzillaTelemetrySink.start
import io.kotzilla.generated.monitoring
import io.kotzilla.sdk.KotzillaConsent
import io.kotzilla.sdk.KotzillaCore
import io.kotzilla.sdk.KotzillaCoreSDK
import kotlin.concurrent.Volatile

/**
 * The telemetry backend of a build configured with a `kotzilla.json`.
 *
 * This source directory is compiled only when the project file is present, which is what keeps every
 * Kotzilla symbol off the classpath of a clone without one. See the source set switch in
 * `composeApp/build.gradle.kts`.
 *
 * Nothing here decides *whether* to send: that is `TelemetryLogWriter`, which consults the user's
 * consent on every line. The SDK is configured with `consentRequired = true` as well, so a line that
 * somehow arrived early would still be held rather than sent.
 */
internal fun createTelemetrySink(): TelemetrySink = KotzillaTelemetrySink

private object KotzillaTelemetrySink : TelemetrySink {

    /**
     * The instance the generated `monitoring()` returns, or null before [start].
     *
     * Held rather than looked up: the SDK's own way of finding the current instance is marked
     * internal, and a line logged before telemetry has started must be dropped rather than be the
     * reason the process crashes.
     */
    @Volatile
    private var core: KotzillaCore? = null

    override fun start() {
        core = monitoring(KTravelBuildInfo.VERSION)
    }

    override fun applyConsent(consent: TelemetryConsent) {
        // Goes through the companion, which buffers the decision when the SDK has not booted yet.
        KotzillaCoreSDK.setConsent(
            when (consent) {
                TelemetryConsent.Granted -> KotzillaConsent.GRANTED
                TelemetryConsent.Denied, TelemetryConsent.Unknown -> KotzillaConsent.NOT_GRANTED
            },
        )
    }

    override fun identify(installationId: String) {
        core?.setUserId(installationId)
    }

    override fun record(level: LogLevel, tag: String, message: String, throwable: Throwable?) {
        val instance = core ?: return
        val line = "[$tag] $message"

        if (throwable != null) {
            instance.logError(line, throwable)
        } else {
            instance.log(line)
        }
    }

    override fun forgetMe() = KotzillaCoreSDK.forgetMe()
}
