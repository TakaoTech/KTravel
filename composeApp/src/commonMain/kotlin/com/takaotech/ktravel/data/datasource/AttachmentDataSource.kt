package com.takaotech.ktravel.data.datasource

import com.takaotech.ktravel.data.entity.AttachmentEntity
import io.github.vinceglb.filekit.PlatformFile

/**
 * Gestisce l'inventario file degli step su disco, con schema
 * `<root>/<travelId>/<stepId>/<uuid>.<ext>` (root = cartella degli allegati dell'app).
 *
 * I binari risiedono su disco; i metadati ([AttachmentEntity]) — con path **relativo** alla root —
 * vengono conservati altrove (nel documento del piano). Il path relativo è essenziale per la
 * portabilità: su iOS il container sandbox cambia tra i lanci e va ricostruito a runtime.
 */
interface AttachmentDataSource {

    /**
     * Copia [source] nell'inventario dello step, generando un nome fisico univoco. Ritorna i
     * metadati con [AttachmentEntity.relativePath] relativo alla root.
     */
    suspend fun saveAttachment(travelId: String, stepId: String, source: PlatformFile): AttachmentEntity

    /** Risolve un path relativo nel [PlatformFile] assoluto per rendering/apertura. */
    fun resolveFile(relativePath: String): PlatformFile

    /** Elimina il singolo file identificato dal path relativo. No-op se già assente. */
    suspend fun deleteAttachment(relativePath: String)

    /** Elimina ricorsivamente l'intera cartella `<root>/<travelId>`. No-op se assente. */
    suspend fun deleteTravelAttachments(travelId: String)
}
