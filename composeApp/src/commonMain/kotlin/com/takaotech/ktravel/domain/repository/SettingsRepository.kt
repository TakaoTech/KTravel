package com.takaotech.ktravel.domain.repository

import com.takaotech.ktravel.core.annotation.OpenForMokkery
import com.takaotech.ktravel.domain.model.TravelSettingsDomain

/**
 * Preferences of a single travel plan: the one place that reads and writes them.
 *
 * Settings are per plan rather than per app — the settings page is only reachable from a trip, and
 * an exported archive carries the preferences of the trip it belongs to — so this repository lives
 * in `PlanningGraphScope`, next to [TravelPlanRepository].
 */
@OpenForMokkery
interface SettingsRepository {
    /**
     * Preferences of the plan this graph belongs to. Reading is synchronous: the plan state is
     * already in memory, and [TravelPlanRepository] is its only writer.
     */
    val settings: TravelSettingsDomain

    /** Sets the HERE API key of this plan; an empty value clears it. */
    suspend fun updateHereApiKey(apiKey: String)
}
