package com.takaotech.ktravel.ui.plan.day.notes

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.UriHandler
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import com.mikepenz.markdown.m3.Markdown
import com.takaotech.ktravel.domain.model.AttachmentReference
import com.takaotech.ktravel.presentation.plan.AttachmentUi
import com.takaotech.ktravel.presentation.plan.day.StepNotesEvent
import com.takaotech.ktravel.presentation.plan.day.StepNotesUiState
import com.takaotech.ktravel.ui.plan.day.attachment.AttachmentImageTransformer
import com.takaotech.ktravel.ui.theme.KTravelTheme
import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.dialogs.FileKitType
import io.github.vinceglb.filekit.dialogs.compose.rememberFilePickerLauncher
import io.github.vinceglb.filekit.dialogs.openFileWithDefaultApplication
import io.github.vinceglb.filekit.exists
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.launch
import ktravel.composeapp.generated.resources.Res
import ktravel.composeapp.generated.resources.check
import ktravel.composeapp.generated.resources.edit
import ktravel.composeapp.generated.resources.error
import ktravel.composeapp.generated.resources.planning_detail_attachment_open_error
import ktravel.composeapp.generated.resources.planning_detail_attachments_missing
import ktravel.composeapp.generated.resources.planning_detail_cd_done_note
import ktravel.composeapp.generated.resources.planning_detail_cd_edit_note
import ktravel.composeapp.generated.resources.planning_detail_cd_note_invalid
import ktravel.composeapp.generated.resources.planning_detail_step_note_empty
import ktravel.composeapp.generated.resources.planning_detail_step_note_label
import ktravel.composeapp.generated.resources.planning_detail_step_note_title
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

/**
 * Test tags of one host's notes block.
 *
 * Passed in rather than fixed, because a tag names *where* a node is: two screens showing the same
 * section still need to be told apart by a test that drives one of them.
 */
@Immutable
data class StepNotesTestTags(
    val noteView: String,
    val noteEditor: String,
    val editToggle: String,
    val noteAlert: String,
    val missingReferences: String,
    val attachments: String,
    val attachmentAdd: String,
    val attachmentItem: String,
)

/**
 * Everything the notes of a step need from the platform, assembled once per host screen.
 *
 * The file picker, the OS-delegated opening, the editor handle and the URI handler that makes
 * `ktravel://attachment/...` links open natively are the same wherever notes appear. What differs
 * between hosts is the layout around them and the words on them, which is why those stay arguments
 * of the composables below rather than living here.
 */
@Stable
class StepNotesHost internal constructor(
    /** The editor's handle, `null` while the notes are read-only — it exists only in editing. */
    internal val controller: MarkdownEditorController?,
    internal val imageTransformer: AttachmentImageTransformer,
    internal val uriHandler: UriHandler,
    /** Opens an inventory file with the system application. */
    val openAttachment: (String) -> Unit,
    /** Asks the user for a file to upload into the inventory. */
    val pickAttachment: () -> Unit,
    /** Writes a Markdown reference to [AttachmentUi] at the caret and opens the editor. */
    val insertReference: (AttachmentUi) -> Unit,
)

/**
 * Wires the notes of one step to the platform and to [onEvent].
 *
 * A failure to open a file — missing on disk, or a platform refusal such as
 * `ActivityNotFoundException` — surfaces on [snackbarHostState] rather than being swallowed: the
 * user asked for something visible to happen and nothing did.
 */
@Composable
fun rememberStepNotesHost(
    state: StepNotesUiState,
    snackbarHostState: SnackbarHostState,
    onEvent: (StepNotesEvent) -> Unit,
): StepNotesHost {
    val resolveFile = state.resolveFile

    // The sink is a fresh lambda on every recomposition, and the collector below outlives them all:
    // without this, the effect would either restart on each frame or keep calling a stale one.
    val currentOnEvent by rememberUpdatedState(onEvent)

    // Shared controller: drives the editor and takes caret insertions from the inventory.
    //
    // Built only in editing, because building it parses the whole note and observing it reserializes
    // the whole Markdown on every snapshot — work a screen that only renders the note never needs.
    // Entering the editor therefore always starts from the note as it is saved now.
    val controller = if (state.isEditing) rememberMarkdownEditorController(state.note) else null
    if (controller != null) {
        LaunchedEffect(controller) {
            controller.markdownFlow.collect { currentOnEvent(StepNotesEvent.NoteChanged(it)) }
        }
    }

    val picker = rememberFilePickerLauncher(type = FileKitType.File()) { file ->
        file?.let { currentOnEvent(StepNotesEvent.AddAttachment(it)) }
    }

    val scope = rememberCoroutineScope()
    val openErrorMessage = stringResource(Res.string.planning_detail_attachment_open_error)
    val openAttachment: (String) -> Unit =
        remember(resolveFile, scope, snackbarHostState, openErrorMessage) {
            { relativePath ->
                try {
                    val file = resolveFile(relativePath)
                    if (file.exists()) {
                        FileKit.openFileWithDefaultApplication(file)
                    } else {
                        scope.launch { snackbarHostState.showSnackbar(openErrorMessage) }
                    }
                } catch (e: Exception) {
                    scope.launch { snackbarHostState.showSnackbar(openErrorMessage) }
                }
            }
        }

    val errorPainter = painterResource(Res.drawable.error)
    val imageTransformer = remember(resolveFile, errorPainter) {
        AttachmentImageTransformer(resolveFile = resolveFile, errorPainter = errorPainter)
    }

    // Custom handler: attachment links open natively, everything else keeps the default behaviour.
    val defaultUriHandler = LocalUriHandler.current
    val uriHandler = remember(defaultUriHandler, openAttachment) {
        object : UriHandler {
            override fun openUri(uri: String) {
                val relativePath = AttachmentReference.relativePathOf(uri)
                if (relativePath != null) {
                    openAttachment(relativePath)
                } else {
                    defaultUriHandler.openUri(uri)
                }
            }
        }
    }

    return remember(controller, imageTransformer, uriHandler, openAttachment, picker) {
        StepNotesHost(
            controller = controller,
            imageTransformer = imageTransformer,
            uriHandler = uriHandler,
            openAttachment = openAttachment,
            pickAttachment = { picker.launch() },
            insertReference = { attachment ->
                controller?.insertAtCursor(attachment.toMarkdownReference())
                currentOnEvent(StepNotesEvent.ToggleEdit(true))
            },
        )
    }
}

