package com.takaotech.ktravel.data.entity

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Preferences of this installation, as they are stored.
 * Deliberately not a field on [TravelPlanEntity]. Which navigator this device talks to is a property
 *
 * Every field added here needs a default: the document written by an older build has none of them,
 * and it is read back with `ignoreUnknownKeys`, never migrated.
 *
 * @constructor Creates a new AppSettingsEntity
 * @property type
 * @property navigatorRemoteBaseUrl Origin of the remote navigator, empty when none is configured.
 * @property telemetry The consent given to diagnostics and everything that hangs off it. Absent from
 *   a document written before diagnostics existed, which reads as an installation that has not
 *   answered yet.
 */
@Serializable
data class AppSettingsEntity(
    @SerialName("type") val type: String = DOCUMENT_TYPE,
    @SerialName("navigator_remote_base_url") val navigatorRemoteBaseUrl: String = "",
    @SerialName("telemetry") val telemetry: TelemetrySettingsEntity = TelemetrySettingsEntity(),
) {
    companion object {
        const val DOCUMENT_TYPE = "app_settings"

        /**
         * The one document of this kind.
         *
         * A fixed id rather than a query: there is exactly one set of preferences per installation,
         * and giving it a name means reading it is a lookup that cannot return two answers.
         */
        const val DOCUMENT_ID = "app_settings"
    }
}
