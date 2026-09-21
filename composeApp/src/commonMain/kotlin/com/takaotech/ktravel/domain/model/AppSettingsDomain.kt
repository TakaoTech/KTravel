package com.takaotech.ktravel.domain.model

import com.takaotech.ktravel.core.logging.DEFAULT_LOG_RETENTION_DAYS
import com.takaotech.ktravel.core.telemetry.TelemetryConsent
import com.takaotech.ktravel.domain.staticflows.IntroRequirement

/**
 * Preferences of this installation, read and written through
 * [com.takaotech.ktravel.domain.repository.AppSettingsRepository].
 *
 * They belong to the device rather than to a trip, which is what separates them from
 * [TravelSettingsDomain]: a trip may override where its routes are computed, but the address it
 * overrides has to exist somewhere that outlives any single trip.
 *
 * @property navigatorRemoteBaseUrl Origin of the remote navigator, empty when none is configured.
 * @property telemetryConsent Whether diagnostics may leave the device. Diagnostics run on the
 *   developer's legitimate interest, so the user opposes rather than consents: the introduction
 *   leaves it on unless they turn it off there.
 * @property acknowledgedIntroVersion The version of the introduction the user has been through.
 * @property acknowledgedPrivacyVersion The version of the privacy policy they were shown.
 * @property logRetentionDays How many days of log files are kept.
 * @property installationId Identifies this installation in a bug report, empty until it is generated.
 */
data class AppSettingsDomain(
    val navigatorRemoteBaseUrl: String = "",
    val telemetryConsent: TelemetryConsent = TelemetryConsent.Unknown,
    val acknowledgedIntroVersion: Int = 0,
    val acknowledgedPrivacyVersion: Int = 0,
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
     * How much of the introduction is due before the application can be used.
     *
     * An introduction that has never been seen brings the whole thing, privacy page included. Once it
     * has, only the privacy half can come back, because the policy was rewritten, and the reading
     * cards are not shown a second time. A stored value this build cannot read falls back to
     * [TelemetryConsent.Unknown], which means nobody has been told what the application sends yet:
     * the privacy half is due for that too.
     *
     * @param introVersion The version of the introduction this build carries.
     * @param policyVersion The version of the privacy policy this build carries.
     */
    fun introRequirement(introVersion: Int, policyVersion: Int): IntroRequirement = when {
        acknowledgedIntroVersion < introVersion -> IntroRequirement.Full
        acknowledgedPrivacyVersion < policyVersion -> IntroRequirement.PrivacyOnly
        telemetryConsent == TelemetryConsent.Unknown -> IntroRequirement.PrivacyOnly
        else -> IntroRequirement.None
    }
}
