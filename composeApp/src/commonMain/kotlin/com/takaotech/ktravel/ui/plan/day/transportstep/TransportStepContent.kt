@file:OptIn(ExperimentalMaterial3Api::class)

package com.takaotech.ktravel.ui.plan.day.transportstep

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfoV2
import androidx.compose.material3.adaptive.layout.AnimatedPane
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import androidx.compose.ui.unit.dp
import androidx.window.core.layout.WindowSizeClass
import com.takaotech.gunzou.api.geometry.PolylineEncoderDecoder
import com.takaotech.ktravel.domain.model.TransportType
import com.takaotech.ktravel.domain.routing.model.RouteSummary
import com.takaotech.ktravel.domain.routing.model.RoutingRoute
import com.takaotech.ktravel.domain.routing.model.RoutingSection
import com.takaotech.ktravel.domain.routing.model.TransitJourney
import com.takaotech.ktravel.domain.routing.model.TransitLine
import com.takaotech.ktravel.domain.routing.model.TransitStep
import com.takaotech.ktravel.domain.routing.model.TransportAnswer
import com.takaotech.ktravel.presentation.plan.day.StepNotesEvent
import com.takaotech.ktravel.presentation.plan.day.StepNotesUiState
import com.takaotech.ktravel.ui.plan.day.attachment.ATTACHMENT_INVENTORY_PEEK_HEIGHT
import com.takaotech.ktravel.ui.plan.day.attachment.AttachmentInventorySection
import com.takaotech.ktravel.ui.plan.day.notes.rememberStepNotesHost
import com.takaotech.ktravel.ui.plan.day.transportstep.component.TransportStepPanel
import com.takaotech.ktravel.ui.shared.component.BackButton
import com.takaotech.ktravel.ui.shared.component.PanelHorizontalDivided
import com.takaotech.ktravel.ui.theme.KTravelTheme
import io.nacular.measured.units.Length
import io.nacular.measured.units.times
import ktravel.composeapp.generated.resources.Res
import ktravel.composeapp.generated.resources.edit
import ktravel.composeapp.generated.resources.map
import ktravel.composeapp.generated.resources.transport_detail_cd_recalculate
import ktravel.composeapp.generated.resources.transport_detail_title
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import kotlin.time.Duration.Companion.seconds
import com.takaotech.ktravel.presentation.plan.day.TransportStepUi as TransportStepUiModel

/**
 * Stateless content of the screen.
 *
 * Map and panel sit side by side on a wide window and stacked on a narrow one, and the panel is a
 * single lazy list: the way through can be hundreds of rows, and it has to scroll with the metrics
 * above it rather than inside a box of its own.
 */
