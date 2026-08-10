package com.takaotech.ktravel.domain.archive

import com.takaotech.ktravel.core.annotation.OpenForMokkery
import com.takaotech.ktravel.domain.model.TravelPlanSummary
import io.github.vinceglb.filekit.PlatformFile

@OpenForMokkery
interface TravelArchiveImporter {

    /**
     * Copies [source] into staging, validates its structure, version and attachments, and detects
     * an id conflict if there is one. It **writes nothing** to the database or the inventory: this
     * is what allows the conflict dialog to be shown before any change, without re-reading the
     * archive after the choice.
     *
     * The [Result] failure is always a [TravelArchiveException].
     */
    suspend fun stage(source: PlatformFile): Result<StagedTravelArchive>

    /**
     * Applies an already validated archive.
     *
     * [nameOverride] lets the UI pass a localized name (e.g. "Tokyo (copy)") without pulling the
     * Compose resources into the data layer.
     *
     * [secretsPassword] decrypts the archived API key. Leaving it null imports the plan without a
     * key, which is a normal outcome: the user may not have the password, or may not want the key.
     * A wrong password fails with [TravelArchiveError.WrongPassword] and writes nothing, so the
     * caller can ask again against the same staged archive.
     */
    suspend fun import(
        staged: StagedTravelArchive,
        strategy: ImportConflictStrategy,
        nameOverride: String? = null,
        secretsPassword: String? = null,
    ): Result<TravelPlanSummary>

    /** Releases the staging of an archive that was not imported (user cancellation or error). */
    suspend fun discard(staged: StagedTravelArchive)
}

enum class ImportConflictStrategy {
    /** Imports as a new trip, regenerating every id. */
    DUPLICATE,

    /** Replaces the existing trip with the same id. */
    REPLACE,
}

/**
 * Deserialized plan of a staged archive. The concrete type lives in the data layer, the only one
 * that knows the entity format: this is what keeps the domain free of dependencies on `data`.
 */
interface StagedPlanPayload

/**
 * Validated archive awaiting confirmation.
 *
 * It exposes stable types only: the internal details stay `internal` so that any Compose state
 * referencing them is not made unstable.
 */
class StagedTravelArchive internal constructor(
    val travelId: String,
    val travelName: String,
    val schemaVersion: Int,
    val exportedAtEpochMillis: Long,
    val attachmentCount: Int,
    /** Name of the trip already saved under the same id, or null when there is no conflict. */
    val conflictingTravelName: String?,
    /** True when the archive carries an encrypted API key that a password could unlock. */
    val hasSecrets: Boolean,
    internal val archive: PlatformFile,
    internal val stagingDir: PlatformFile,
    internal val payload: StagedPlanPayload,
)
