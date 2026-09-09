package com.takaotech.ktravel.ui.intro

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
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
import ktravel.composeapp.generated.resources.intro_allow
import ktravel.composeapp.generated.resources.intro_back
import ktravel.composeapp.generated.resources.intro_cd_loading
import ktravel.composeapp.generated.resources.intro_deny
import ktravel.composeapp.generated.resources.intro_next
import org.jetbrains.compose.resources.stringResource

/** Size of one dot of the page indicator. */
private val INDICATOR_DOT_SIZE = 8.dp

/** How the bottom bar is split between going back and going on: the way on is the wider half. */
private const val BACK_WEIGHT = 1f
private const val ACTIONS_WEIGHT = 2f

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
 * Everything the user can act on lives in the scaffold's bottom bar, within reach of a thumb, so a
 * step is the whole content area and scrolls under a frame that does not move. Going back a step is
 * offered twice, by the button and by the system gesture, and both do the same thing; on the first
 * step neither is handled, so back keeps meaning what it meant before — leaving the application
 * without an answer.
 */
@Composable
private fun IntroSteps(state: IntroUiState, modifier: Modifier = Modifier) {
    val pagerState = rememberPagerState { state.steps.size }
    val scope = rememberCoroutineScope()
    val canGoBack = pagerState.currentPage > 0
    val goBack = { scope.launch { pagerState.animateScrollToPage(pagerState.currentPage - 1) } }

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
                onBack = { goBack() },
            )
        },
    ) { padding ->
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize().padding(padding).testTag(IntroTestTags.PAGER),
        ) { page ->
            IntroStepItem(
                step = state.steps[page],
                onPolicyOpened = { state.eventSink(IntroEvent.PolicyOpened(it)) },
            )
        }
    }
}

/** Where the reader is, and what there is to do about it: the bottom of every step. */
@Composable
private fun IntroBottomBar(
    state: IntroUiState,
    pagerState: PagerState,
    canGoBack: Boolean,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val scope = rememberCoroutineScope()

    Column(modifier = modifier) {
        PageIndicator(count = state.steps.size, current = pagerState.currentPage)

        IntroActions(
            isDecision = state.steps[pagerState.currentPage] is IntroStep.Decision,
            canGoBack = canGoBack,
            onBack = onBack,
            onNext = { scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) } },
            onAnswer = { state.eventSink(IntroEvent.Answered(it)) },
        )
    }
}

/** Where the reader is in the introduction: one dot per step, the current one filled. */
@Composable
private fun PageIndicator(count: Int, current: Int, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth().padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
    ) {
        repeat(count) { page ->
            val color = if (page == current) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            }

            Box(Modifier.size(INDICATOR_DOT_SIZE).clip(CircleShape).background(color))
        }
    }
}

/**
 * Going back, going on, or answering: the last step is the only one that offers a choice.
 *
 * Back is outlined and narrower than what it sits beside, because it is the secondary way out of a
 * step — the reader is meant to move on, not to retrace. It sits to the left of the step's own
 * buttons on every step that has one behind it, so it never moves with the number of buttons a
 * step happens to carry.
 */
@Composable
private fun IntroActions(
    isDecision: Boolean,
    canGoBack: Boolean,
    onBack: () -> Unit,
    onNext: () -> Unit,
    onAnswer: (TelemetryConsent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.Top,
    ) {
        if (canGoBack) {
            OutlinedButton(
                onClick = onBack,
                modifier = Modifier.weight(BACK_WEIGHT).testTag(IntroTestTags.BACK),
            ) {
                Text(stringResource(Res.string.intro_back))
            }
        }

        Column(
            modifier = Modifier.weight(ACTIONS_WEIGHT),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            if (isDecision) {
                Button(
                    onClick = { onAnswer(TelemetryConsent.Granted) },
                    modifier = Modifier.fillMaxWidth().testTag(IntroTestTags.ALLOW),
                ) {
                    Text(stringResource(Res.string.intro_allow))
                }

                OutlinedButton(
                    onClick = { onAnswer(TelemetryConsent.Denied) },
                    modifier = Modifier.fillMaxWidth().testTag(IntroTestTags.DENY),
                ) {
                    Text(stringResource(Res.string.intro_deny))
                }
            } else {
                Button(
                    onClick = onNext,
                    modifier = Modifier.fillMaxWidth().testTag(IntroTestTags.NEXT),
                ) {
                    Text(stringResource(Res.string.intro_next))
                }
            }
        }
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
