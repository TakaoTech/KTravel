package com.takaotech.ktravel.data.entity

import com.takaotech.ktravel.core.logging.DEFAULT_LOG_RETENTION_DAYS
import com.takaotech.ktravel.core.telemetry.TelemetryConsent
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * What the user answered about diagnostics, and what that answer was given to, as they are stored.
 *
 * Nested under [AppSettingsEntity] rather than spread across it: these fields are read and written
 * together — the introduction ends on the question, and the answer is only meaningful next to the
 * versions of the introduction and of the privacy policy it was given to.
 *
 * Every field needs a default here too: the object is absent from a document written by a build that
 * predates diagnostics, and it is read back with `ignoreUnknownKeys`, never migrated.
 *
 * @constructor Creates a new TelemetrySettingsEntity
 * @property consent What the user answered about sending diagnostics. A value this build does not
 *   know reads back as `Unknown`, through the `coerceInputValues` of the data source.
 * @property acknowledgedIntroVersion The version of the introduction the user has already been
 *   through. A newer introduction is shown again.
 * @property acknowledgedPrivacyVersion The version of the privacy policy the answer was given to. A
 *   newer policy means the question has to be asked again.
 * @property consentDecidedAtEpochMillis When the answer was given, zero when there is none. An
 *   answer expires, so this is what says the privacy page is due again.
 * @property logRetentionDays How many days of log files are kept.
 * @property installationId Identifies this installation in a bug report and, once consented, in the
 *   telemetry console. Empty until it is generated on first use; it names an install, not a person.
 */
@Serializable
data class TelemetrySettingsEntity(
    @SerialName("consent") val consent: TelemetryConsent = TelemetryConsent.Unknown,
    @SerialName("acknowledged_intro_version") val acknowledgedIntroVersion: Int = 0,
    @SerialName("acknowledged_privacy_version") val acknowledgedPrivacyVersion: Int = 0,
    @SerialName("consent_decided_at") val consentDecidedAtEpochMillis: Long = 0,
    @SerialName("log_retention_days") val logRetentionDays: Int = DEFAULT_LOG_RETENTION_DAYS,
    @SerialName("installation_id") val installationId: String = "",
)
