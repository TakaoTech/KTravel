package com.takaotech.ktravel.ui.planning.detail

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfoV2
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.navigationevent.NavigationEventInfo
import androidx.navigationevent.compose.NavigationBackHandler
import androidx.navigationevent.compose.rememberNavigationEventState
import androidx.window.core.layout.WindowSizeClass
import com.slack.circuit.foundation.CircuitContent
import com.slack.circuit.foundation.NavEvent
import com.takaotech.ktravel.presentation.planning.detail.PlacesBacklogScreen
import com.takaotech.ktravel.presentation.planning.detail.PlanningDetailEvent
import com.takaotech.ktravel.presentation.planning.detail.PlanningDetailUiState
import kotlinx.serialization.Serializable
import ktravel.composeapp.generated.resources.Res
import ktravel.composeapp.generated.resources.planning_detail_cd_close_backlog
import org.jetbrains.compose.resources.stringResource

@Serializable
data class PlanningDetailPageNavigation(val id: String)

/**
 * Whether the backlog pane is showing.
 *
 * The toolbar toggle lives in the itinerary, a sibling `CircuitContent` whose `Ui` takes only its
 * own state: this is how the page tells it to render the toggle as active. Read by [StepsPaneUi],
 * `false` anywhere the itinerary is mounted without a pane beside it.
 */
internal val LocalBacklogPaneOpen = compositionLocalOf { false }

internal object PlanningDetailTestTags {
    const val ITINERARY = "planning_detail_itinerary"
    const val BACKLOG_PANE = "planning_detail_backlog_pane"
    const val SCRIM = "planning_detail_scrim"
}

// Backlog pane sizing, from the design: the pane hugs the right edge and the itinerary is inset by
// the pane plus a gutter, so the two never touch.
private val ExpandedPaneWidth = 300.dp
private val MediumPaneWidth = 284.dp
private val CompactPaneWidth = 296.dp
private const val COMPACT_PANE_MAX_WIDTH_FRACTION = 0.92f
private val PaneGutter = 12.dp
private val PaneShape = RoundedCornerShape(topStart = 28.dp)
private val PaneElevation = 8.dp
private const val SCRIM_ALPHA = 0.32f

// 300ms on the emphasized curve, as the design animates the pane.
private const val PANE_ANIMATION_MILLIS = 300
private val PaneEasing = CubicBezierEasing(0.2f, 0f, 0f, 1f)

/**
 * Pagina del dettaglio giorno: l'itinerario occupa la pagina e il backlog entra da destra come
 * pannello laterale, secondo il design.
 *
 * Il pulsante in toolbar dell'itinerario emette [NavEvent.GoTo] verso [PlacesBacklogScreen]: qui
 * viene tradotto in un toggle del pannello, così la stessa icona lo apre e lo chiude. La `X` del
 * pannello emette [NavEvent.Pop]. Ogni altro [NavEvent] dei figli risale al presenter padre tramite
 * [PlanningDetailEvent.ChildNav].
 */
@Composable
fun PlanningDetailPage(state: PlanningDetailUiState, modifier: Modifier = Modifier) {
    val expanded = currentWindowAdaptiveInfoV2().windowSizeClass
        .isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_EXPANDED_LOWER_BOUND)

    // Expanded showed both panes side by side before this layout existed: keep landing on that.
    var backlogOpen by rememberSaveable { mutableStateOf(expanded) }

    PlanningDetailLayout(
        backlogOpen = backlogOpen,
        onBacklogDismiss = { backlogOpen = false },
        modifier = modifier,
        itinerary = {
            CompositionLocalProvider(LocalBacklogPaneOpen provides backlogOpen) {
                CircuitContent(
                    modifier = Modifier.fillMaxSize(),
                    screen = state.stepsPaneScreen,
                    onNavEvent = { event ->
                        if (event is NavEvent.GoTo && event.screen is PlacesBacklogScreen) {
                            backlogOpen = !backlogOpen
                        } else {
                            state.eventSink(PlanningDetailEvent.ChildNav(event))
                        }
                    },
                )
            }
        },
        backlog = {
            CircuitContent(
                modifier = Modifier.fillMaxSize(),
                screen = state.placesBacklogScreen,
                onNavEvent = { event ->
                    if (event is NavEvent.Pop) {
                        backlogOpen = false
                    } else {
                        state.eventSink(PlanningDetailEvent.ChildNav(event))
                    }
                },
            )
        },
    )
}

