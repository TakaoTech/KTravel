package com.takaotech.ktravel.ui.common

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import ktravel.composeapp.generated.resources.Res
import ktravel.composeapp.generated.resources.travel_import_conflict_cancel
import ktravel.composeapp.generated.resources.travel_import_conflict_duplicate
import ktravel.composeapp.generated.resources.travel_import_conflict_message
import ktravel.composeapp.generated.resources.travel_import_conflict_replace
import ktravel.composeapp.generated.resources.travel_import_conflict_title
import org.jetbrains.compose.resources.stringResource

internal object ImportConflictDialogTestTags {
    const val DUPLICATE = "import_conflict_duplicate"
    const val REPLACE = "import_conflict_replace"
    const val CANCEL = "import_conflict_cancel"
}

/**
 * Dialog a tre esiti per un archivio il cui viaggio è già presente.
 *
 * [DisruptiveOperationDialog] non è riutilizzabile qui: modella una conferma binaria, mentre questa
 * scelta ha tre uscite distinte, di cui una sola distruttiva.
 */
@Composable
internal fun ImportConflictDialog(
    existingName: String,
    onDuplicate: () -> Unit,
    onReplace: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = stringResource(Res.string.travel_import_conflict_title)) },
        text = {
            Text(text = stringResource(Res.string.travel_import_conflict_message, existingName))
        },
        // AlertDialog M3 offre due soli slot: le tre azioni stanno tutte nel confirmButton.
        confirmButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                TextButton(
                    modifier = Modifier.testTag(ImportConflictDialogTestTags.CANCEL),
                    onClick = onDismiss,
                ) {
                    Text(text = stringResource(Res.string.travel_import_conflict_cancel))
                }
                TextButton(
                    modifier = Modifier.testTag(ImportConflictDialogTestTags.REPLACE),
                    onClick = onReplace,
                ) {
                    Text(
                        text = stringResource(Res.string.travel_import_conflict_replace),
                        color = MaterialTheme.colorScheme.error,
                    )
                }
                TextButton(
                    modifier = Modifier.testTag(ImportConflictDialogTestTags.DUPLICATE),
                    onClick = onDuplicate,
                ) {
                    Text(text = stringResource(Res.string.travel_import_conflict_duplicate))
                }
            }
        },
    )
}
