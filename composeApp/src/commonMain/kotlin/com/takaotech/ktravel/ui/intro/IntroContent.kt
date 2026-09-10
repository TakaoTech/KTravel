package com.takaotech.ktravel.ui.intro

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.BottomAppBarDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import androidx.compose.ui.unit.dp
import androidx.navigationevent.NavigationEventInfo
import androidx.navigationevent.compose.NavigationBackHandler
import androidx.navigationevent.compose.rememberNavigationEventState
import com.takaotech.ktravel.core.telemetry.TelemetryConsent
import com.takaotech.ktravel.domain.staticflows.IntroStep
import com.takaotech.ktravel.presentation.intro.IntroEvent
import com.takaotech.ktravel.presentation.intro.IntroUiState
import com.takaotech.ktravel.ui.theme.KTravelTheme
import kotlinx.coroutines.launch
import ktravel.composeapp.generated.resources.Res
import ktravel.composeapp.generated.resources.arrow_back
import ktravel.composeapp.generated.resources.arrow_right_alt
import ktravel.composeapp.generated.resources.flight
import ktravel.composeapp.generated.resources.intro_back
import ktravel.composeapp.generated.resources.intro_cd_loading
import ktravel.composeapp.generated.resources.intro_decision_answer_hint
import ktravel.composeapp.generated.resources.intro_finish
import ktravel.composeapp.generated.resources.intro_next
import ktravel.composeapp.generated.resources.intro_privacy_acknowledge_hint
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import kotlin.math.abs

/** How thick one segment of the page indicator is drawn. */
private val INDICATOR_SEGMENT_HEIGHT = 4.dp

/** How far apart two segments of the page indicator sit. */
private val INDICATOR_SEGMENT_SPACING = 6.dp

/**
 * How much a step already behind the reader fades.
 *
 * A passed step keeps the accent — it is ground already covered, not ground still ahead — but at
 * less than full strength, so the segment the reader is on stays the one the eye lands on.
 */
private const val INDICATOR_PAST_ALPHA = 0.45f

/** The frame the introduction is drawn in: the counter above, the bar below. */
private val FRAME_HORIZONTAL_PADDING = 24.dp
private val FRAME_VERTICAL_PADDING = 16.dp

/** How far the page indicator sits from the buttons under it. */
private val BOTTOM_BAR_SPACING = 16.dp

/** How far apart two buttons of the bottom bar sit. */
private val ACTION_SPACING = 8.dp

/**
 * The introduction: the steps, and the buttons the last of them carries.
 *
 * Everything it draws is an argument, so it can be previewed and tested without a graph behind it.
 * The user cannot leave without answering — there is no dismiss — which is the whole point of
 * showing it as the root screen. Back is a step backwards inside the introduction rather than a way
 * out of it, and reading the policy comes back to the step it was left on.
 *
 * @param state What to draw, and where what the user does goes.
 * @param modifier The modifier applied to the screen.
 */
@Composable
internal fun IntroContent(state: IntroUiState, modifier: Modifier = Modifier) {
    if (state.steps.isEmpty()) {
        IntroLoading(modifier = modifier)
    } else {
        IntroSteps(state = state, modifier = modifier)
    }
}

/** The introduction before its content has been read: nothing to page through yet. */
@Composable
private fun IntroLoading(modifier: Modifier = Modifier) {
    Scaffold(modifier = modifier) { padding ->
        Box(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentAlignment = Alignment.Center,
        ) {
            val loading = stringResource(Res.string.intro_cd_loading)
            CircularProgressIndicator(
                modifier = Modifier
                    .testTag(IntroTestTags.LOADING)
                    .semantics { contentDescription = loading },
            )
        }
    }
}

/**
 * The steps themselves, with the way forward and the way back around them.
 *
 * A step is framed rather than left to fill the screen: how far along the reader is, said in words
 * above and drawn as a bar below, so a page of text never has to be judged by how much of it is
 * left. Everything the user can act on lives in the scaffold's bottom bar, within reach of a thumb,
 * so a step is the whole content area and scrolls under a frame that does not move. Going back a
 * step is offered twice, by the button and by the system gesture, and both do the same thing; on
 * the first step neither is handled, so back keeps meaning what it meant before — leaving the
 * application without an answer.
 */