/**
 * Layout adattivo della pagina, senza Circuit: [itinerary] riempie la pagina e [backlog] entra da
 * destra.
 *
 * Da medium in su il pannello affianca l'itinerario, che si restringe della larghezza del pannello
 * più [PaneGutter]; su compatto si sovrappone sopra uno scrim che lo congeda al tocco, largo al
 * più [COMPACT_PANE_MAX_WIDTH_FRACTION] della finestra così che l'itinerario resti intravisto sotto.
 */
@Composable
internal fun PlanningDetailLayout(
    backlogOpen: Boolean,
    onBacklogDismiss: () -> Unit,
    itinerary: @Composable () -> Unit,
    backlog: @Composable () -> Unit,
    modifier: Modifier = Modifier,
) {
    val windowSizeClass = currentWindowAdaptiveInfoV2().windowSizeClass
    val besideContent =
        windowSizeClass.isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_MEDIUM_LOWER_BOUND)
    val expanded =
        windowSizeClass.isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_EXPANDED_LOWER_BOUND)

    // On compact the pane is an overlay, so back closes it; beside the content there is nothing to
    // dismiss and back belongs to the day itself.
    NavigationBackHandler(
        state = rememberNavigationEventState(NavigationEventInfo.None),
        isBackEnabled = backlogOpen && !besideContent,
        onBackCompleted = onBacklogDismiss,
    )

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val paneWidth = when {
            expanded -> ExpandedPaneWidth
            besideContent -> MediumPaneWidth
            else -> minOf(CompactPaneWidth, maxWidth * COMPACT_PANE_MAX_WIDTH_FRACTION)
        }
        val itineraryInset by animateDpAsState(
            targetValue = if (backlogOpen && besideContent) paneWidth + PaneGutter else 0.dp,
            animationSpec = tween(PANE_ANIMATION_MILLIS, easing = PaneEasing),
            label = "backlogInset",
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(end = itineraryInset)
                .testTag(PlanningDetailTestTags.ITINERARY),
        ) {
            itinerary()
        }

        if (!besideContent) {
            val dismissLabel = stringResource(Res.string.planning_detail_cd_close_backlog)
            AnimatedVisibility(
                visible = backlogOpen,
                enter = fadeIn(tween(PANE_ANIMATION_MILLIS, easing = PaneEasing)),
                exit = fadeOut(tween(PANE_ANIMATION_MILLIS, easing = PaneEasing)),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.scrim.copy(alpha = SCRIM_ALPHA))
                        .clickable(
                            interactionSource = null,
                            indication = null,
                            onClickLabel = dismissLabel,
                            onClick = onBacklogDismiss,
                        )
                        .testTag(PlanningDetailTestTags.SCRIM),
                )
            }
        }

        AnimatedVisibility(
            visible = backlogOpen,
            modifier = Modifier.align(Alignment.CenterEnd),
            enter = slideInHorizontally(tween(PANE_ANIMATION_MILLIS, easing = PaneEasing)) { it },
            exit = slideOutHorizontally(tween(PANE_ANIMATION_MILLIS, easing = PaneEasing)) { it },
        ) {
            Box(
                modifier = Modifier
                    .width(paneWidth)
                    .fillMaxHeight()
                    .shadow(PaneElevation, PaneShape)
                    .testTag(PlanningDetailTestTags.BACKLOG_PANE),
            ) {
                backlog()
            }
        }
    }
}
