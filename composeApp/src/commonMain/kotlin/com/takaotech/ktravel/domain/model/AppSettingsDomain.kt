package com.takaotech.ktravel.domain.model

import com.takaotech.ktravel.core.logging.DEFAULT_LOG_RETENTION_DAYS
import com.takaotech.ktravel.core.telemetry.TelemetryConsent
import com.takaotech.ktravel.domain.staticflows.IntroRequirement
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
 * @property acknowledgedIntroVersion The version of the introduction the user has been through.
 * @property acknowledgedPrivacyVersion The version of the privacy policy that answer was given to.
 * @property consentDecidedAt When it was given, null when it never was.
 * @property logRetentionDays How many days of log files are kept.
 * @property installationId Identifies this installation in a bug report, empty until it is generated.
 */
data class AppSettingsDomain(
    val navigatorRemoteBaseUrl: String = "",
    val telemetryConsent: TelemetryConsent = TelemetryConsent.Unknown,
    val acknowledgedIntroVersion: Int = 0,
    val acknowledgedPrivacyVersion: Int = 0,
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
     * How much of the introduction is due before the application can be used.
     *
     * An introduction that has never been seen brings the whole thing, privacy page included. Once it
     * has, only the privacy half can come back — because the policy was rewritten, or because the
     * answer given to it has expired — and the reading cards are not shown a second time.
     *
     * @param introVersion The version of the introduction this build carries.
     * @param policyVersion The version of the privacy policy this build carries.
     * @param now The moment being judged against.
     */
    fun introRequirement(introVersion: Int, policyVersion: Int, now: Instant): IntroRequirement = when {
        acknowledgedIntroVersion < introVersion -> IntroRequirement.Full
        acknowledgedPrivacyVersion < policyVersion -> IntroRequirement.PrivacyOnly
        effectiveConsent(now) == TelemetryConsent.Unknown -> IntroRequirement.PrivacyOnly
        else -> IntroRequirement.None
    }
}