@Composable
private fun IntroSteps(state: IntroUiState, modifier: Modifier = Modifier) {
    val pagerState = rememberPagerState { state.steps.size }
    val scope = rememberCoroutineScope()
    val canGoBack = pagerState.currentPage > 0
    val goBack = { scope.launch { pagerState.animateScrollToPage(pagerState.currentPage - 1) } }
    var isAcknowledged by rememberSaveable { mutableStateOf(false) }

    // Telemetry initialized with Unknown because need explicit User consent
    var selectedConsent by rememberSaveable { mutableStateOf(TelemetryConsent.Unknown) }

    val currentStep = state.steps[pagerState.currentPage]

    // The privacy step is the one step that has to be answered before it is left. Both ways forward
    // are closed while it is: the button is disabled, and the swipe is turned off — which stops the
    // gesture only, so the button and the system back still scroll the pager backwards. The
    // question needs no such gesture gate: it is the last step, and there is nothing to swipe on to.
    val isAwaitingAcknowledgement = currentStep is IntroStep.Privacy && !isAcknowledged

    val pendingHint = when {
        isAwaitingAcknowledgement -> Res.string.intro_privacy_acknowledge_hint

        currentStep is IntroStep.Decision && selectedConsent == TelemetryConsent.Unknown ->
            Res.string.intro_decision_answer_hint

        else -> null
    }

    NavigationBackHandler(
        state = rememberNavigationEventState(NavigationEventInfo.None),
        isBackEnabled = canGoBack,
        onBackCompleted = { goBack() },
    )

    Scaffold(
        modifier = modifier,
        bottomBar = {
            IntroBottomBar(
                state = state,
                pagerState = pagerState,
                canGoBack = canGoBack,
                pendingHint = pendingHint,
                onBack = { goBack() },
                onConfirm = { state.eventSink(IntroEvent.Answered(selectedConsent)) },
            )
        },
    ) { padding ->
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize().padding(padding).testTag(IntroTestTags.PAGER),
            userScrollEnabled = !isAwaitingAcknowledgement,
        ) { page ->
            IntroStepItem(
                step = state.steps[page],
                isAcknowledged = isAcknowledged,
                onAcknowledgedChange = { isAcknowledged = it },
                selectedConsent = selectedConsent,
                onConsentChange = { selectedConsent = it },
                onPolicyOpen = { state.eventSink(IntroEvent.PolicyOpened(it)) },
            )
        }
    }
}

/**
 * Where the reader is, and what there is to do about it: the bottom of every step.
 *
 * The bar is lifted off the step it sits under — its own container colour, and a hairline where the
 * two meet — because a step scrolls and the bar does not, and the seam is what says so.
 */
@Composable
private fun IntroBottomBar(
    state: IntroUiState,
    pagerState: PagerState,
    canGoBack: Boolean,
    pendingHint: StringResource?,
    onBack: () -> Unit,
    onConfirm: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val scope = rememberCoroutineScope()

    Surface(
        modifier = modifier
            .fillMaxWidth(),
        tonalElevation = BottomAppBarDefaults.ContainerElevation,
        shadowElevation = 4.dp,
        color = MaterialTheme.colorScheme.surfaceContainerLow,
    ) {
        Column(
            modifier = Modifier.safeDrawingPadding(),
        ) {
            Column(
                modifier = Modifier.padding(
                    horizontal = FRAME_HORIZONTAL_PADDING,
                    vertical = FRAME_VERTICAL_PADDING,
                ),
                verticalArrangement = Arrangement.spacedBy(BOTTOM_BAR_SPACING),
            ) {
                PageIndicator(
                    count = state.steps.size,
                    position = { pagerState.currentPage + pagerState.currentPageOffsetFraction },
                )

                IntroActions(
                    isDecision = state.steps[pagerState.currentPage] is IntroStep.Decision,
                    canGoBack = canGoBack,
                    pendingHint = pendingHint,
                    onBack = onBack,
                    onNext = {
                        scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) }
                    },
                    onConfirm = onConfirm,
                )
            }
        }
    }
}

/**
 * How far along the reader is: one segment per step, filling as the step arrives.
 *
 * [position] gives the pager's own place between two pages rather than the page it has settled on,
 * so the bar is dragged along with the step instead of catching up to it once the swipe is over —
 * the indicator tracks the finger, and the same interpolation carries it when a button scrolls the
 * pager. The number of segments is the number of steps the content happens to have: nothing here
 * knows how long the introduction is.
 *
 * It is a lambda read inside [drawBehind], and both halves of that matter: a swipe changes the
 * position on every frame, and reading it in composition instead would recompose the whole bottom
 * bar — buttons, labels and all — for a colour that only the draw phase needs.
 *
 * Left out of the semantics tree on purpose. It says exactly what [IntroStepCounter] says, and a
 * screen reader announcing a row of unlabelled boxes after the sentence that already gave the
 * position is noise.
 */
