package com.takaotech.ktravel.data.archive

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Contratto stabile dell'archivio: è la sola parte che non viene **mai** migrata, perché è ciò che
 * permette di decidere se e come migrare il resto.
 *
 * Tutti i campi tranne [schemaVersion] e [travelId] hanno un default, così un manifest scritto da
 * una versione futura resta leggibile abbastanza da produrre un errore sensato.
 */
@Serializable
data class TravelArchiveManifest(
    @SerialName("schema_version") val schemaVersion: Int,
    @SerialName("travel_id") val travelId: String,
    @SerialName("travel_name") val travelName: String = "",
    @SerialName("app_version") val appVersion: String = "",
    // Long epoch millis invece di un Instant: nessuna dipendenza dai serializer temporali, e
    // coerente con lo stile già usato nelle entity (`date_epoch_days`).
    @SerialName("exported_at_epoch_millis") val exportedAtEpochMillis: Long = 0L,
    // Esplicito, così una versione futura può spostare il file del piano senza rompere i reader
    // vecchi, che leggono questo campo invece della costante.
    @SerialName("plan_entry") val planEntry: String = TravelArchiveFormat.PLAN_ENTRY,
    @SerialName("attachments") val attachments: List<String> = emptyList()
)
