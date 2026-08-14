package com.takaotech.ktravel.data.repository

import com.takaotech.ktravel.data.datasource.AppSettingsStorageDataSource
import com.takaotech.ktravel.data.entity.AppSettingsEntity
import com.takaotech.ktravel.di.AppScope
import com.takaotech.ktravel.domain.model.AppSettingsDomain
import com.takaotech.ktravel.domain.repository.AppSettingsRepository
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.updateAndGet

/**
 * The installation's preferences, held in memory and written through on every change.
 *
 * Seeded in the constructor rather than asynchronously, the way [TravelPlanRepositoryImpl] seeds a
 * plan: it is one document read by primary key from a local database, and the alternative is a flow
 * that briefly reports "no remote navigator configured" to whoever reads it first — which, for the
 * routing layer, would mean silently computing a route on the embedded server instead.
 */
@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class)
@Inject
class AppSettingsRepositoryImpl(private val dataSource: AppSettingsStorageDataSource) : AppSettingsRepository {

    private val _settings = MutableStateFlow(dataSource.getAppSettings().toDomain())

    override val settings: StateFlow<AppSettingsDomain> = _settings.asStateFlow()

    override suspend fun updateNavigatorRemote(baseUrl: String) {
        val updated = _settings.updateAndGet {
            it.copy(navigatorRemoteBaseUrl = baseUrl.trim())
        }

        dataSource.saveAppSettings(updated.toEntity())
    }
}

private fun AppSettingsEntity.toDomain(): AppSettingsDomain =
    AppSettingsDomain(navigatorRemoteBaseUrl = navigatorRemoteBaseUrl)

private fun AppSettingsDomain.toEntity(): AppSettingsEntity =
    AppSettingsEntity(navigatorRemoteBaseUrl = navigatorRemoteBaseUrl)
