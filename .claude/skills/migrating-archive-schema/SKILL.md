---
name: migrating-archive-schema
description: Evolve the `.ktravel` archive schema when `TravelPlanEntity` changes, by adding a `TravelPlanJsonMigration` step so archives exported by older builds stay importable. Encodes the four-step procedure (write the migration, register it, bump `CURRENT_SCHEMA_VERSION`, test it), the rule that migrations operate on raw `JsonObject` and never on data classes, and how to drop support for versions that are too old. Use when changing, renaming, or removing a field in `TravelPlanEntity` / `TravelDayEntity` / `StepEntity` / `PlaceEntity` / `AttachmentEntity` / `RouteEntity`; when adding a new `@SerialName`; or when the user mentions schema version, archive migration, `TravelPlanJsonMigration`, `TravelPlanMigrations`, `TravelPlanSchemaMigrator`, `CURRENT_SCHEMA_VERSION`, `MIN_SUPPORTED_SCHEMA_VERSION`, `MalformedPlanJson`, `UnsupportedSchemaVersion`, or "old .ktravel file cannot be imported".
---

# Migrating the `.ktravel` archive schema

## When this is required

A `.ktravel` archive carries `travel.json`, which is `TravelPlanEntity` serialized verbatim. Users
keep archives on disk and in cloud storage, so **any build can be asked to import a file written by
any older build**.

Adding a migration is required whenever a change to the entities would make an older `travel.json`
fail to deserialize or, worse, deserialize into something wrong:

| Change to the entities                                               | Migration needed?                                 |
|----------------------------------------------------------------------|---------------------------------------------------|
| Add a field **with a default**                                       | No — `ignoreUnknownKeys` and the default cover it |
| Add a field **without a default**                                    | **Yes** — old archives lack the key               |
| Rename a `@SerialName`                                               | **Yes** — old key must be moved to the new one    |
| Remove a field                                                       | No — `ignoreUnknownKeys = true` drops it          |
| Change a field's type or unit (seconds → millis, string → object)    | **Yes** — silently wrong otherwise                |
| Change a sealed-class discriminator value (`"place"`, `"transport"`) | **Yes**                                           |
| Restructure nesting (flatten, group, split)                          | **Yes**                                           |

If in doubt, write the migration: a no-op migration is cheap, an unreadable archive is not.

## The four steps

Everything lives in
`composeApp/src/commonMain/kotlin/com/takaotech/ktravel/data/archive/migration/`.

### 1. Write the migration

One class per version step, named after the transition:

```kotlin
// TravelPlanMigrationV1ToV2.kt
package com.takaotech.ktravel.data.archive.migration

import kotlinx.serialization.json.*

/**
 * v1 -> v2: `note` moved from a plain string to `{ "text": ..., "format": "markdown" }`.
 */
internal class TravelPlanMigrationV1ToV2 : TravelPlanJsonMigration {

    override val fromVersion: Int = 1

    override fun migrate(plan: JsonObject): JsonObject = plan.mapPlaceSteps { step ->
        buildJsonObject {
            step.forEach { (key, value) -> if (key != "note") put(key, value) }
            putJsonObject("note") {
                put("text", step["note"]?.jsonPrimitive?.contentOrNull ?: "")
                put("format", "markdown")
            }
        }
    }
}

/** Rebuilds every place step of every day through [transform], leaving the rest untouched. */
private fun JsonObject.mapPlaceSteps(transform: (JsonObject) -> JsonObject): JsonObject =
    buildJsonObject {
        this@mapPlaceSteps.forEach { (key, value) -> if (key != "days") put(key, value) }
        putJsonArray("days") {
            this@mapPlaceSteps["days"]?.jsonArray?.forEach { day ->
                val dayObject = day.jsonObject
                addJsonObject {
                    dayObject.forEach { (key, value) -> if (key != "steps") put(key, value) }
                    putJsonArray("steps") {
                        dayObject["steps"]?.jsonArray?.forEach { step ->
                            val stepObject = step.jsonObject
                            // The sealed-class discriminator tells place steps from transport ones.
                            if (stepObject["type"]?.jsonPrimitive?.contentOrNull == "place") {
                                add(transform(stepObject))
                            } else {
                                add(stepObject)
                            }
                        }
                    }
                }
            }
        }
    }
```

Check the discriminator key against `data/entity/TravelPlanEntity.kt` before relying on it:
`StepEntity` is a `@Serializable sealed class` with `@SerialName("place")` /
`@SerialName("transport")`,
and the key name is whatever the `Json` configuration uses (`"type"` by default).

`fromVersion` is the version read **in**; the migration always produces `fromVersion + 1`. Never
skip a step: to go from 1 to 3 you write two migrations, and `TravelPlanSchemaMigrator` chains them.

### 2. Register it

```kotlin
internal object TravelPlanMigrations {
    val ALL: List<TravelPlanJsonMigration> = listOf(
        TravelPlanMigrationV1ToV2(),
    )
}
```

A gap in the chain is not silent: the migrator raises
`TravelArchiveError.MigrationFailed(from, to, "no migration registered")`.

### 3. Bump the version

In `data/archive/TravelArchiveFormat.kt`:

```kotlin
const val CURRENT_SCHEMA_VERSION: Int = 2
```

Nothing else changes. The exporter stamps the new version into `manifest.json`, and the importer
migrates anything older up to it.

### 4. Test it

