package com.takaotech.ktravel.ui.common

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import ktravel.composeapp.generated.resources.*
import org.jetbrains.compose.resources.stringResource

@Composable
fun <T> PermanentDeleteDialog(
    state: PermanentDeleteDialogStateHolder<T>
) {
    if (state.showDialog) {
        AlertDialog(
            onDismissRequest = {
                state.dismiss()
            },
            title = {
                Text(text = stringResource(Res.string.permanent_delete_dialog_title))
            },
            text = {
                Text(text = stringResource(Res.string.permanent_delete_dialog_message))
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        state.confirm()
                    }
                ) {
                    Text(text = stringResource(Res.string.permanent_delete_dialog_confirm))
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
class PermanentDeleteDialogStateHolder<T>(
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
fun <T> rememberPermanentDeleteDialogState(
    onConfirm: (T) -> Unit
): PermanentDeleteDialogStateHolder<T> {
    return remember(onConfirm) {
        PermanentDeleteDialogStateHolder(
            onConfirm = onConfirm
        )
    }
}