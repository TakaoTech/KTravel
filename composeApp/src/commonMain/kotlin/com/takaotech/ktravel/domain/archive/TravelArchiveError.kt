package com.takaotech.ktravel.domain.archive

/**
 * Failure causes of a travel archive export or import.
 *
 * They are typed because the UI has to tell them apart: "archive too old" and "archive damaged"
 * call for different messages and different actions from the user.
 */
sealed interface TravelArchiveError {

    /** The file is not a readable zip, or its entries are inconsistent or unsafe. */
    data class CorruptedArchive(val reason: String) : TravelArchiveError

    /** An expected entry is missing (`manifest.json`, the plan file, ...). */
    data class MissingEntry(val entryPath: String) : TravelArchiveError

    /** `manifest.json` is present but is not a valid KTravel manifest. */
    data class InvalidManifest(val reason: String) : TravelArchiveError

    /** Schema too old: no migration chain covers it any more. */
    data class UnsupportedSchemaVersion(val found: Int, val minSupported: Int) : TravelArchiveError

    /** Schema produced by a future version of the app. */
    data class FutureSchemaVersion(val found: Int, val current: Int) : TravelArchiveError

    data class MigrationFailed(val fromVersion: Int, val toVersion: Int, val reason: String) :
        TravelArchiveError

    /** The plan cannot be deserialized into the current format, not even after the migration. */
    data class MalformedPlanJson(val reason: String) : TravelArchiveError

    /** The plan references an attachment the archive does not carry. */
    data class MissingAttachment(val relativePath: String) : TravelArchiveError

    /** A read or a write failed. */
    data class Io(val reason: String) : TravelArchiveError

    /**
     * The password did not decrypt `secrets.json`.
     *
     * The AEAD tag is what tells this apart from a corrupted entry: the archive is intact, the
     * password is not. The user can simply try again, so this must never abort the import.
     */
    data object WrongPassword : TravelArchiveError

    /** `secrets.json` was sealed with a scheme this build does not know how to open. */
    data class UnsupportedSecretsScheme(val scheme: String) : TravelArchiveError
}

/** Exception carrying a [TravelArchiveError]: always the failure of the archive `Result`s. */
class TravelArchiveException(val error: TravelArchiveError) : Exception(error.toString())

/**
 * Normalizes a throwable into a [TravelArchiveError], so the UI can keep an exhaustive `when` even
 * over unexpected failures.
 */
fun Throwable.asTravelArchiveError(): TravelArchiveError = (this as? TravelArchiveException)?.error
    ?: TravelArchiveError.Io(message ?: this::class.simpleName.orEmpty())

/** Maps any throwable back to the failure type every archive API uses. */
internal fun Throwable.asTravelArchiveException(): TravelArchiveException =
    this as? TravelArchiveException ?: TravelArchiveException(asTravelArchiveError())
