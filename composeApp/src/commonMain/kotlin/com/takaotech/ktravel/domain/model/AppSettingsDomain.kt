package com.takaotech.ktravel.domain.model

/**
 * Preferences of this installation, read and written through
 * [com.takaotech.ktravel.domain.repository.AppSettingsRepository].
 *
 * They belong to the device rather than to a trip, which is what separates them from
 * [TravelSettingsDomain]: a trip may override where its routes are computed, but the address it
 * overrides has to exist somewhere that outlives any single trip.
 *
 * @property navigatorRemoteBaseUrl Origin of the remote navigator, empty when none is configured.
 */
data class AppSettingsDomain(val navigatorRemoteBaseUrl: String = "") {
    /**
     * Whether a remote navigator can be reached at all.
     *
     * An address is all it takes. The only credential that travels is the trip's own provider key,
     * and that one is sent by the routing call rather than configured here.
     */
    val hasRemoteNavigator: Boolean get() = navigatorRemoteBaseUrl.isNotBlank()
}
