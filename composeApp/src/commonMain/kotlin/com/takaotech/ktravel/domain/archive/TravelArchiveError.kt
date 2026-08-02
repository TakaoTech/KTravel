package com.takaotech.ktravel.domain.archive

/**
 * Cause di fallimento di export e import di un archivio viaggio.
 *
 * Sono tipizzate perché la UI deve poterle distinguere: "archivio troppo vecchio" e "archivio
 * danneggiato" richiedono messaggi diversi e azioni diverse da parte dell'utente.
 */
sealed interface TravelArchiveError {

    /** Il file non è uno zip leggibile, oppure le entry sono incoerenti o pericolose. */
    data class CorruptedArchive(val reason: String) : TravelArchiveError

    /** Manca una entry attesa (`manifest.json`, il file del piano, ...). */
    data class MissingEntry(val entryPath: String) : TravelArchiveError

    /** `manifest.json` è presente ma non è un manifest KTravel valido. */
    data class InvalidManifest(val reason: String) : TravelArchiveError

    /** Schema troppo vecchio: nessuna catena di migrazione lo copre più. */
    data class UnsupportedSchemaVersion(val found: Int, val minSupported: Int) : TravelArchiveError

    /** Schema prodotto da una versione futura dell'app. */
    data class FutureSchemaVersion(val found: Int, val current: Int) : TravelArchiveError

    data class MigrationFailed(
        val fromVersion: Int,
        val toVersion: Int,
        val reason: String
    ) : TravelArchiveError

    /** Il piano non è deserializzabile nel formato corrente, nemmeno dopo la migrazione. */
    data class MalformedPlanJson(val reason: String) : TravelArchiveError

    /** Il piano referenzia un allegato che l'archivio non contiene. */
    data class MissingAttachment(val relativePath: String) : TravelArchiveError

    /** Lettura o scrittura fallita. */
    data class Io(val reason: String) : TravelArchiveError
}

/** Eccezione portatrice di un [TravelArchiveError]: è sempre il failure dei `Result` di archivio. */
class TravelArchiveException(val error: TravelArchiveError) : Exception(error.toString())

/**
 * Normalizza un throwable in un [TravelArchiveError], così la UI può fare un `when` esaustivo anche
 * sui fallimenti imprevisti.
 */
fun Throwable.asTravelArchiveError(): TravelArchiveError =
    (this as? TravelArchiveException)?.error
        ?: TravelArchiveError.Io(message ?: this::class.simpleName.orEmpty())

/** Riporta un throwable qualsiasi al tipo di failure usato da tutte le API di archivio. */
internal fun Throwable.asTravelArchiveException(): TravelArchiveException =
    this as? TravelArchiveException ?: TravelArchiveException(asTravelArchiveError())
