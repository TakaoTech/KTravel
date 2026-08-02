package com.takaotech.ktravel.domain.archive

import com.takaotech.ktravel.core.annotation.OpenForMokkery
import com.takaotech.ktravel.domain.model.TravelPlanSummary
import io.github.vinceglb.filekit.PlatformFile

@OpenForMokkery
interface TravelArchiveImporter {

    /**
     * Copia [source] in staging, ne valida struttura, versione e allegati, e rileva l'eventuale
     * conflitto di id. **Non scrive nulla** su database o inventario: serve a poter mostrare il
     * dialog di conflitto prima di qualunque modifica, senza rileggere l'archivio dopo la scelta.
     *
     * Il failure del [Result] è sempre una [TravelArchiveException].
     */
    suspend fun stage(source: PlatformFile): Result<StagedTravelArchive>

    /**
     * Applica un archivio già validato.
     *
     * [nameOverride] permette alla UI di passare un nome localizzato (es. "Tokyo (copia)") senza
     * portare le risorse Compose nel data layer.
     */
    suspend fun import(
        staged: StagedTravelArchive,
        strategy: ImportConflictStrategy,
        nameOverride: String? = null
    ): Result<TravelPlanSummary>

    /** Rilascia lo staging di un archivio non importato (annullamento dell'utente o errore). */
    suspend fun discard(staged: StagedTravelArchive)
}

enum class ImportConflictStrategy {
    /** Importa come nuovo viaggio, rigenerando tutti gli id. */
    DUPLICATE,

    /** Sostituisce il viaggio esistente con lo stesso id. */
    REPLACE
}

/**
 * Piano deserializzato di un archivio in staging. Il tipo concreto vive nel data layer, che è
 * l'unico a conoscere il formato delle entity: così il dominio resta senza dipendenze da `data`.
 */
interface StagedPlanPayload

/**
 * Archivio validato in attesa di conferma.
 *
 * Espone solo tipi stabili: i dettagli interni restano `internal` per non rendere instabile lo
 * stato Compose che dovesse referenziarli.
 */
class StagedTravelArchive internal constructor(
    val travelId: String,
    val travelName: String,
    val schemaVersion: Int,
    val exportedAtEpochMillis: Long,
    val attachmentCount: Int,
    /** Nome del viaggio già salvato con lo stesso id, o null se non c'è conflitto. */
    val conflictingTravelName: String?,
    internal val archive: PlatformFile,
    internal val stagingDir: PlatformFile,
    internal val payload: StagedPlanPayload
)
