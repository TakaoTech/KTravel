package com.takaotech.ktravel.ui.common

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.takaotech.password.NbvcxzPasswordStrengthEvaluator
import com.takaotech.password.PasswordStrength
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import ktravel.composeapp.generated.resources.Res
import ktravel.composeapp.generated.resources.export_secrets_cancel
import ktravel.composeapp.generated.resources.export_secrets_confirm
import ktravel.composeapp.generated.resources.export_secrets_include
import ktravel.composeapp.generated.resources.export_secrets_message
import ktravel.composeapp.generated.resources.export_secrets_password
import ktravel.composeapp.generated.resources.export_secrets_password_confirm
import ktravel.composeapp.generated.resources.export_secrets_password_mismatch
import ktravel.composeapp.generated.resources.export_secrets_password_weak
import ktravel.composeapp.generated.resources.export_secrets_title
import org.jetbrains.compose.resources.stringResource

internal object ExportSecretsDialogTestTags {
    const val INCLUDE = "export_secrets_include"
    const val PASSWORD = "export_secrets_password"
    const val CONFIRM_PASSWORD = "export_secrets_password_confirm"
    const val CONFIRM = "export_secrets_confirm"
    const val CANCEL = "export_secrets_cancel"
}

/**
 * Asks whether to include the plan's API key in the archive, and under which password.
 *
 * Only shown when the plan actually has a key. Declining is a first-class outcome: [onConfirm] is
 * then called with a null password and the archive comes out exactly as it did before this feature
 * existed, so it stays importable by older builds.
 */
@Composable
internal fun ExportSecretsDialog(onConfirm: (password: String?) -> Unit, onDismiss: () -> Unit) {
    var includeKey by remember { mutableStateOf(true) }
    var password by remember { mutableStateOf("") }
    var confirmation by remember { mutableStateOf("") }
    var strength by remember { mutableStateOf<PasswordStrength?>(null) }

    val evaluator = remember { NbvcxzPasswordStrengthEvaluator() }

    // The first evaluation materialises the dictionaries, so it never runs on the main thread.
    LaunchedEffect(password) {
        strength = if (password.isEmpty()) {
            null
        } else {
            withContext(Dispatchers.Default) { evaluator.evaluate(password) }
        }
    }

    val mismatch = confirmation.isNotEmpty() && password != confirmation
    // Score 2 is zxcvbn's "somewhat guessable": below it the archive is barely protected at all.
    val tooWeak = password.isNotEmpty() && (strength?.score ?: 0) < MINIMUM_SCORE
    val canConfirm = !includeKey ||
        (password.isNotEmpty() && password == confirmation && !tooWeak)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(Res.string.export_secrets_title)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(stringResource(Res.string.export_secrets_message))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Checkbox(
                        checked = includeKey,
                        onCheckedChange = { includeKey = it },
                        modifier = Modifier.testTag(ExportSecretsDialogTestTags.INCLUDE),
                    )
                    Text(stringResource(Res.string.export_secrets_include))
                }

                if (includeKey) {
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text(stringResource(Res.string.export_secrets_password)) },
                        visualTransformation = PasswordVisualTransformation(),
                        isError = tooWeak,
                        supportingText = if (tooWeak) {
                            { Text(stringResource(Res.string.export_secrets_password_weak)) }
                        } else {
                            null
                        },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag(ExportSecretsDialogTestTags.PASSWORD),
                    )

                    PasswordStrengthMeter(strength)

                    OutlinedTextField(
                        value = confirmation,
                        onValueChange = { confirmation = it },
                        label = { Text(stringResource(Res.string.export_secrets_password_confirm)) },
                        visualTransformation = PasswordVisualTransformation(),
                        isError = mismatch,
                        supportingText = if (mismatch) {
                            { Text(stringResource(Res.string.export_secrets_password_mismatch)) }
                        } else {
                            null
                        },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag(ExportSecretsDialogTestTags.CONFIRM_PASSWORD),
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(password.takeIf { includeKey }) },
                enabled = canConfirm,
                modifier = Modifier.testTag(ExportSecretsDialogTestTags.CONFIRM),
            ) {
                Text(stringResource(Res.string.export_secrets_confirm))
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.testTag(ExportSecretsDialogTestTags.CANCEL),
            ) {
                Text(
                    text = stringResource(Res.string.export_secrets_cancel),
                    style = MaterialTheme.typography.labelLarge,
                )
            }
        },
    )
}

private const val MINIMUM_SCORE = 2