/**
 * Notes of a step: title with the edit toggle, then either the editor or the rendered Markdown.
 *
 * [title], [editorLabel] and [emptyText] are the host's own words — a place is annotated
 * differently from the leg that leads to it — and [testTags] names this host's nodes.
 */
@Composable
fun StepNotesSection(
    host: StepNotesHost,
    state: StepNotesUiState,
    title: String,
    editorLabel: String,
    emptyText: String,
    testTags: StepNotesTestTags,
    onEvent: (StepNotesEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.weight(1f),
            )
            if (state.noteInvalid) {
                Icon(
                    modifier = Modifier.testTag(testTags.noteAlert),
                    painter = painterResource(Res.drawable.error),
                    contentDescription = stringResource(Res.string.planning_detail_cd_note_invalid),
                    tint = MaterialTheme.colorScheme.error,
                )
            }
            IconButton(
                modifier = Modifier.testTag(testTags.editToggle),
                onClick = { onEvent(StepNotesEvent.ToggleEdit(!state.isEditing)) },
            ) {
                if (state.isEditing) {
                    Icon(
                        painter = painterResource(Res.drawable.check),
                        contentDescription = stringResource(Res.string.planning_detail_cd_done_note),
                    )
                } else {
                    Icon(
                        painter = painterResource(Res.drawable.edit),
                        contentDescription = stringResource(Res.string.planning_detail_cd_edit_note),
                    )
                }
            }
        }

        if (state.missingReferences.isNotEmpty()) {
            Text(
                modifier = Modifier.testTag(testTags.missingReferences),
                text = stringResource(Res.string.planning_detail_attachments_missing),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
            )
        }

        if (state.isEditing && host.controller != null) {
            MarkdownNoteEditor(
                controller = host.controller,
                label = editorLabel,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag(testTags.noteEditor),
            )
        } else if (state.note.isBlank()) {
            Text(
                modifier = Modifier.testTag(testTags.noteView),
                text = emptyText,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        } else {
            CompositionLocalProvider(LocalUriHandler provides host.uriHandler) {
                Markdown(
                    content = state.note,
                    imageTransformer = host.imageTransformer,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag(testTags.noteView),
                )
            }
        }
    }
}

/** Markdown snippet referencing the attachment: inline image, or link to the file. */
internal fun AttachmentUi.toMarkdownReference(): String = if (isImage) {
    AttachmentReference.imageMarkdown(relativePath, altText = originalName)
} else {
    AttachmentReference.fileMarkdown(relativePath, label = originalName)
}

//region Previews

@Preview(showBackground = true)
@Composable
private fun StepNotesSectionPreview(@PreviewParameter(StepNotesSectionPreviewParams::class) state: StepNotesUiState) =
    KTravelTheme {
        Surface {
            StepNotesSection(
                host = rememberStepNotesHost(
                    state = state,
                    snackbarHostState = remember { SnackbarHostState() },
                    onEvent = {},
                ),
                state = state,
                title = stringResource(Res.string.planning_detail_step_note_title),
                editorLabel = stringResource(Res.string.planning_detail_step_note_label),
                emptyText = stringResource(Res.string.planning_detail_step_note_empty),
                testTags = StepNotesTestTags(
                    noteView = "preview-note-view",
                    noteEditor = "preview-note-editor",
                    editToggle = "preview-edit-toggle",
                    noteAlert = "preview-note-alert",
                    missingReferences = "preview-missing-references",
                    attachments = "preview-attachments",
                    attachmentAdd = "preview-attachment-add",
                    attachmentItem = "preview-attachment-item",
                ),
                onEvent = {},
                modifier = Modifier.padding(16.dp),
            )
        }
    }

/**
 * The four shapes the section takes, which are the four branches it draws.
 *
 * The dangling reference comes with [StepNotesUiState.noteInvalid] on purpose: a note that stopped
 * parsing and a note pointing at a file that is gone are different failures, and the preview is
 * where their two warnings have to stay distinguishable from each other and from the title.
 */
internal class StepNotesSectionPreviewParams : PreviewParameterProvider<StepNotesUiState> {
    override val values = sequenceOf(
        StepNotesUiState(note = "## Opening hours\n\n- 09:00 - 23:00\n- Last lift at 22:30"),
        StepNotesUiState(),
        StepNotesUiState(
            note = "Bring the [ticket](ktravel://attachment/t1/s1/ticket.pdf).",
            noteInvalid = true,
            missingReferences = persistentListOf("t1/s1/ticket.pdf"),
        ),
        StepNotesUiState(note = "Ask at the desk whether the roof terrace is open", isEditing = true),
    )
}

//endregion Previews
