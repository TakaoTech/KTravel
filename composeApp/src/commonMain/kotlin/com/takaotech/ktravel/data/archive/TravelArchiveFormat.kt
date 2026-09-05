package com.takaotech.ktravel.data.archive

/**
 * Constants of the `.ktravel` archive format.
 *
 * Entry layout:
 * ```
 * manifest.json                                  versioned, never migrated
 * travel.json                                    serialized TravelPlanEntity
 * secrets.json                                   encrypted credentials, only if manifest.has_secrets
 * attachments/<travelId>/<stepId>/<uuid>.<ext>   "attachments/" + AttachmentEntity.relativePath
 * ```
 */
object TravelArchiveFormat {

    /** Extension produced by the export. */
    const val FILE_EXTENSION: String = "ktravel"

    /** Extensions accepted on import: archives are zips, so `.zip` is valid too. */
    val ACCEPTED_EXTENSIONS: Set<String> = setOf(FILE_EXTENSION, "zip")

    /** Entry read first on import: it says which schema the rest is written in. */
    const val MANIFEST_ENTRY: String = "manifest.json"

    /**
     * Entry holding the serialized plan.
     *
     * The import reads the path from `TravelArchiveManifest.planEntry` rather than from here, so a
     * later build can move the plan without older readers losing it.
     */
    const val PLAN_ENTRY: String = "travel.json"

    /**
     * Credentials encrypted under the password the user chose. Present only when the export asked
     * for it; `travel.json` never holds the key in clear, with or without this entry.
     */
    const val SECRETS_ENTRY: String = "secrets.json"

    /** Folder every attachment sits under, which is what keeps them apart from the metadata. */
    const val ATTACHMENTS_PREFIX: String = "attachments/"

    /** Plan schema version produced by this build. */
    const val CURRENT_SCHEMA_VERSION: Int = 1

    /** Oldest version still migratable: below this threshold the import is rejected. */
    const val MIN_SUPPORTED_SCHEMA_VERSION: Int = 1

    /** Zip-bomb guard: an archive declaring more entries than this is refused unread. */
    const val MAX_ENTRIES: Int = 10_000

    /** Zip-bomb guard: an archive that would expand past this size is refused unread. */
    const val MAX_UNCOMPRESSED_BYTES: Long = 2L * 1024 * 1024 * 1024

    /** Entry path of the attachment with the inventory's [relativePath]. */
    fun attachmentEntry(relativePath: String): String = ATTACHMENTS_PREFIX + relativePath
}
