package com.takaotech.ktravel.ui.planning.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mikepenz.markdown.m3.Markdown
import com.slack.circuit.codegen.annotations.CircuitInject
import com.takaotech.ktravel.di.AppScope
import com.takaotech.ktravel.presentation.planning.StepUi
import com.takaotech.ktravel.presentation.planning.detail.StepDetailEvent
import com.takaotech.ktravel.presentation.planning.detail.StepDetailScreen
import com.takaotech.ktravel.presentation.planning.detail.StepDetailUiState
import com.takaotech.os_map.LatLng
import com.takaotech.os_map.RouteMap
import ktravel.composeapp.generated.resources.Res
import ktravel.composeapp.generated.resources.arrow_back
import ktravel.composeapp.generated.resources.check
import ktravel.composeapp.generated.resources.edit
import ktravel.composeapp.generated.resources.error
import ktravel.composeapp.generated.resources.planning_detail_cd_back
import ktravel.composeapp.generated.resources.planning_detail_cd_done_note
import ktravel.composeapp.generated.resources.planning_detail_cd_edit_note
import ktravel.composeapp.generated.resources.planning_detail_cd_note_invalid
import ktravel.composeapp.generated.resources.planning_detail_step_note_empty
import ktravel.composeapp.generated.resources.planning_detail_step_note_label
import ktravel.composeapp.generated.resources.planning_detail_step_note_title
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

internal object StepDetailTestTags {
    const val MAP = "step_detail_map"
    const val NOTE_VIEW = "step_detail_note_view"
    const val NOTE_EDITOR = "step_detail_note_editor"
    const val EDIT_TOGGLE = "step_detail_edit_toggle"
    const val NOTE_ALERT = "step_detail_note_alert"
    const val BACK_BUTTON = "step_detail_back"
}

/**
 * `Ui` Circuit del dettaglio step (registrata via Metro `@CircuitInject`). Sviluppa la grafica per
 * [StepUi.Place]: nome, mappa con marker e note in Markdown (editor Hyphen + renderer mikepenz).
 */
@CircuitInject(StepDetailScreen::class, AppScope::class)
@Composable
fun StepDetailUi(state: StepDetailUiState, modifier: Modifier = Modifier) {
    val sink = state.eventSink
    val place = state.place
    if (place == null) {
        StepDetailLoading(modifier = modifier, onBack = { sink(StepDetailEvent.NavigateBack) })
    } else {
        StepDetailPlaceContent(
            place = place,
            isEditing = state.isEditing,
            noteInvalid = state.noteInvalid,
            modifier = modifier,
            onBack = { sink(StepDetailEvent.NavigateBack) },
            onToggleEdit = { sink(StepDetailEvent.ToggleEdit(it)) },
            onNoteChanged = { sink(StepDetailEvent.NoteChanged(it)) }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun StepDetailLoading(onBack: () -> Unit, modifier: Modifier = Modifier) {
    Scaffold(
        modifier = modifier,
        topBar = { TopAppBar(title = {}, navigationIcon = { BackButton(onBack) }) }
    ) { padding ->
        Box(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun StepDetailPlaceContent(
    place: StepUi.Place,
    isEditing: Boolean,
    noteInvalid: Boolean,
    onBack: () -> Unit,
    onToggleEdit: (Boolean) -> Unit,
    onNoteChanged: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = place.name,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                navigationIcon = {
                    BackButton(onClick = onBack)
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            RouteMap(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp)
                    .testTag(StepDetailTestTags.MAP),
                enable = true,
                marker = LatLng(lat = place.lat, lng = place.lng)
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = stringResource(Res.string.planning_detail_step_note_title),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f)
                )
                if (noteInvalid) {
                    Icon(
                        modifier = Modifier.testTag(StepDetailTestTags.NOTE_ALERT),
                        painter = painterResource(Res.drawable.error),
                        contentDescription = stringResource(Res.string.planning_detail_cd_note_invalid),
                        tint = MaterialTheme.colorScheme.error
                    )
                }
                IconButton(
                    modifier = Modifier.testTag(StepDetailTestTags.EDIT_TOGGLE),
                    onClick = { onToggleEdit(!isEditing) }
                ) {
                    if (isEditing) {
                        Icon(
                            painter = painterResource(Res.drawable.check),
                            contentDescription = stringResource(Res.string.planning_detail_cd_done_note)
                        )
                    } else {
                        Icon(
                            painter = painterResource(Res.drawable.edit),
                            contentDescription = stringResource(Res.string.planning_detail_cd_edit_note)
                        )
                    }
                }
            }

            if (isEditing) {
                MarkdownNoteEditor(
                    initialValue = place.note,
                    onValueChange = onNoteChanged,
                    label = stringResource(Res.string.planning_detail_step_note_label),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag(StepDetailTestTags.NOTE_EDITOR)
                )
            } else {
                if (place.note.isBlank()) {
                    Text(
                        modifier = Modifier.testTag(StepDetailTestTags.NOTE_VIEW),
                        text = stringResource(Res.string.planning_detail_step_note_empty),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    Markdown(
                        content = place.note,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag(StepDetailTestTags.NOTE_VIEW)
                    )
                }
            }
        }
    }
}

@Composable
private fun BackButton(onClick: () -> Unit) {
    IconButton(
        modifier = Modifier.testTag(StepDetailTestTags.BACK_BUTTON),
        onClick = onClick
    ) {
        Icon(
            painter = painterResource(Res.drawable.arrow_back),
            contentDescription = stringResource(Res.string.planning_detail_cd_back)
        )
    }
}

@Preview
@Composable
private fun StepDetailPlaceContentPreview() {
    StepDetailPlaceContent(
        place = StepUi.Place(
            name = "Tokyo Tower",
            lat = 35.6586,
            lng = 139.7454,
            note = "# Cose da vedere\n- Osservatorio principale\n- **Foto** al tramonto"
        ),
        isEditing = false,
        noteInvalid = false,
        onBack = {},
        onToggleEdit = {},
        onNoteChanged = {}
    )
}

@Preview
@Composable
private fun StepDetailPlaceContentEmptyNotePreview() {
    StepDetailPlaceContent(
        place = StepUi.Place(name = "Shibuya Crossing", lat = 35.6595, lng = 139.7005),
        isEditing = false,
        noteInvalid = true,
        onBack = {},
        onToggleEdit = {},
        onNoteChanged = {}
    )
}
