package com.takaotech.ktravel.ui.shared.dialog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import ktravel.composeapp.generated.resources.Res
import ktravel.composeapp.generated.resources.import_secrets_choice_import
import ktravel.composeapp.generated.resources.import_secrets_choice_message
import ktravel.composeapp.generated.resources.import_secrets_choice_skip
import ktravel.composeapp.generated.resources.import_secrets_choice_title
import ktravel.composeapp.generated.resources.import_secrets_password
import ktravel.composeapp.generated.resources.import_secrets_password_cancel
import ktravel.composeapp.generated.resources.import_secrets_password_confirm
import ktravel.composeapp.generated.resources.import_secrets_password_message
import ktravel.composeapp.generated.resources.import_secrets_password_title
import ktravel.composeapp.generated.resources.travel_archive_error_wrong_password
import org.jetbrains.compose.resources.stringResource

/**
 * Asks whether the archived API key should be imported too.
 *
 * Shown after the conflict dialog: the user decides what happens to the trip first, and only then
 * whether its credentials come along. Skipping is not a failure — the trip imports without a key.
 */
@Composable
internal fun ImportSecretsChoiceDialog(onImport: () -> Unit, onSkip: () -> Unit) {
    AlertDialog(
        // Not dismissable by tapping outside: skipping is an explicit answer, not an accident.
        onDismissRequest = { },
        title = { Text(stringResource(Res.string.import_secrets_choice_title)) },
        text = { Text(stringResource(Res.string.import_secrets_choice_message)) },
        confirmButton = {
            TextButton(
                onClick = onImport,
                modifier = Modifier.testTag(ImportSecretsDialogTestTags.CHOICE_IMPORT),
            ) {
                Text(stringResource(Res.string.import_secrets_choice_import))
            }
        },
        dismissButton = {
            TextButton(
                onClick = onSkip,
                modifier = Modifier.testTag(ImportSecretsDialogTestTags.CHOICE_SKIP),
            ) {
                Text(stringResource(Res.string.import_secrets_choice_skip))
            }
        },
    )
}

/**
 * Collects the password that unlocks the archived key.
 *
 * [attemptFailed] marks a wrong previous attempt. The dialog stays up: nothing has been written to
 * the database yet, so retrying is free and cancelling still imports nothing.
 */
@Composable
internal fun ImportSecretsPasswordDialog(
    attemptFailed: Boolean,
    onConfirm: (password: String) -> Unit,
    onCancel: () -> Unit,
) {
    var password by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onCancel,
        title = { Text(stringResource(Res.string.import_secrets_password_title)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(stringResource(Res.string.import_secrets_password_message))

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text(stringResource(Res.string.import_secrets_password)) },
                    visualTransformation = PasswordVisualTransformation(),
                    isError = attemptFailed,
                    supportingText = if (attemptFailed) {
                        { Text(stringResource(Res.string.travel_archive_error_wrong_password)) }
                    } else {
                        null
                    },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag(ImportSecretsDialogTestTags.PASSWORD),
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(password) },
                enabled = password.isNotEmpty(),
                modifier = Modifier.testTag(ImportSecretsDialogTestTags.PASSWORD_CONFIRM),
            ) {
                Text(stringResource(Res.string.import_secrets_password_confirm))
            }
        },
        dismissButton = {
            TextButton(
                onClick = onCancel,
                modifier = Modifier.testTag(ImportSecretsDialogTestTags.PASSWORD_CANCEL),
            ) {
                Text(stringResource(Res.string.import_secrets_password_cancel))
            }
        },
    )
}
