package com.takaotech.ktravel.ui.shared.dialog

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue

@Stable
class DisruptiveOperationDialogStateHolder<T>(private val onConfirm: (T) -> Unit) {
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
 * [onConfirm] can be any lambda, stable or not: it is read through [rememberUpdatedState] instead
 * of being used as a [remember] key, so the holder is built once and always invokes the most recent
 * lambda. Keying on the lambda would rebuild the holder whenever the lambda changes identity —
 * which happens on every recomposition for a lambda capturing a ViewModel or a piece of ui state —
 * and would silently close an open dialog.
 */
@Composable
fun <T> rememberDisruptiveOperationDialog(onConfirm: (T) -> Unit): DisruptiveOperationDialogStateHolder<T> {
    val currentOnConfirm by rememberUpdatedState(onConfirm)
    return remember {
        DisruptiveOperationDialogStateHolder { payload -> currentOnConfirm(payload) }
    }
}
