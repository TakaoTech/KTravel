package com.takaotech.ktravel.domain.repository

import com.takaotech.ktravel.core.annotation.OpenForMokkery
import com.takaotech.ktravel.core.telemetry.TelemetryConsent
import com.takaotech.ktravel.domain.model.AppSettingsDomain
import kotlinx.coroutines.flow.StateFlow

/**
 * Preferences of this installation: the one place that reads and writes them.
 *
 * Lives in `AppScope`, unlike [SettingsRepository], because there is one set of them per device and
 * they have to be readable before any trip is open — the settings screen that edits them is reached
 * from the trip list, not from inside a trip.
 */
@OpenForMokkery
interface AppSettingsRepository {

    /**
     * The current preferences, always with a value.
     *
     * A flow rather than a getter because two things read them at once: a settings screen that has to
     * redraw when they change, and the routing layer, which resolves the navigator address on every
     * request so an edit takes effect on the next route rather than on the next launch.
     */
    val settings: StateFlow<AppSettingsDomain>

    /**
     * Sets where the remote navigator is. A blank value clears it and falls back to the embedded one.
     */
    suspend fun updateNavigatorRemote(baseUrl: String)

    /**
     * Records where the user left diagnostics, and which documents they were shown when they did.
     *
     * The choice does not expire, so nothing but the versions is stored beside it: diagnostics rest
     * on legitimate interest, and an objection stands until the user withdraws it. The versions are
     * what bring the privacy page back when the policy is rewritten.
     *
     * @param consent Where they left it.
     * @param introVersion The version of the introduction they went through.
     * @param privacyVersion The version of the privacy policy they were shown.
     */
    suspend fun updateTelemetryConsent(consent: TelemetryConsent, introVersion: Int, privacyVersion: Int)

    /**
     * Sets how many days of log files are kept, within the allowed range.
     *
     * @param days What the user asked for; a value outside the range is brought back into it.
     */
    suspend fun updateLogRetentionDays(days: Int)

    /**
     * The id this installation is known by, generated and stored on first use.
     *
     * Suspending because the first call writes it. It names an install and nothing else: it is
     * quoted in a bug report so a maintainer can line the report up with what the telemetry console
     * shows, when the user consented to that at all.
     */
    suspend fun installationId(): String
}
