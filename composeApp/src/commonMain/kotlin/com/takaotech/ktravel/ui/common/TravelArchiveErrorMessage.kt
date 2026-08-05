package com.takaotech.ktravel.ui.common

import androidx.compose.runtime.Composable
import com.takaotech.ktravel.domain.archive.TravelArchiveError
import ktravel.composeapp.generated.resources.Res
import ktravel.composeapp.generated.resources.travel_archive_error_corrupted
import ktravel.composeapp.generated.resources.travel_archive_error_invalid_manifest
import ktravel.composeapp.generated.resources.travel_archive_error_io
import ktravel.composeapp.generated.resources.travel_archive_error_malformed_plan
import ktravel.composeapp.generated.resources.travel_archive_error_migration_failed
import ktravel.composeapp.generated.resources.travel_archive_error_missing_attachment
import ktravel.composeapp.generated.resources.travel_archive_error_missing_entry
import ktravel.composeapp.generated.resources.travel_archive_error_too_new
import ktravel.composeapp.generated.resources.travel_archive_error_too_old
import org.jetbrains.compose.resources.stringResource

/**
 * Messaggio utente per un errore di archivio. Il `when` è esaustivo: aggiungere un caso a
 * [TravelArchiveError] senza tradurlo diventa un errore di compilazione.
 */
@Composable
fun TravelArchiveError.message(): String = when (this) {
    is TravelArchiveError.CorruptedArchive ->
        stringResource(Res.string.travel_archive_error_corrupted)

    is TravelArchiveError.MissingEntry ->
        stringResource(Res.string.travel_archive_error_missing_entry, entryPath)

    is TravelArchiveError.InvalidManifest ->
        stringResource(Res.string.travel_archive_error_invalid_manifest)

    is TravelArchiveError.UnsupportedSchemaVersion ->
        stringResource(Res.string.travel_archive_error_too_old)

    is TravelArchiveError.FutureSchemaVersion ->
        stringResource(Res.string.travel_archive_error_too_new)

    is TravelArchiveError.MigrationFailed ->
        stringResource(Res.string.travel_archive_error_migration_failed)

    is TravelArchiveError.MalformedPlanJson ->
        stringResource(Res.string.travel_archive_error_malformed_plan)

    is TravelArchiveError.MissingAttachment ->
        stringResource(Res.string.travel_archive_error_missing_attachment, relativePath)

    is TravelArchiveError.Io -> stringResource(Res.string.travel_archive_error_io)
}
