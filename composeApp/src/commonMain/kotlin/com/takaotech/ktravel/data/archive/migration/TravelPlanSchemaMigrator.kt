package com.takaotech.ktravel.data.archive.migration

import com.takaotech.ktravel.data.archive.TravelArchiveFormat
import com.takaotech.ktravel.domain.archive.TravelArchiveError
import com.takaotech.ktravel.domain.archive.TravelArchiveException
import kotlinx.serialization.json.JsonObject

/**
 * Brings the plan JSON from the version stored in the archive up to the current one, applying the
 * registered migrations in sequence.
 *
 * The steps come from a [TravelPlanMigrationFactory] and are applied one version at a time, so a gap
 * in the chain is reported as the specific step that is missing rather than as a generic failure.
 * The dependencies are constructor parameters with production defaults purely so tests can drive an
 * arbitrary version range.
 */
internal class TravelPlanSchemaMigrator(
    private val migrations: TravelPlanMigrationFactory = TravelPlanMigrations,
    private val currentVersion: Int = TravelArchiveFormat.CURRENT_SCHEMA_VERSION,
    private val minSupportedVersion: Int = TravelArchiveFormat.MIN_SUPPORTED_SCHEMA_VERSION,
) {

    /**
     * Migrates [plan], read from an archive declaring [fromVersion], up to [currentVersion].
     *
     * Returns [plan] untouched when it is already at the current version, since the chain is then
     * empty.
     *
     * @throws TravelArchiveException with [TravelArchiveError.UnsupportedSchemaVersion] when
     * [fromVersion] is older than [minSupportedVersion], [TravelArchiveError.FutureSchemaVersion]
     * when it was written by a newer build, or [TravelArchiveError.MigrationFailed] when a step is
     * missing from the chain or throws.
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
