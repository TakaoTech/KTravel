package com.takaotech.ktravel.ui.shared.format

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
import ktravel.composeapp.generated.resources.travel_archive_error_unsupported_secrets_scheme
import ktravel.composeapp.generated.resources.travel_archive_error_wrong_password
import org.jetbrains.compose.resources.stringResource

/**
 * User-facing message for an archive error. The `when` is exhaustive: adding a case to
 * [TravelArchiveError] without a message for it becomes a compile error.
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

    is TravelArchiveError.WrongPassword ->
        stringResource(Res.string.travel_archive_error_wrong_password)

    is TravelArchiveError.UnsupportedSecretsScheme ->
        stringResource(Res.string.travel_archive_error_unsupported_secrets_scheme)
}
