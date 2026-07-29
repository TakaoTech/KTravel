package com.takaotech.ktravel.ui.common

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
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
        DisruptiveOperationDialog(
            onConfirm = { state.confirm() },
            onDismiss = { state.dismiss() },
            title = title,
            text = text,
            confirmText = confirmText
        )
    }
}

/**
 * Variante stateless del dialog di conferma: la visibilità è decisa dal chiamante (es. stato del
 * presenter), che riceve conferma o annullamento tramite [onConfirm]/[onDismiss].
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
        onDismissRequest = onDismiss,
        title = {
            Text(text = title)
        },
        text = {
            Text(text = text)
        },
        confirmButton = {
            TextButton(
                onClick = onConfirm
            ) {
                Text(
                    text = confirmText,
                    color = MaterialTheme.colorScheme.error
                )
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss
            ) {
                Text(text = stringResource(Res.string.permanent_delete_dialog_cancel))
            }
        }
    )
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

/**
 * Creates and remembers a [DisruptiveOperationDialogStateHolder].
 *
 * [onConfirm] can be any lambda, stable or not: it is read through [rememberUpdatedState] instead of
 * being used as a [remember] key, so the holder is built once and always invokes the most recent
 * lambda. Keying on the lambda would rebuild the holder whenever the lambda changes identity — which
 * happens on every recomposition for a lambda capturing a ViewModel or a piece of ui state — and
 * would silently close an open dialog.
 */
@Composable
fun <T> rememberDisruptiveOperationDialog(
    onConfirm: (T) -> Unit
): DisruptiveOperationDialogStateHolder<T> {
    val currentOnConfirm by rememberUpdatedState(onConfirm)
    return remember {
        DisruptiveOperationDialogStateHolder { payload -> currentOnConfirm(payload) }
    }
}
