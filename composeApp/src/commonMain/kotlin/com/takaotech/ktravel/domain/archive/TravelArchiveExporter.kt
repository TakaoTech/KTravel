package com.takaotech.ktravel.domain.archive

import com.takaotech.ktravel.core.annotation.OpenForMokkery
import io.github.vinceglb.filekit.PlatformFile

@OpenForMokkery
interface TravelArchiveExporter {

    /**
     * Costruisce l'archivio del viaggio [travelId] e lo scrive in [destination].
     *
     * [destination] può essere un `content://` scelto dal file saver: l'archivio viene assemblato in
     * una directory di staging reale e poi copiato con le sole API di FileKit.
     *
     * Il failure del [Result] è sempre una [TravelArchiveException].
     */
    suspend fun export(
        travelId: String,
        destination: PlatformFile
    ): Result<TravelArchiveExportResult>
}

data class TravelArchiveExportResult(
    val travelId: String,
    val travelName: String,
    val attachmentCount: Int,
    /**
     * Allegati referenziati dal piano ma non più presenti su disco: vengono saltati senza far
     * fallire l'export, perché un riferimento dangling è una condizione che l'app già tollera e
     * bloccare l'export renderebbe il viaggio inesportabile.
     */
    val skippedAttachments: List<String>
)
