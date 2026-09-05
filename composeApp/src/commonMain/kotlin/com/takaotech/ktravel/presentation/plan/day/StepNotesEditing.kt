package com.takaotech.ktravel.presentation.plan.day

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import com.mikepenz.markdown.model.State
import com.mikepenz.markdown.model.parseMarkdown
import com.takaotech.ktravel.domain.model.AttachmentReference
import com.takaotech.ktravel.domain.repository.TravelPlanRepository
import com.takaotech.ktravel.presentation.plan.AttachmentUi
import io.github.vinceglb.filekit.PlatformFile
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.io.files.Path

/**
 * Notes and file inventory of a step, in the shape every screen that hosts them needs.
 *
 * A fragment of a screen's state rather than a state of its own: a place detail and a transport
 * detail both carry one, alongside whatever else only they show. That is what keeps the two screens
 * from each growing their own copy of the same six fields.
 */
@Immutable
data class StepNotesUiState(
    /** Markdown as it is saved in the plan. */
    val note: String = "",
    /** File inventory of the step. */
    val attachments: PersistentList<AttachmentUi> = persistentListOf(),
    /** True while the Markdown editor is open, false in read-only rendering. */
    val isEditing: Boolean = false,
    /** True when the last save attempt failed because the Markdown does not parse. */
    val noteInvalid: Boolean = false,
    /**
     * Relative paths referenced by the Markdown but absent from the inventory ("dangling"): empty
     * means consistent.
     */
    val missingReferences: ImmutableList<String> = persistentListOf(),
    /** Resolves an inventory relative path into the actual file, for rendering and opening. */
    val resolveFile: (String) -> PlatformFile = { PlatformFile(Path(it)) },
)

/** What a host screen forwards to [StepNotesEditing] when the user acts on notes or attachments. */
sealed interface StepNotesEvent {

    /** Enters or leaves the Markdown editor. */
    data class ToggleEdit(val editing: Boolean) : StepNotesEvent

    /** New Markdown content, saved automatically when valid. */
    data class NoteChanged(val note: String) : StepNotesEvent

    /** Uploads a file into the step's inventory. */
    data class AddAttachment(val file: PlatformFile) : StepNotesEvent

    /** Removes a file from the step's inventory. */
    data class RemoveAttachment(val attachmentId: String) : StepNotesEvent
}

/**
 * The notes half of a detail presenter: the state to publish and the sink to feed it.
 *
 * Held by the presenter of whichever screen hosts the notes, which puts [state] into its own
 * `CircuitUiState` and routes its note events here.
 */
@Stable
class StepNotesEditing internal constructor(val state: StepNotesUiState, private val sink: (StepNotesEvent) -> Unit) {

    operator fun invoke(event: StepNotesEvent) = sink(event)
}

/**
 * Drives the notes of one step: autosave, validation and dangling-reference detection.
 *
 * The note is saved a second after the last keystroke and **only when the Markdown parses**;
 * otherwise [StepNotesUiState.noteInvalid] reports it and nothing is written, because a note that
 * cannot be rendered is worse in the plan than out of it. Leaving the editor flushes what is
 * pending without waiting for the debounce.
 *
 * [savedNote] and [attachments] come from the plan's flow, so an edit made elsewhere still lands
 * here; what the user is typing lives in this function and is not published until it is saved.
 */
@Composable
fun rememberStepNotesEditing(
    repository: TravelPlanRepository,
    resolveFile: (String) -> PlatformFile,
    dayId: String,
    stepId: String,
    savedNote: String,
    attachments: PersistentList<AttachmentUi>,
): StepNotesEditing {
    val scope = rememberCoroutineScope()

    var isEditing by rememberSaveable(stepId) { mutableStateOf(false) }
    var noteInvalid by rememberSaveable(stepId) { mutableStateOf(false) }
    // Last note typed and not yet saved; null = no pending edit.
    var pendingNote by remember(stepId) { mutableStateOf<String?>(null) }

    LaunchedEffect(pendingNote) {
        val note = pendingNote ?: return@LaunchedEffect
        if (note == savedNote) return@LaunchedEffect
        // TODO Check this debounce
        delay(NOTE_SAVE_DEBOUNCE_MS)
        noteInvalid = !repository.persistNoteIfValid(dayId, stepId, note)
    }

    val currentNote = pendingNote ?: savedNote
    val inventoryPaths = remember(attachments) { attachments.map { it.relativePath } }
    val missingReferences = remember(currentNote, inventoryPaths) {
        AttachmentReference.missingReferences(currentNote, inventoryPaths).toImmutableList()
    }

    val state = StepNotesUiState(
        note = savedNote,
        attachments = attachments,
        isEditing = isEditing,
        noteInvalid = noteInvalid,
        missingReferences = missingReferences,
        resolveFile = resolveFile,
    )

    return remember(state) {
        StepNotesEditing(state) { event ->
            when (event) {
                is StepNotesEvent.ToggleEdit -> {
                    isEditing = event.editing
                    // Leaving the editor: flush the pending note without waiting for the debounce.
                    if (!event.editing) {
                        scope.launch {
                            noteInvalid = repository.flush(dayId, stepId, pendingNote, savedNote) ?: noteInvalid
                        }
                    }
                }

                is StepNotesEvent.NoteChanged -> {
                    pendingNote = event.note
                }

                is StepNotesEvent.AddAttachment -> scope.launch {
                    repository.addAttachment(dayId, stepId, event.file)
                }

                is StepNotesEvent.RemoveAttachment -> scope.launch {
                    repository.removeAttachment(dayId, stepId, event.attachmentId)
                }
            }
        }
    }
}

/**
 * Writes [pending] when there is something to write, and answers whether it was rejected.
 *
 * Null means nothing was attempted — no pending edit, or one that matches what is already saved —
 * which is not the same as a successful save and must not clear a standing error.
 */
private suspend fun TravelPlanRepository.flush(
    dayId: String,
    stepId: String,
    pending: String?,
    saved: String,
): Boolean? {
    if (pending == null || pending == saved) return null
    return !persistNoteIfValid(dayId, stepId, pending)
}

private const val NOTE_SAVE_DEBOUNCE_MS = 1000L

/**
 * Persists the note only when the Markdown is valid. Returns `true` when saved, `false` when the
 * Markdown does not parse (note not persisted).
 */
private suspend fun TravelPlanRepository.persistNoteIfValid(dayId: String, stepId: String, note: String): Boolean =
    if (isValidMarkdown(note)) {
        updateStepNote(dayId, stepId, note)
        true
    } else {
        false
    }

/** Validates the Markdown by attempting to parse it (mikepenz GFM parser). */
internal fun isValidMarkdown(text: String): Boolean =
    runCatching { parseMarkdown(text) is State.Success }.getOrDefault(false)
