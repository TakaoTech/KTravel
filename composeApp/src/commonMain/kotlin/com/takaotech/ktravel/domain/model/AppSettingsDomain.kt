package com.takaotech.ktravel.domain.model

import com.takaotech.ktravel.core.logging.DEFAULT_LOG_RETENTION_DAYS
import com.takaotech.ktravel.core.telemetry.TelemetryConsent
import com.takaotech.ktravel.domain.staticflows.isConsentExpired
import kotlin.time.Instant

/**
 * Preferences of this installation, read and written through
 * [com.takaotech.ktravel.domain.repository.AppSettingsRepository].
 *
 * They belong to the device rather than to a trip, which is what separates them from
 * [TravelSettingsDomain]: a trip may override where its routes are computed, but the address it
 * overrides has to exist somewhere that outlives any single trip.
 *
 * @property navigatorRemoteBaseUrl Origin of the remote navigator, empty when none is configured.
 * @property telemetryConsent What the user answered about sending diagnostics away from the device.
 * @property acknowledgedConsentVersion The version of the privacy notice that answer was given to.
 * @property consentDecidedAt When it was given, null when it never was.
 * @property logRetentionDays How many days of log files are kept.
 * @property installationId Identifies this installation in a bug report, empty until it is generated.
 */
data class AppSettingsDomain(
    val navigatorRemoteBaseUrl: String = "",
    val telemetryConsent: TelemetryConsent = TelemetryConsent.Unknown,
    val acknowledgedConsentVersion: Int = 0,
    val consentDecidedAt: Instant? = null,
    val logRetentionDays: Int = DEFAULT_LOG_RETENTION_DAYS,
    val installationId: String = "",
) {
    /**
     * Whether a remote navigator can be reached at all.
     *
     * An address is all it takes. The only credential that travels is the trip's own provider key,
     * and that one is sent by the routing call rather than configured here.
     */
    val hasRemoteNavigator: Boolean get() = navigatorRemoteBaseUrl.isNotBlank()

    /**
     * The consent as it stands at [now], which is not always the one that was stored.
     *
     * A decision older than [com.takaotech.ktravel.domain.staticflows.CONSENT_VALIDITY] is treated as
     * absent: telemetry stops before the user is asked again, rather than after they answer.
     *
     * @param now The moment being judged against.
     */
    fun effectiveConsent(now: Instant): TelemetryConsent = when {
        consentDecidedAt == null -> TelemetryConsent.Unknown
        isConsentExpired(consentDecidedAt, now) -> TelemetryConsent.Unknown
        else -> telemetryConsent
    }

    /**
     * Whether the privacy notice has to be shown before the application can be used.
     *
     * Three reasons, and they are all the same reason: the answer on file was not given to the
     * notice that is being shipped now. It is missing, it was given to an older version, or it has
     * expired.
     *
     * @param flowVersion The version of the notice this build carries.
     * @param now The moment being judged against.
     */
    fun needsConsentFlow(flowVersion: Int, now: Instant): Boolean =
        effectiveConsent(now) == TelemetryConsent.Unknown || acknowledgedConsentVersion < flowVersion
}