See [Tests](#tests) below — a migration without a test is not done.

## Non-negotiable rules

**Operate on `JsonObject`, never on data classes.** A migration written against today's
`TravelPlanEntity` breaks the moment those classes change again, which is exactly when it matters.
`TravelPlanSchemaMigrator.migrate(plan, fromVersion)` runs **before**
`decodeFromJsonElement(TravelPlanEntity.serializer(), migrated)` in
`TravelArchiveImporterImpl.readPlan`, so at migration time the JSON has not been validated against
any schema — that is the point.

**Never mutate an old migration.** Once `TravelPlanMigrationV1ToV2` has shipped, its behavior is
frozen: archives in the wild depend on it. Fixing a bug in it means shipping v3, not editing v2.

**Keys, not properties.** Migrations manipulate the wire names (`@SerialName` values such as
`period_start`, `date_epoch_days`, `relative_path`, `location`), never Kotlin property names. Note
that `StepEntity.Place.name` is serialized as `"location"` for backwards compatibility — check the
`@SerialName` in `data/entity/TravelPlanEntity.kt` before writing a key.

**Do not touch `manifest.json`.** It is the one part of the archive that is never migrated: it is
what tells you *whether* and *how* to migrate. It is read leniently, and every field except
`schema_version` and `travel_id` has a default. If you ever need a new manifest field, give it a
default so old archives keep parsing.

**`travel.json` is untrusted input.** Anything the migration derives from it is untrusted too. Paths
in particular stay subject to `TravelArchiveValidation.isSafeRelativePath` — never bypass it, and if
a migration rewrites `relative_path`, keep producing the `<travelId>/<stepId>/<fileName>` shape.

**Attachment entries follow the plan.** `attachments/<relativePath>` entry paths are derived from
`travel.json`. A migration that rewrites `relative_path` breaks the link to the zip entries: either
leave paths alone, or extend `TravelArchiveImporterImpl.extractAttachments` in the same change.

## Working with `JsonObject`

kotlinx.serialization's JSON tree is immutable — build new objects, don't mutate.

```kotlin
// Rename a key
buildJsonObject {
    plan.forEach { (key, value) -> if (key != "old_name") put(key, value) }
    plan["old_name"]?.let { put("new_name", it) }
}

// Add a key only when absent
if ("new_flag" !in plan) buildJsonObject {
    plan.forEach { (k, v) -> put(k, v) }; put(
    "new_flag",
    false
)
}

// Convert a unit
val seconds = plan["duration_seconds"]?.jsonPrimitive?.longOrNull ?: 0L
put("duration_millis", seconds * 1000)

// Walk into days and steps
val days = plan["days"]?.jsonArray ?: JsonArray(emptyList())
```

Prefer a small private helper (`mapPlaceSteps`, `mapDays`) over four levels of nested
`buildJsonObject`
— the transformation should be readable at a glance.

Be defensive about what you read: a missing or unexpected key must not throw. If a migration does
throw, the migrator wraps it into `TravelArchiveError.MigrationFailed(from, to, message)` and the
user sees "the archive could not be converted to the current format" — correct, but a lost import.

## Dropping old versions

When a migration chain gets long enough to be a liability, raise the floor in
`TravelArchiveFormat.kt`:

```kotlin
const val MIN_SUPPORTED_SCHEMA_VERSION: Int = 2
```

Archives below it are rejected with `TravelArchiveError.UnsupportedSchemaVersion`, which already
maps
to the translated message `travel_archive_error_too_old` in both `values/` and `values-it/`. Delete
the now-unreachable migrations and their registrations in the same change.

Do this only when the old version is genuinely out of circulation: the failure mode is a user who
cannot import their own trip.

## Tests

Add to
`composeApp/src/commonTest/kotlin/com/takaotech/ktravel/data/archive/migration/TravelPlanSchemaMigratorTest.kt`
(chain mechanics, already covered with fake migrations) plus a dedicated spec per migration:

```kotlin
class TravelPlanMigrationV1ToV2Test : BehaviorSpec({
    given("a v1 plan json") {
        val v1 = Json.parseToJsonElement(V1_FIXTURE).jsonObject

        `when`("it is migrated to v2") {
            val migrated = TravelPlanMigrationV1ToV2().migrate(v1)

            then("the note becomes a structured object") { /* ... */ }
            then("the untouched fields are preserved") { /* ... */ }
            then("it deserializes into the current TravelPlanEntity") {
                Json { ignoreUnknownKeys = true }
                    .decodeFromJsonElement(TravelPlanEntity.serializer(), migrated)
            }
        }
    }
})
```

The third assertion is the one that matters: a migration is correct only if its output feeds the
**current** entity. Keep the v1 fixture as a **literal JSON string** in the test — never generate it
by serializing today's entities, or the fixture silently follows your refactors and stops testing
anything.

Also extend `TravelArchiveCorruptionTest`, which builds archives by hand, with a case at the new
boundary: `schema_version = MIN_SUPPORTED - 1` must still yield `UnsupportedSchemaVersion`.

Test names in English, `Given ... When ... Then ...`, per the project rule.

## Verify

```bash
# JDK >= 22 is required (kzip is compiled for Java 22)
export JAVA_HOME=$(/usr/libexec/java_home -v 25)

# --tests does NOT filter Kotest specs; use the env var
KOTEST_FILTER_SPECS='*Migration*' ./gradlew :composeApp:jvmTest --rerun-tasks
KOTEST_FILTER_SPECS='*Archive*'   ./gradlew :composeApp:jvmTest --rerun-tasks

./gradlew :composeApp:compileKotlinIosSimulatorArm64 :composeApp:compileAndroidMain
```

Manual end-to-end check, worth doing once per schema bump: export a trip with the **previous**build,
keep the file, then import it with the new build and confirm the plan, the notes and the attachments
all survive.
