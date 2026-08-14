package com.takaotech.ktravel.data.entity

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Preferences of this installation, as they are stored.
 * Deliberately not a field on [TravelPlanEntity]. Which navigator this device talks to is a property
 *
 * @constructor Creates a new AppSettingsEntity
 * @property type
 * @property navigatorRemoteBaseUrl Origin of the remote navigator, empty when none is configured.
 */
@Serializable
data class AppSettingsEntity(
    @SerialName("type") val type: String = DOCUMENT_TYPE,
    @SerialName("navigator_remote_base_url") val navigatorRemoteBaseUrl: String = "",
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
