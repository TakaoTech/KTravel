package com.takaotech.ktravel.ui.consent

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
import com.takaotech.ktravel.core.telemetry.TelemetryConsent
import com.takaotech.ktravel.domain.staticflows.ConsentCard
import com.takaotech.ktravel.ui.theme.KTravelTheme
import kotlinx.collections.immutable.ImmutableList
import kotlinx.coroutines.launch
import ktravel.composeapp.generated.resources.Res
import ktravel.composeapp.generated.resources.consent_allow
import ktravel.composeapp.generated.resources.consent_cd_loading
import ktravel.composeapp.generated.resources.consent_deny
import ktravel.composeapp.generated.resources.consent_next
import org.jetbrains.compose.resources.stringResource

/** Size of one dot of the page indicator. */
private val INDICATOR_DOT_SIZE = 8.dp

/**
 * The privacy notice: the cards, and the two buttons on the last of them.
 *
 * Everything it draws is an argument, so it can be previewed and tested without a graph behind it.
 * The user cannot leave without answering — there is no dismiss and no back — which is the whole
 * point of showing it as the root screen.
 *
 * @param cards What to read, in order. Empty while the packaged file is still being read.
 * @param onAnswer Called with the answer, once.
 * @param modifier The modifier applied to the screen.
 */
@Composable
internal fun ConsentContent(
    cards: ImmutableList<ConsentCard>,
    onAnswer: (TelemetryConsent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(modifier = modifier) { padding ->
        if (cards.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center,
            ) {
                val loading = stringResource(Res.string.consent_cd_loading)
                CircularProgressIndicator(
                    modifier = Modifier
                        .testTag(ConsentTestTags.LOADING)
                        .semantics { contentDescription = loading },
                )
            }
        } else {
            val pagerState = rememberPagerState { cards.size }
            val scope = rememberCoroutineScope()

            Column(modifier = Modifier.fillMaxSize().padding(padding)) {
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.weight(1f).testTag(ConsentTestTags.PAGER),
                ) { page ->
                    ConsentCardItem(cards[page])
                }

                PageIndicator(count = cards.size, current = pagerState.currentPage)

                ConsentActions(
                    isDecision = cards[pagerState.currentPage].isDecision,
                    onNext = { scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) } },
                    onAnswer = onAnswer,
                )
            }
        }
    }
}

/** Where the reader is in the notice: one dot per card, the current one filled. */
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

/** Moving on, or answering: the last card is the only one that offers a choice. */
@Composable
private fun ConsentActions(
    isDecision: Boolean,
    onNext: () -> Unit,
    onAnswer: (TelemetryConsent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        if (isDecision) {
            Button(
                onClick = { onAnswer(TelemetryConsent.Granted) },
                modifier = Modifier.fillMaxWidth().testTag(ConsentTestTags.ALLOW),
            ) {
                Text(stringResource(Res.string.consent_allow))
            }

            OutlinedButton(
                onClick = { onAnswer(TelemetryConsent.Denied) },
                modifier = Modifier.fillMaxWidth().testTag(ConsentTestTags.DENY),
            ) {
                Text(stringResource(Res.string.consent_deny))
            }
        } else {
            Button(
                onClick = onNext,
                modifier = Modifier.fillMaxWidth().testTag(ConsentTestTags.NEXT),
            ) {
                Text(stringResource(Res.string.consent_next))
            }
        }
    }
}

//region Previews

@PreviewScreenSizes
@Composable
private fun ConsentContentPreview(
    @PreviewParameter(ConsentContentPreviewParams::class) cards: ImmutableList<ConsentCard>,
) = KTravelTheme {
    ConsentContent(cards = cards, onAnswer = {})
}
//endregion Previews
