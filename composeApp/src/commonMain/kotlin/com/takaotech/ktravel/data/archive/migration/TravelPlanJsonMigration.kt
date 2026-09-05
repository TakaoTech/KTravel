package com.takaotech.ktravel.data.archive.migration

import kotlinx.serialization.json.JsonObject

/**
 * A single schema step of the travel plan archive.
 *
 * Works on a raw [JsonObject] and never on a data class: a migration has to stay correct even after
 * `TravelPlanEntity` has changed radically, otherwise old archives become unreadable at the first
 * refactor of the entities.
 *
 * Steps are registered in [TravelPlanMigrations] and applied in order by [TravelPlanSchemaMigrator].
 */
internal interface TravelPlanJsonMigration {

    /** Version read as input; the migration produces [fromVersion] + 1. */
    val fromVersion: Int

    /**
     * Rewrites [plan] into the shape expected by version [fromVersion] + 1.
     *
     * Throwing is allowed: [TravelPlanSchemaMigrator] wraps the failure into a
     * `TravelArchiveError.MigrationFailed` naming the two versions involved.
     */
    fun migrate(plan: JsonObject): JsonObject
}
