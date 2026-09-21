package com.takaotech.ktravel.data.entity

import com.takaotech.ktravel.core.logging.DEFAULT_LOG_RETENTION_DAYS
import com.takaotech.ktravel.core.telemetry.TelemetryConsent
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Where the user left diagnostics, and which documents they were shown, as they are stored.
 *
 * Nested under [AppSettingsEntity] rather than spread across it: these fields are read and written
 * together — the introduction ends on diagnostics, and where the user left them is only meaningful
 * next to the versions of the introduction and of the privacy policy they were shown.
 *
 * Every field needs a default here too: the object is absent from a document written by a build that
 * predates diagnostics, and it is read back with `ignoreUnknownKeys`, never migrated.
 *
 * @constructor Creates a new TelemetrySettingsEntity
 * @property consent Where the user left diagnostics. A value this build does not know reads back as
 *   `Unknown`, through the `coerceInputValues` of the data source.
 * @property acknowledgedIntroVersion The version of the introduction the user has already been
 *   through. A newer introduction is shown again.
 * @property acknowledgedPrivacyVersion The version of the privacy policy the user was shown. A newer
 *   policy brings the privacy page back.
 * @property logRetentionDays How many days of log files are kept.
 * @property installationId Identifies this installation in a bug report and, while diagnostics are
 *   on, in the telemetry console. Empty until it is generated on first use; it names an install, not a person.
 */
@Serializable
data class TelemetrySettingsEntity(
    @SerialName("consent") val consent: TelemetryConsent = TelemetryConsent.Unknown,
    @SerialName("acknowledged_intro_version") val acknowledgedIntroVersion: Int = 0,
    @SerialName("acknowledged_privacy_version") val acknowledgedPrivacyVersion: Int = 0,
    @SerialName("log_retention_days") val logRetentionDays: Int = DEFAULT_LOG_RETENTION_DAYS,
    @SerialName("installation_id") val installationId: String = "",
)
