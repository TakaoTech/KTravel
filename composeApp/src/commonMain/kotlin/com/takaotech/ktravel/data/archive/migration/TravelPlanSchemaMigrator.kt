package com.takaotech.ktravel.data.archive.migration

import com.takaotech.ktravel.data.archive.TravelArchiveFormat
import com.takaotech.ktravel.domain.archive.TravelArchiveError
import com.takaotech.ktravel.domain.archive.TravelArchiveException
import kotlinx.serialization.json.JsonObject

/**
 * Porta il JSON del piano dalla versione dell'archivio a quella corrente, applicando in sequenza le
 * migrazioni registrate.
 */
internal class TravelPlanSchemaMigrator(
    private val migrations: TravelPlanMigrationFactory = TravelPlanMigrations,
    private val currentVersion: Int = TravelArchiveFormat.CURRENT_SCHEMA_VERSION,
    private val minSupportedVersion: Int = TravelArchiveFormat.MIN_SUPPORTED_SCHEMA_VERSION,
) {

    /**
     * @throws TravelArchiveException con [TravelArchiveError.UnsupportedSchemaVersion],
     * [TravelArchiveError.FutureSchemaVersion] o [TravelArchiveError.MigrationFailed].
     */
    fun migrate(plan: JsonObject, fromVersion: Int): JsonObject {
        if (fromVersion < minSupportedVersion) {
            throw TravelArchiveException(
                TravelArchiveError.UnsupportedSchemaVersion(fromVersion, minSupportedVersion),
            )
        }
        if (fromVersion > currentVersion) {
            throw TravelArchiveException(
                TravelArchiveError.FutureSchemaVersion(fromVersion, currentVersion),
            )
        }

        // Built after the guards on purpose: an out of range version allocates nothing.
        val chain = migrations.migrationsFrom(fromVersion, currentVersion)

        return (fromVersion until currentVersion).fold(plan) { migrated, version ->
            val migration = chain.firstOrNull { it.fromVersion == version }
                ?: throw TravelArchiveException(
                    TravelArchiveError.MigrationFailed(
                        fromVersion = version,
                        toVersion = version + 1,
                        reason = "no migration registered",
                    ),
                )
            runCatching { migration.migrate(migrated) }.getOrElse { throwable ->
                throw TravelArchiveException(
                    TravelArchiveError.MigrationFailed(
                        fromVersion = version,
                        toVersion = version + 1,
                        reason = throwable.message ?: throwable::class.simpleName.orEmpty(),
                    ),
                )
            }
        }
    }
}
