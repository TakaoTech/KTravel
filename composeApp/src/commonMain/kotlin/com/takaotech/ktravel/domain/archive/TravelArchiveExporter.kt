package com.takaotech.ktravel.domain.archive

import com.takaotech.ktravel.core.annotation.OpenForMokkery
import io.github.vinceglb.filekit.PlatformFile

@OpenForMokkery
interface TravelArchiveExporter {

    /**
     * Builds the archive of trip [travelId] and writes it to [destination].
     *
     * [destination] may be a `content://` picked by the file saver: the archive is assembled in a
     * real staging directory and then copied using FileKit APIs only.
     *
     * The [Result] failure is always a [TravelArchiveException].
     *
     * When [secretsPassword] is given and the plan has an API key, the key is written to
     * `secrets.json` encrypted under that password. It is **never** written to `travel.json`:
     * the plan entry always carries an empty key, whether or not the user opted in.
     */
    suspend fun export(
        travelId: String,
        destination: PlatformFile,
        secretsPassword: String? = null,
    ): Result<TravelArchiveExportResult>
}

data class TravelArchiveExportResult(
    val travelId: String,
    val travelName: String,
    val attachmentCount: Int,
    /**
     * Attachments referenced by the plan but no longer present on disk: they are skipped without
     * failing the export, because a dangling reference is a condition the app already tolerates and
     * blocking the export would make the trip impossible to export.
     */
    val skippedAttachments: List<String>,
)
