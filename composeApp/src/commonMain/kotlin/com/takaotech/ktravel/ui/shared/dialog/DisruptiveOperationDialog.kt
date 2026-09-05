package com.takaotech.ktravel.ui.shared.dialog

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import ktravel.composeapp.generated.resources.Res
import ktravel.composeapp.generated.resources.permanent_delete_dialog_cancel
import ktravel.composeapp.generated.resources.permanent_delete_dialog_confirm
import ktravel.composeapp.generated.resources.permanent_delete_dialog_message
import ktravel.composeapp.generated.resources.permanent_delete_dialog_title
import org.jetbrains.compose.resources.stringResource

/**
 * Displays a dialog for confirming or rejecting a potentially disruptive operation. The dialog
 * includes a title, a descriptive message, and options to confirm or cancel the operation.
 *
 * @param T The type of the payload associated with the dialog operation.
 * @param state The state holder responsible for managing the visibility and actions of the dialog.
 * @param title The title displayed at the top of the dialog. Defaults to a localized string
 * resource for permanent deletion.
 * @param text The descriptive message displayed in the body of the dialog. Defaults to a
 *   localized string
 * resource for a permanent deletion warning.
 * @param confirmText The text for the confirm button. Defaults to a localized string resource
 * for a "delete" action.
 */
@Composable
fun <T> DisruptiveOperationDialog(
    state: DisruptiveOperationDialogStateHolder<T>,
    title: String = stringResource(Res.string.permanent_delete_dialog_title),
    text: String = stringResource(Res.string.permanent_delete_dialog_message),
    confirmText: String = stringResource(Res.string.permanent_delete_dialog_confirm),
) {
    if (state.showDialog) {
        DisruptiveOperationDialog(
            onConfirm = { state.confirm() },
            onDismiss = { state.dismiss() },
            title = title,
            text = text,
            confirmText = confirmText,
        )
    }
}

/**
 * The same confirmation, drawn from plain values.
 *
 * Whether it is showing is the caller's to decide, which is what lets a presenter hold that
 * decision and a test answer the dialog without a screen.
 */
@Composable
fun DisruptiveOperationDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    title: String = stringResource(Res.string.permanent_delete_dialog_title),
    text: String = stringResource(Res.string.permanent_delete_dialog_message),
    confirmText: String = stringResource(Res.string.permanent_delete_dialog_confirm),
) {
    AlertDialog(
        modifier = Modifier.testTag(DisruptiveOperationDialogTestTags.DIALOG),
        onDismissRequest = onDismiss,
        title = {
            Text(text = title)
        },
        text = {
            Text(text = text)
        },
        confirmButton = {
            TextButton(
                modifier = Modifier.testTag(DisruptiveOperationDialogTestTags.CONFIRM),
                onClick = onConfirm,
            ) {
                Text(
                    text = confirmText,
                    color = MaterialTheme.colorScheme.error,
                )
            }
        },
        dismissButton = {
            TextButton(
                modifier = Modifier.testTag(DisruptiveOperationDialogTestTags.CANCEL),
                onClick = onDismiss,
            ) {
                Text(text = stringResource(Res.string.permanent_delete_dialog_cancel))
            }
        },
    )
}
