package com.takaotech.ktravel.data.archive.migration

/**
 * Registro delle migrazioni dello schema del piano, in ordine di versione.
 *
 * Per introdurre la v2: creare `TravelPlanMigrationV1ToV2 : TravelPlanJsonMigration`, registrarla
 * qui e alzare `TravelArchiveFormat.CURRENT_SCHEMA_VERSION`. Nient'altro va toccato.
 *
 * Per smettere di supportare la v1: alzare `TravelArchiveFormat.MIN_SUPPORTED_SCHEMA_VERSION`; il
 * rigetto e il messaggio all'utente esistono già.
 */
internal object TravelPlanMigrations {
    val ALL: List<TravelPlanJsonMigration> = emptyList()
}