@Composable
private fun PageIndicator(count: Int, position: () -> Float, modifier: Modifier = Modifier) {
    val reached = MaterialTheme.colorScheme.primary
    val ahead = MaterialTheme.colorScheme.surfaceContainerHighest

    Row(
        modifier = modifier.fillMaxWidth().clearAndSetSemantics { },
        horizontalArrangement = Arrangement.spacedBy(INDICATOR_SEGMENT_SPACING),
    ) {
        repeat(count) { page ->
            Box(
                Modifier
                    .weight(1f)
                    .height(INDICATOR_SEGMENT_HEIGHT)
                    .clip(CircleShape)
                    .drawBehind { drawRect(segmentColor(page - position(), reached, ahead)) },
            )
        }
    }
}

/**
 * The colour of the segment [distance] pages away from where the reader currently is.
 *
 * Negative is behind and positive is ahead; a fraction is a swipe in progress. A segment behind
 * keeps the accent and loses opacity down to [INDICATOR_PAST_ALPHA], one ahead fades from the
 * accent into [ahead]. Both meet at full accent when the distance is nothing, which is what makes
 * the bar continuous while the pager is between two steps.
 */
private fun segmentColor(distance: Float, reached: Color, ahead: Color): Color {
    val activation = (1f - abs(distance)).coerceIn(0f, 1f)

    return if (distance < 0f) {
        reached.copy(alpha = INDICATOR_PAST_ALPHA + (1f - INDICATOR_PAST_ALPHA) * activation)
    } else {
        lerp(ahead, reached, activation)
    }
}

/**
 * Going back, going on, or giving the answer: one row, whatever the step.
 *
 * Back is the quietest thing on the bar and sits on the left; the way on is the filled button on
 * the right, with the arrow that names its direction. The step that asks the question keeps the
 * same shape and only changes what that button says and does — the answer itself is picked on the
 * page above, so the bar never has to hold two choices that must not read as one default and one
 * afterthought.
 *
 * Two steps can owe something before they are left — the privacy step its tick, the question its
 * answer — and [pendingHint] is what they owe, or null when they owe nothing. It does both jobs at
 * once: it disables the button, and it is the line printed above it saying what is missing. A
 * button that does nothing when it is pressed teaches the reader nothing about why.
 */
@Composable
private fun IntroActions(
    isDecision: Boolean,
    canGoBack: Boolean,
    pendingHint: StringResource?,
    onBack: () -> Unit,
    onNext: () -> Unit,
    onConfirm: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(ACTION_SPACING),
    ) {
        pendingHint?.let { hint ->
            Text(
                text = stringResource(hint),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(ACTION_SPACING),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (canGoBack) {
                BackButton(onBack = onBack)
            }

            Spacer(Modifier.weight(1f))

            if (isDecision) {
                Button(
                    onClick = onConfirm,
                    modifier = Modifier.testTag(IntroTestTags.FINISH),
                    enabled = pendingHint == null,
                ) {
                    Icon(
                        painter = painterResource(Res.drawable.flight),
                        contentDescription = null,
                        modifier = Modifier.size(ButtonDefaults.IconSize),
                    )
                    Spacer(Modifier.width(ButtonDefaults.IconSpacing))
                    Text(stringResource(Res.string.intro_finish))
                }
            } else {
                Button(
                    onClick = onNext,
                    modifier = Modifier.testTag(IntroTestTags.NEXT),
                    enabled = pendingHint == null,
                ) {
                    Text(stringResource(Res.string.intro_next))
                    Spacer(Modifier.width(ButtonDefaults.IconSpacing))
                    Icon(
                        painter = painterResource(Res.drawable.arrow_right_alt),
                        contentDescription = null,
                        modifier = Modifier.size(ButtonDefaults.IconSize),
                    )
                }
            }
        }
    }
}

/**
 * The way back a step: the quietest button on the bar.
 *
 * Text rather than outlined, because the reader is meant to move on and retracing is the exception.
 * The arrow carries no description of its own — the label beside it is already the label of the
 * whole button.
 */
@Composable
private fun BackButton(onBack: () -> Unit, modifier: Modifier = Modifier) {
    TextButton(onClick = onBack, modifier = modifier.testTag(IntroTestTags.BACK)) {
        Icon(
            painter = painterResource(Res.drawable.arrow_back),
            contentDescription = null,
            modifier = Modifier.size(ButtonDefaults.IconSize),
        )
        Spacer(Modifier.width(ButtonDefaults.IconSpacing))
        Text(stringResource(Res.string.intro_back))
    }
}

//region Previews

@PreviewScreenSizes
@Composable
private fun IntroContentPreview(@PreviewParameter(IntroContentPreviewParams::class) state: IntroUiState) =
    KTravelTheme {
        IntroContent(state = state)
    }
//endregion Previews
