package com.takaotech.ktravel.data.repository

import com.takaotech.ktravel.di.PlanningGraphScope
import com.takaotech.ktravel.domain.model.TravelSettingsDomain
import com.takaotech.ktravel.domain.navigator.NavigatorKind
import com.takaotech.ktravel.domain.repository.SettingsRepository
import com.takaotech.ktravel.domain.repository.TravelPlanRepository
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn

/**
 * Facade over [TravelPlanRepository], which owns the plan document and stays its only writer:
 * keeping a second copy of the settings here would mean two writers racing on the same document.
 */
@SingleIn(PlanningGraphScope::class)
@ContributesBinding(PlanningGraphScope::class)
@Inject
class SettingsRepositoryImpl(private val travelPlanRepository: TravelPlanRepository) : SettingsRepository {

    override val settings: TravelSettingsDomain
        get() = travelPlanRepository.planningState.value.settings

    override suspend fun updateHereApiKey(apiKey: String) =
        travelPlanRepository.updateSettings(settings.copy(hereApiKey = apiKey))

    override suspend fun updateNavigatorSettings(preference: NavigatorKind, remoteBaseUrl: String) =
        travelPlanRepository.updateSettings(
            settings.copy(
                navigatorPreference = preference,
                navigatorRemoteBaseUrl = remoteBaseUrl.trim(),
            ),
        )
}
