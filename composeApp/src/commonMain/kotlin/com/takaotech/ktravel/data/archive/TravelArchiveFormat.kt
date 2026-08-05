package com.takaotech.ktravel.data.archive

/**
 * Costanti del formato di archivio `.ktravel`.
 *
 * Layout delle entry:
 * ```
 * manifest.json                                  versionato, mai migrato
 * travel.json                                    TravelPlanEntity serializzato
 * attachments/<travelId>/<stepId>/<uuid>.<ext>   "attachments/" + AttachmentEntity.relativePath
 * ```
 */
object TravelArchiveFormat {

    /** Estensione prodotta dall'export. */
    const val FILE_EXTENSION: String = "ktravel"

    /** Estensioni accettate in import: gli archivi sono zip, quindi anche `.zip` è valido. */
    val ACCEPTED_EXTENSIONS: Set<String> = setOf(FILE_EXTENSION, "zip")

    const val MANIFEST_ENTRY: String = "manifest.json"
    const val PLAN_ENTRY: String = "travel.json"
    const val ATTACHMENTS_PREFIX: String = "attachments/"

    /** Versione dello schema del piano prodotta da questa build. */
    const val CURRENT_SCHEMA_VERSION: Int = 1

    /** Versione più vecchia ancora migrabile: sotto questa soglia l'import viene rifiutato. */
    const val MIN_SUPPORTED_SCHEMA_VERSION: Int = 1

    /** Guardie anti zip-bomb applicate prima di leggere qualsiasi contenuto. */
    const val MAX_ENTRIES: Int = 10_000
    const val MAX_UNCOMPRESSED_BYTES: Long = 2L * 1024 * 1024 * 1024

    /** Entry path dell'allegato con [relativePath] dell'inventario. */
    fun attachmentEntry(relativePath: String): String = ATTACHMENTS_PREFIX + relativePath
}
