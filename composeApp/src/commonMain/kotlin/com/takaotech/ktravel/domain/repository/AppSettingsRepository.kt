package com.takaotech.ktravel.domain.repository

import com.takaotech.ktravel.core.annotation.OpenForMokkery
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
}
