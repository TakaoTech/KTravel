@file:OptIn(ExperimentalUuidApi::class)

package com.takaotech.ktravel.data.repository

import com.takaotech.ktravel.core.logging.LogRetention
import com.takaotech.ktravel.core.telemetry.TelemetryConsent
import com.takaotech.ktravel.data.datasource.AppSettingsStorageDataSource
import com.takaotech.ktravel.data.entity.AppSettingsEntity
import com.takaotech.ktravel.data.entity.TelemetrySettingsEntity
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
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.time.Instant
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

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

    /** Serialises the read-modify-write of the installation id, which two callers could race into. */
    private val installationIdLock = Mutex()

    override val settings: StateFlow<AppSettingsDomain> = _settings.asStateFlow()

    override suspend fun updateNavigatorRemote(baseUrl: String) = persist {
        it.copy(navigatorRemoteBaseUrl = baseUrl.trim())
    }

    override suspend fun updateTelemetryConsent(
        consent: TelemetryConsent,
        introVersion: Int,
        privacyVersion: Int,
        decidedAt: Instant,
    ) = persist {
        it.copy(
            telemetryConsent = consent,
            acknowledgedIntroVersion = introVersion,
            acknowledgedPrivacyVersion = privacyVersion,
            consentDecidedAt = decidedAt,
        )
    }

    override suspend fun updateLogRetentionDays(days: Int) = persist {
        it.copy(logRetentionDays = LogRetention.coerce(days))
    }

    override suspend fun installationId(): String = installationIdLock.withLock {
        val existing = _settings.value.installationId
        if (existing.isNotBlank()) return@withLock existing

        val generated = Uuid.random().toString()
        persist { it.copy(installationId = generated) }

        generated
    }

    private suspend fun persist(change: (AppSettingsDomain) -> AppSettingsDomain) {
        val updated = _settings.updateAndGet(change)

        dataSource.saveAppSettings(updated.toEntity())
    }
}

private fun AppSettingsEntity.toDomain(): AppSettingsDomain = AppSettingsDomain(
    navigatorRemoteBaseUrl = navigatorRemoteBaseUrl,
    telemetryConsent = telemetry.consent,
    acknowledgedIntroVersion = telemetry.acknowledgedIntroVersion,
    acknowledgedPrivacyVersion = telemetry.acknowledgedPrivacyVersion,
    consentDecidedAt = telemetry.consentDecidedAtEpochMillis.takeIf { it > 0 }
        ?.let(Instant::fromEpochMilliseconds),
    logRetentionDays = LogRetention.coerce(telemetry.logRetentionDays),
    installationId = telemetry.installationId,
)

private fun AppSettingsDomain.toEntity(): AppSettingsEntity = AppSettingsEntity(
    navigatorRemoteBaseUrl = navigatorRemoteBaseUrl,
    telemetry = TelemetrySettingsEntity(
        consent = telemetryConsent,
        acknowledgedIntroVersion = acknowledgedIntroVersion,
        acknowledgedPrivacyVersion = acknowledgedPrivacyVersion,
        consentDecidedAtEpochMillis = consentDecidedAt?.toEpochMilliseconds() ?: 0,
        logRetentionDays = logRetentionDays,
        installationId = installationId,
    ),
)
