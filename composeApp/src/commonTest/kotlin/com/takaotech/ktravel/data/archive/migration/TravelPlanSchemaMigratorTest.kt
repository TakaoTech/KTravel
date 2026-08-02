package com.takaotech.ktravel.data.archive.migration

import com.takaotech.ktravel.domain.archive.TravelArchiveError
import com.takaotech.ktravel.domain.archive.TravelArchiveException
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray

/** Migrazione fittizia che accoda il proprio nome a una lista di tracciamento nel JSON. */
private class TracingMigration(override val fromVersion: Int) : TravelPlanJsonMigration {
    override fun migrate(plan: JsonObject): JsonObject = buildJsonObject {
        plan.forEach { (key, value) -> if (key != TRACE) put(key, value) }
        putJsonArray(TRACE) {
            plan[TRACE]?.jsonArray?.forEach { add(it) }
            add(JsonPrimitive("v$fromVersion->v${fromVersion + 1}"))
        }
    }

    companion object {
        const val TRACE = "trace"
    }
}

private class FailingMigration(override val fromVersion: Int) : TravelPlanJsonMigration {
    override fun migrate(plan: JsonObject): JsonObject = error("boom")
}

class TravelPlanSchemaMigratorTest : BehaviorSpec({

    val plan = buildJsonObject { put("name", "Tokyo") }

    given("a migrator with a v1->v2 and a v2->v3 migration") {
        val migrator = TravelPlanSchemaMigrator(
            migrations = listOf(TracingMigration(1), TracingMigration(2)),
            currentVersion = 3,
            minSupportedVersion = 1
        )

        `when`("migrating a plan from version 1") {
            val migrated = migrator.migrate(plan, fromVersion = 1)

            then("both migrations run in order") {
                migrated[TracingMigration.TRACE]?.jsonArray?.map { it.jsonPrimitive.content } shouldBe
                        listOf("v1->v2", "v2->v3")
            }

            then("the untouched fields are preserved") {
                migrated["name"]?.jsonPrimitive?.content shouldBe "Tokyo"
            }
        }

        `when`("migrating a plan already at the current version") {
            val migrated = migrator.migrate(plan, fromVersion = 3)

            then("the json is returned untouched") {
                migrated shouldBe plan
            }
        }

        `when`("the archive schema is newer than the app") {
            then("it is rejected as a future version") {
                val exception = shouldThrow<TravelArchiveException> {
                    migrator.migrate(plan, fromVersion = 4)
                }
                exception.error shouldBe TravelArchiveError.FutureSchemaVersion(
                    found = 4,
                    current = 3
                )
            }
        }
    }

    given("a migrator whose oldest supported version is 2") {
        val migrator = TravelPlanSchemaMigrator(
            migrations = listOf(TracingMigration(2)),
            currentVersion = 3,
            minSupportedVersion = 2
        )

        `when`("migrating a plan from the dropped version 1") {
            then("it is rejected as unsupported") {
                val exception = shouldThrow<TravelArchiveException> {
                    migrator.migrate(plan, fromVersion = 1)
                }
                exception.error shouldBe TravelArchiveError.UnsupportedSchemaVersion(
                    found = 1,
                    minSupported = 2
                )
            }
        }
    }

    given("a migrator with a gap in the migration chain") {
        val migrator = TravelPlanSchemaMigrator(
            migrations = listOf(TracingMigration(1)),
            currentVersion = 3,
            minSupportedVersion = 1
        )

        `when`("the missing step is reached") {
            then("migration fails reporting the missing step") {
                val exception = shouldThrow<TravelArchiveException> {
                    migrator.migrate(plan, fromVersion = 1)
                }
                exception.error shouldBe TravelArchiveError.MigrationFailed(
                    fromVersion = 2,
                    toVersion = 3,
                    reason = "no migration registered"
                )
            }
        }
    }

    given("a migrator whose migration throws") {
        val migrator = TravelPlanSchemaMigrator(
            migrations = listOf(FailingMigration(1)),
            currentVersion = 2,
            minSupportedVersion = 1
        )

        `when`("the failing migration runs") {
            then("the failure is reported with the involved versions") {
                val exception = shouldThrow<TravelArchiveException> {
                    migrator.migrate(plan, fromVersion = 1)
                }
                exception.error shouldBe TravelArchiveError.MigrationFailed(
                    fromVersion = 1,
                    toVersion = 2,
                    reason = "boom"
                )
            }
        }
    }
})
