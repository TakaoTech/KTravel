package com.takaotech.ktravel.ui.plan.day.placestep

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewFontScale
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import androidx.compose.ui.unit.dp
import com.takaotech.ktravel.presentation.plan.StepUi
import com.takaotech.ktravel.presentation.plan.VisitScheduleUi
import com.takaotech.ktravel.presentation.plan.day.StepNotesEvent
import com.takaotech.ktravel.presentation.plan.day.StepNotesUiState
import com.takaotech.ktravel.ui.plan.day.attachment.ATTACHMENT_INVENTORY_PEEK_HEIGHT
import com.takaotech.ktravel.ui.plan.day.attachment.AttachmentInventorySection
import com.takaotech.ktravel.ui.plan.day.notes.StepNotesSection
import com.takaotech.ktravel.ui.plan.day.notes.rememberStepNotesHost
import com.takaotech.ktravel.ui.plan.day.placestep.component.PlaceMap
import com.takaotech.ktravel.ui.plan.day.placestep.component.ScheduleSection
import com.takaotech.ktravel.ui.shared.component.BackButton
import com.takaotech.ktravel.ui.theme.KTravelTheme
import kotlinx.collections.immutable.persistentListOf
import kotlinx.datetime.LocalTime
import ktravel.composeapp.generated.resources.Res
import ktravel.composeapp.generated.resources.planning_detail_step_note_empty
import ktravel.composeapp.generated.resources.planning_detail_step_note_label
import ktravel.composeapp.generated.resources.planning_detail_step_note_title
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun PlaceStepContent(
    place: StepUi.Place,
    notes: StepNotesUiState,
    onBack: () -> Unit,
    onNotesEvent: (StepNotesEvent) -> Unit,
    onSetStartTime: (LocalTime) -> Unit,
    onSetEndTime: (LocalTime) -> Unit,
    modifier: Modifier = Modifier,
) {
    val scaffoldState = rememberBottomSheetScaffoldState()
    val host = rememberStepNotesHost(
        state = notes,
        snackbarHostState = scaffoldState.snackbarHostState,
        onEvent = onNotesEvent,
    )

    BottomSheetScaffold(
        modifier = modifier,
        scaffoldState = scaffoldState,
        snackbarHost = {
            SnackbarHost(
                modifier = Modifier
                    .navigationBarsPadding(),
                hostState = it,
            )
        },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = place.name,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                },
                navigationIcon = {
                    BackButton(
                        onClick = onBack,
                        modifier = Modifier.testTag(PlaceStepTestTags.BACK_BUTTON),
                    )
                },
            )
        },
        sheetPeekHeight = ATTACHMENT_INVENTORY_PEEK_HEIGHT,
        sheetContent = {
            AttachmentInventorySection(
                modifier = Modifier
                    .navigationBarsPadding(),
                attachments = notes.attachments,
                resolveFile = notes.resolveFile,
                isEditing = notes.isEditing,
                testTags = PlaceStepTestTags.NOTES,
                onAdd = host.pickAttachment,
                onInsert = host.insertReference,
                onOpen = { attachment -> host.openAttachment(attachment.relativePath) },
                onRemove = { attachment -> onNotesEvent(StepNotesEvent.RemoveAttachment(attachment.id)) },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            PlaceMap(
                lat = place.lat,
                lng = place.lng,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp)
                    .testTag(PlaceStepTestTags.MAP),
            )

            ScheduleSection(
                schedule = place.schedule,
                onSetStartTime = onSetStartTime,
                onSetEndTime = onSetEndTime,
            )

            StepNotesSection(
                host = host,
                state = notes,
                title = stringResource(Res.string.planning_detail_step_note_title),
                editorLabel = stringResource(Res.string.planning_detail_step_note_label),
                emptyText = stringResource(Res.string.planning_detail_step_note_empty),
                testTags = PlaceStepTestTags.NOTES,
                onEvent = onNotesEvent,
            )

            Spacer(Modifier.height(16.dp))
        }
    }
}

//region Previews
@PreviewScreenSizes
@PreviewFontScale
@Composable
private fun PlaceStepContentPreview() = KTravelTheme {
    PlaceStepContent(
        place = StepUi.Place(
            name = "Tokyo Tower",
            lat = 35.6586,
            lng = 139.7454,
            schedule = VisitScheduleUi(
                startTime = LocalTime(9, 30),
                endTime = LocalTime(11, 0),
            ),
            note = "# Cose da vedere\n- Osservatorio principale\n- **Foto** al tramonto",
        ),
        notes = StepNotesUiState(note = "# Cose da vedere\n- Osservatorio principale\n- **Foto** al tramonto"),
        onBack = {},
        onNotesEvent = {},
        onSetStartTime = {},
        onSetEndTime = {},
    )
}

@PreviewScreenSizes
@PreviewFontScale
@Composable
private fun PlaceStepContentEmptyNotePreview() = KTravelTheme {
    PlaceStepContent(
        place = StepUi.Place(name = "Shibuya Crossing", lat = 35.6595, lng = 139.7005),
        notes = StepNotesUiState(
            noteInvalid = true,
            missingReferences = persistentListOf("t1/s1/missing.jpg"),
        ),
        onBack = {},
        onNotesEvent = {},
        onSetStartTime = {},
        onSetEndTime = {},
    )
}
//endregion Previews
