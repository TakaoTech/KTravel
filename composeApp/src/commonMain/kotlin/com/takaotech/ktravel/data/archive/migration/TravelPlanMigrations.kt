package com.takaotech.ktravel.data.archive.migration

import com.takaotech.ktravel.data.archive.migration.TravelPlanMigrations.migrationFrom


/**
 * Builds the chain of migrations that takes a plan from one schema version to another.
 *
 * Implementations are expected to create the steps on demand: a chain is needed only while an
 * archive is being imported, and holding migration instances for the whole process lifetime buys
 * nothing.
 */
internal fun interface TravelPlanMigrationFactory {

    /**
     * Steps to apply, in order, to go from [fromVersion] to [toVersion] (exclusive).
     *
     * A version with no registered step is simply left out of the chain; reporting the gap is up to
     * the caller, which is the only one that knows how to surface it to the user.
     */
    fun migrationsFrom(fromVersion: Int, toVersion: Int): List<TravelPlanJsonMigration>
}

/**
 * Registry of the plan schema migrations.
 *
 * The registry is code, not state: nothing is instantiated until a chain is requested, and the
 * chain is garbage as soon as the import that asked for it is over.
 *
 * To introduce v2: write `TravelPlanMigrationV1ToV2 : TravelPlanJsonMigration`, add its branch to
 * [migrationFrom] and raise `TravelArchiveFormat.CURRENT_SCHEMA_VERSION`. Nothing else changes.
 *
 * To stop supporting v1: raise `TravelArchiveFormat.MIN_SUPPORTED_SCHEMA_VERSION`; the rejection and
 * the user-facing message already exist.
 */
internal object TravelPlanMigrations : TravelPlanMigrationFactory {

    override fun migrationsFrom(
        fromVersion: Int,
        toVersion: Int
    ): List<TravelPlanJsonMigration> = (fromVersion until toVersion).mapNotNull(::migrationFrom)

    /**
     * The one place a schema step is registered: add a branch, nothing else.
     *
     * Returns null for an unregistered step; [TravelPlanSchemaMigrator] turns the gap into a
     * `MigrationFailed` naming the missing versions.
     */
    private fun migrationFrom(version: Int): TravelPlanJsonMigration? = when (version) {
        // 1 -> TravelPlanMigrationV1ToV2()
        else -> null
    }
}
