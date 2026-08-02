package com.takaotech.ktravel.data.archive.migration

import kotlinx.serialization.json.JsonObject

/**
 * Migrazione di un singolo gradino dello schema del piano.
 *
 * Lavora su [JsonObject] puro e mai su una data class: una migrazione deve restare corretta anche
 * dopo che `TravelPlanEntity` è cambiata radicalmente, altrimenti gli archivi vecchi diventano
 * illeggibili al primo refactor delle entity.
 */
internal interface TravelPlanJsonMigration {

    /** Versione letta in ingresso; la migrazione produce [fromVersion] + 1. */
    val fromVersion: Int

    fun migrate(plan: JsonObject): JsonObject
}