@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
internal fun TransportStepContent(
    transport: TransportStepUiModel,
    notes: StepNotesUiState,
    canRecalculate: Boolean,
    onBack: () -> Unit,
    onRecalculate: () -> Unit,
    onNotesEvent: (StepNotesEvent) -> Unit,
    modifier: Modifier = Modifier,
    onPointClick: (PolylineEncoderDecoder.LatLngZ) -> Unit = {},
    map: @Composable (Modifier) -> Unit = {},
) {
    val scaffoldState = rememberBottomSheetScaffoldState()
    val host = rememberStepNotesHost(
        state = notes,
        snackbarHostState = scaffoldState.snackbarHostState,
        onEvent = onNotesEvent,
    )

    val wide = currentWindowAdaptiveInfoV2().windowSizeClass
        .isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_EXPANDED_LOWER_BOUND)

    BottomSheetScaffold(
        modifier = modifier,
        scaffoldState = scaffoldState,
        snackbarHost = {
            SnackbarHost(
                modifier = Modifier.navigationBarsPadding(),
                hostState = it,
            )
        },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(Res.string.transport_detail_title),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                },
                navigationIcon = {
                    BackButton(
                        onClick = onBack,
                        modifier = Modifier.testTag(TransportStepTestTags.BACK_BUTTON),
                    )
                },
                actions = {
                    IconButton(
                        modifier = Modifier.testTag(TransportStepTestTags.RECALCULATE),
                        enabled = canRecalculate,
                        onClick = onRecalculate,
                    ) {
                        Icon(
                            painter = painterResource(Res.drawable.edit),
                            contentDescription = stringResource(Res.string.transport_detail_cd_recalculate),
                        )
                    }
                },
            )
        },
        sheetPeekHeight = ATTACHMENT_INVENTORY_PEEK_HEIGHT,
        sheetContent = {
            AttachmentInventorySection(
                modifier = Modifier.navigationBarsPadding(),
                attachments = notes.attachments,
                resolveFile = notes.resolveFile,
                isEditing = notes.isEditing,
                testTags = TransportStepTestTags.NOTES,
                onAdd = host.pickAttachment,
                onInsert = host.insertReference,
                onOpen = { attachment -> host.openAttachment(attachment.relativePath) },
                onRemove = { attachment -> onNotesEvent(StepNotesEvent.RemoveAttachment(attachment.id)) },
            )
        },
    ) { padding ->
        if (wide) {
            // TODO To be explore if this graphic is okay
            PanelHorizontalDivided(
                modifier = Modifier
                    .padding(top = padding.calculateTopPadding()),
                mainPane = {
                    AnimatedPane {
                        TransportStepPanel(
                            modifier = Modifier
                                .fillMaxHeight(),
                            contentPadding = padding,
                            transport = transport,
                            host = host,
                            notes = notes,
                            onPointClick = onPointClick,
                            onNotesEvent = onNotesEvent,
                            map = null,
                        )
                    }
                },
                supportingPane = {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight(),
                    ) { map(Modifier.fillMaxSize()) }
                },
            )
        } else {
            TransportStepPanel(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = padding.calculateTopPadding()),
                contentPadding = padding,
                transport = transport,
                host = host,
                notes = notes,
                onPointClick = onPointClick,
                onNotesEvent = onNotesEvent,
                map = {
                    map(
                        Modifier
                            .fillMaxWidth()
                            .height(COMPACT_MAP_HEIGHT)
                            .padding(horizontal = 16.dp)
                            .clip(RoundedCornerShape(16.dp)),
                    )
                },
            )
        }
    }
}

//region Previews
@PreviewScreenSizes
@Composable
private fun TransportStepRoadPreview() = KTravelTheme {
    TransportStepContent(
        transport = TransportStepUiModel(
            type = TransportType.CAR,
            fromName = "Binasco",
            toName = "Assago",
            answer = TransportAnswer.Routing(
                RoutingRoute(
                    summary = RouteSummary(849.seconds, 10400.0 * Length.meters),
                    sections = listOf(
                        RoutingSection(
                            summary = RouteSummary(849.seconds, 10400.0 * Length.meters),
                            mode = "car",
                        ),
                    ),
                ),
            ),
            totalDuration = 849.seconds,
            totalDistance = 10400.0 * Length.meters,
        ),
        notes = StepNotesUiState(note = "Pieno prima della A7, sosta pranzo a Binasco."),
        canRecalculate = true,
        onBack = {},
        onRecalculate = {},
        onNotesEvent = {},
    )
}

@PreviewScreenSizes
@Composable
private fun TransportStepTransitPreview() = KTravelTheme {
    TransportStepContent(
        transport = TransportStepUiModel(
            type = TransportType.TRAIN,
            fromName = "Duomo",
            toName = "Sesto",
            answer = TransportAnswer.Transit(
                TransitJourney(
                    summary = RouteSummary(1800.seconds, 9000.0 * Length.meters),
                    steps = listOf(
                        TransitStep.Walk(
                            summary = RouteSummary(
                                480.seconds,
                                600.0 * Length.meters,
                            ),
                        ),
                        TransitStep.Ride(
                            summary = RouteSummary(1320.seconds, 8400.0 * Length.meters),
                            line = TransitLine(mode = "SUBWAY", name = "M1", color = "#FF0000"),
                        ),
                    ),
                ),
            ),
            totalDuration = 1800.seconds,
            totalDistance = 9000.0 * Length.meters,
        ),
        notes = StepNotesUiState(),
        canRecalculate = false,
        onBack = {},
        onRecalculate = {},
        onNotesEvent = {},
    )
}
//endregion Previews
