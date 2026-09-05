package com.takaotech.ktravel.ui.plan.day.transportstep.component

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import com.takaotech.gunzou.api.geometry.PolylineEncoderDecoder
import com.takaotech.ktravel.presentation.plan.day.StepNotesEvent
import com.takaotech.ktravel.presentation.plan.day.StepNotesUiState
import com.takaotech.ktravel.ui.plan.day.notes.StepNotesHost
import com.takaotech.ktravel.ui.plan.day.notes.StepNotesSection
import com.takaotech.ktravel.ui.plan.day.notes.rememberStepNotesHost
import com.takaotech.ktravel.ui.plan.day.transportstep.TransportStepTestTags
import com.takaotech.ktravel.ui.theme.KTravelTheme
import ktravel.composeapp.generated.resources.Res
import ktravel.composeapp.generated.resources.transport_detail_note_empty
import ktravel.composeapp.generated.resources.transport_detail_note_label
import ktravel.composeapp.generated.resources.transport_detail_note_title
import org.jetbrains.compose.resources.stringResource
import com.takaotech.ktravel.presentation.plan.day.TransportStepUi as TransportStepUiModel

@Composable
internal fun TransportStepPanel(
    contentPadding: PaddingValues,
    transport: TransportStepUiModel,
    host: StepNotesHost,
    notes: StepNotesUiState,
    onPointClick: (PolylineEncoderDecoder.LatLngZ) -> Unit,
    onNotesEvent: (StepNotesEvent) -> Unit,
    modifier: Modifier = Modifier,
    map: (@Composable LazyItemScope.() -> Unit)?,
) {
    var stepsExpanded by rememberSaveable { mutableStateOf(true) }

    LazyColumn(
        modifier = modifier,
        contentPadding = contentPadding,
    ) {
        if (map != null) {
            item(contentType = "map", content = map)
        }

        item(contentType = "segmentTransport") {
            SegmentHead(
                transport = transport,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            )
        }
        item(contentType = "transportMetrics") {
            Metrics(
                transport = transport,
                modifier = Modifier.padding(horizontal = 16.dp),
            )
        }
        item(contentType = "notes") {
            StepNotesSection(
                host = host,
                state = notes,
                title = stringResource(Res.string.transport_detail_note_title),
                editorLabel = stringResource(Res.string.transport_detail_note_label),
                emptyText = stringResource(Res.string.transport_detail_note_empty),
                testTags = TransportStepTestTags.NOTES,
                onEvent = onNotesEvent,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp),
            )
        }
        stepsSection(
            answer = transport.answer,
            expanded = stepsExpanded,
            onToggleExpanded = { stepsExpanded = !stepsExpanded },
            onPointClick = onPointClick,
        )
        @Suppress("LazyListContentType")
        item { Spacer(Modifier.height(24.dp)) }
    }
}

//region Previews

@Preview(showBackground = true)
@Composable
private fun TransportStepPanelPreview(
    @PreviewParameter(TransportStepPanelPreviewParams::class) state: TransportStepPanelPreviewState,
) = KTravelTheme {
    Surface {
        TransportStepPanel(
            contentPadding = PaddingValues(vertical = 12.dp),
            transport = state.transport,
            host = rememberStepNotesHost(
                state = state.notes,
                snackbarHostState = remember { SnackbarHostState() },
                onEvent = {},
            ),
            notes = state.notes,
            onPointClick = {},
            onNotesEvent = {},
            map = if (state.withMap) {
                { PreviewMapSlot() }
            } else {
                null
            },
        )
    }
}

/** Where the caller's map goes: the panel only leaves room for it, and never draws one. */
@Composable
private fun LazyItemScope.PreviewMapSlot() = Surface(
    modifier = Modifier
        .fillMaxWidth()
        .height(160.dp),
    color = MaterialTheme.colorScheme.surfaceContainerHigh,
) {}

/** A leg, the notes written on it, and whether the caller gave the panel a map to place. */
internal data class TransportStepPanelPreviewState(
    val transport: TransportStepUiModel,
    val notes: StepNotesUiState,
    val withMap: Boolean,
)

/**
 * The panel with a map above it, and without one.
 *
 * The map is the caller's, so its absence is not an empty box but one item fewer in the list: what
 * has to hold in both is the order of everything under it. The empty note comes with the mapless
 * one because a leg nobody annotated is the common case, and it is the shortest the panel ever is.
 */
internal class TransportStepPanelPreviewParams : PreviewParameterProvider<TransportStepPanelPreviewState> {
    override val values = sequenceOf(
        TransportStepPanelPreviewState(
            transport = previewRoadStep(),
            notes = StepNotesUiState(note = "Toll booth before the ring road, keep the ticket."),
            withMap = true,
        ),
        TransportStepPanelPreviewState(
            transport = previewTransitStep(),
            notes = StepNotesUiState(),
            withMap = false,
        ),
    )
}
//endregion Previews
