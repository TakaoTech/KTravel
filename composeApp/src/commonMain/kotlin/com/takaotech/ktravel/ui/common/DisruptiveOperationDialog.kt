package com.takaotech.ktravel.ui.common

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import ktravel.composeapp.generated.resources.*
import org.jetbrains.compose.resources.stringResource

/**
 * Displays a dialog for confirming or rejecting a potentially disruptive operation. The dialog
 * includes a title, a descriptive message, and options to confirm or cancel the operation.
 *
 * @param T The type of the payload associated with the dialog operation.
 * @param state The state holder responsible for managing the visibility and actions of the dialog.
 * @param title The title displayed at the top of the dialog. Defaults to a localized string
 *        resource for permanent deletion.
 * @param text The descriptive message displayed in the body of the dialog. Defaults to a localized string
 *        resource for a permanent deletion warning.
 * @param confirmText The text for the confirm button. Defaults to a localized string resource
 *        for a "delete" action.
 */
@Composable
fun <T> DisruptiveOperationDialog(
    state: DisruptiveOperationDialogStateHolder<T>,
    title: String = stringResource(Res.string.permanent_delete_dialog_title),
    text: String = stringResource(Res.string.permanent_delete_dialog_message),
    confirmText: String = stringResource(Res.string.permanent_delete_dialog_confirm),
) {
    if (state.showDialog) {
        AlertDialog(
            onDismissRequest = {
                state.dismiss()
            },
            title = {
                Text(text = title)
            },
            text = {
                Text(text = text)
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        state.confirm()
                    }
                ) {
                    Text(
                        text = confirmText,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        state.dismiss()
                    }
                ) {
                    Text(text = stringResource(Res.string.permanent_delete_dialog_cancel))
                }
            }
        )
    }
}

@Stable
class DisruptiveOperationDialogStateHolder<T>(
    private val onConfirm: (T) -> Unit
) {
    internal var showDialog by mutableStateOf(false)
        private set

    private var payload by mutableStateOf<T?>(null)

    fun show(payload: T) {
        this.payload = payload
        showDialog = true
    }

    fun dismiss() {
        payload = null
        showDialog = false
    }

    fun confirm() {
        payload?.let { onConfirm(it) }
        showDialog = false
    }
}

@Composable
fun <T> rememberDisruptiveOperationDialog(
    onConfirm: (T) -> Unit
): DisruptiveOperationDialogStateHolder<T> {
    return remember(onConfirm) {
        DisruptiveOperationDialogStateHolder(
            onConfirm = onConfirm
        )
    }
}
