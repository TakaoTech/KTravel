package com.takaotech.ktravel.ui.plan.day

import androidx.compose.animation.AnimatedVisibility
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
import com.takaotech.ktravel.presentation.plan.day.DayDetailEvent
import com.takaotech.ktravel.presentation.plan.day.DayDetailUiState
import com.takaotech.ktravel.presentation.plan.day.PlacesBacklogScreen
import ktravel.composeapp.generated.resources.Res
import ktravel.composeapp.generated.resources.planning_detail_cd_close_backlog
import org.jetbrains.compose.resources.stringResource

/**
 * Whether the backlog pane is showing.
 *
 * The toolbar toggle lives in the itinerary, a sibling `CircuitContent` whose `Ui` takes only its
 * own state: this is how the page tells it to render the toggle as active. Read by [StepsPaneUi],
 * `false` anywhere the itinerary is mounted without a pane beside it.
 */
internal val LocalBacklogPaneOpen = compositionLocalOf { false }

/**
 * One day, as a page: the itinerary fills it and the backlog comes in from the right.
 *
 * The backlog is a pane rather than a destination, so the events its neighbours raise about it are
 * answered here instead of travelling: the itinerary's toolbar button asks to go to
 * [PlacesBacklogScreen] and the pane's close button asks to pop, and both are turned into a toggle
 * of the same pane — which is what lets one icon open and close it. Anything else a child raises
 * carries on to the presenter as [DayDetailEvent.ChildNav].
 */
@Composable
fun DayDetailContent(state: DayDetailUiState, modifier: Modifier = Modifier) {
    val expanded = currentWindowAdaptiveInfoV2().windowSizeClass
        .isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_EXPANDED_LOWER_BOUND)

    // Expanded showed both panes side by side before this layout existed: keep landing on that.
    var backlogOpen by rememberSaveable { mutableStateOf(expanded) }

    DayDetailLayout(
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
                            state.eventSink(DayDetailEvent.ChildNav(event))
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
                        state.eventSink(DayDetailEvent.ChildNav(event))
                    }
                },
            )
        },
    )
}

/**
 * The adaptive half of the page, with no Circuit in it: [itinerary] fills the page and [backlog]
 * comes in from the right.
 *
 * From medium widths up the pane sits beside the itinerary, which gives up the pane's width plus
 * [PaneGutter] to make room. On a compact width it lies over the itinerary instead, on a scrim that
 * dismisses it when touched, and takes at most [COMPACT_PANE_MAX_WIDTH_FRACTION] of the window so
 * that what it covers stays visible underneath.
 */
@Composable
internal fun DayDetailLayout(
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
                .testTag(DayDetailTestTags.ITINERARY),
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
                        .testTag(DayDetailTestTags.SCRIM),
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
                    .testTag(DayDetailTestTags.BACKLOG_PANE),
            ) {
                backlog()
            }
        }
    }
}
