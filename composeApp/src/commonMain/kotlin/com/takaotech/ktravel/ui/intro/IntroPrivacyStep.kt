package com.takaotech.ktravel.ui.intro

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withLink
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.mikepenz.markdown.m3.Markdown
import com.mikepenz.markdown.m3.markdownColor
import com.mikepenz.markdown.m3.markdownTypography
import com.takaotech.ktravel.core.telemetry.TelemetryConsent
import com.takaotech.ktravel.domain.staticflows.IntroStep
import com.takaotech.ktravel.domain.staticflows.PrivacyDetail
import com.takaotech.ktravel.ui.theme.KTravelTheme
import ktravel.composeapp.generated.resources.Res
import ktravel.composeapp.generated.resources.intro_cd_open_detail
import ktravel.composeapp.generated.resources.intro_privacy_acknowledge
import ktravel.composeapp.generated.resources.intro_privacy_link
import ktravel.composeapp.generated.resources.intro_privacy_overline
import ktravel.composeapp.generated.resources.open_in_new
import ktravel.composeapp.generated.resources.shield
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

/** How far the text of a privacy point sits from the arrow that opens it. */
private val DETAIL_SPACING = 14.dp

/** The arrow that says a point opens the document: small, and a step behind the text. */
private val DETAIL_TRAILING_SIZE = 20.dp
private const val DETAIL_TRAILING_ALPHA = 0.8f

/** Names the link that opens the policy, among the annotations of the acknowledgement. */
private const val POLICY_LINK_TAG = "privacyLink"

/** How much room the acknowledgement keeps inside its own outline. */
private val ACKNOWLEDGE_HORIZONTAL_PADDING = 18.dp
private val ACKNOWLEDGE_VERTICAL_PADDING = 16.dp

/**
 * The privacy page: the badge, what it is about, the points, and the tick that ends it.
 *
 * Left aligned rather than centred, unlike the pages built around a picture: this one is a column
 * of boxes, and boxes that do not share a left edge read as a list of unrelated things. The points
 * sit closer to each other than to the blocks around them, so they read as one group.
 */
@Composable
internal fun IntroPrivacyStep(
    step: IntroStep.Privacy,
    isAcknowledged: Boolean,
    onAcknowledgedChange: (Boolean) -> Unit,
    onPolicyOpened: (String?) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.spacedBy(SECTION_SPACING),
    ) {
        IntroBadge(icon = Res.drawable.shield)

        IntroStepHeading(
            overline = stringResource(Res.string.intro_privacy_overline),
            title = step.title,
            body = step.body,
        )

        Column(verticalArrangement = Arrangement.spacedBy(BLOCK_SPACING)) {
            step.details.forEach { detail ->
                PrivacyDetailRow(detail = detail, onClick = { onPolicyOpened(detail.policyRef) })
            }
        }

        PrivacyAcknowledgement(
            isAcknowledged = isAcknowledged,
            onAcknowledgedChange = onAcknowledgedChange,
            onPolicyOpened = { onPolicyOpened(null) },
        )
    }
}

/**
 * One privacy point: the summary, and the whole row as the way into what the policy says about it.
 *
 * The row is the target rather than an icon button beside it — a point is one thing to read and one
 * thing to open, and a full width target is what a thumb finds. Touching it opens the policy itself,
 * scrolled to the section this point summarises, so what is read is the document and not a second
 * copy of it.
 *
 * Outlined rather than raised: three of these stacked with a shadow each would read as three cards
 * competing with the acknowledgement under them, when they are only three lines to tap.
 */
@Composable
private fun PrivacyDetailRow(detail: PrivacyDetail, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth().testTag(IntroTestTags.detail(detail.id)),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        ),
        border = BorderStroke(
            width = OUTLINE_WIDTH,
            color = MaterialTheme.colorScheme.outlineVariant,
        ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = CARD_HORIZONTAL_PADDING,
                    vertical = CARD_VERTICAL_PADDING,
                ),
            horizontalArrangement = Arrangement.spacedBy(DETAIL_SPACING),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Text(
                    text = detail.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium,
                )

                Markdown(
                    content = detail.body,
                    colors = markdownColor(text = MaterialTheme.colorScheme.onSurfaceVariant),
                    typography = markdownTypography(
                        paragraph = MaterialTheme.typography.bodyMedium,
                    ),
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            Icon(
                painter = painterResource(Res.drawable.open_in_new),
                contentDescription = stringResource(Res.string.intro_cd_open_detail),
                modifier = Modifier.size(DETAIL_TRAILING_SIZE),
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = DETAIL_TRAILING_ALPHA),
            )
        }
    }
}

/**
 * The tick that says the policy has been seen, and the only thing on this page that has to be done.
 *
 * The whole box is the target and the checkbox inside it answers to nothing of its own
 * ([Checkbox] is handed a null callback, and [toggleable] on the row is what carries the state and
 * the role), so a screen reader is offered one control and not two, and a thumb has the whole width
 * to hit. Ticking it fills the box with the accent container: the state has to be readable at a
 * glance from the bar underneath, where the way on is waiting on it.
 *
 * The one thing inside it that is not the tick is the link on the last word, which opens the policy
 * at the top rather than on a section — nothing here summarises one point, it acknowledges the whole
 * document. It is underlined rather than coloured because it sits on two different containers, and a
 * colour that reads as a link on one of them would be lost on the other.
 */
@Composable
private fun PrivacyAcknowledgement(
    isAcknowledged: Boolean,
    onAcknowledgedChange: (Boolean) -> Unit,
    onPolicyOpened: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val shape = MaterialTheme.shapes.large
    val container by animateColorAsState(
        targetValue = if (isAcknowledged) {
            MaterialTheme.colorScheme.primaryContainer
        } else {
            MaterialTheme.colorScheme.surfaceContainerLow
        },
        label = "acknowledgement container",
    )
    val outline by animateColorAsState(
        targetValue = if (isAcknowledged) Color.Transparent else MaterialTheme.colorScheme.outline,
        label = "acknowledgement outline",
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(container)
            .border(width = OUTLINE_WIDTH, color = outline, shape = shape)
            .toggleable(
                value = isAcknowledged,
                onValueChange = onAcknowledgedChange,
                role = Role.Checkbox,
            )
            .padding(
                horizontal = ACKNOWLEDGE_HORIZONTAL_PADDING,
                vertical = ACKNOWLEDGE_VERTICAL_PADDING,
            )
            .testTag(IntroTestTags.ACKNOWLEDGE),
        horizontalArrangement = Arrangement.spacedBy(CARD_CONTENT_SPACING),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Checkbox(checked = isAcknowledged, onCheckedChange = null)

        val content = if (isAcknowledged) {
            MaterialTheme.colorScheme.onPrimaryContainer
        } else {
            MaterialTheme.colorScheme.onSurface
        }

        Text(
            text = buildAnnotatedString {
                append(stringResource(Res.string.intro_privacy_acknowledge))
                append(" ")
                withLink(
                    LinkAnnotation.Clickable(
                        tag = POLICY_LINK_TAG,
                    ) { onPolicyOpened() },
                ) {
                    append(stringResource(Res.string.intro_privacy_link))
                }
            },
            modifier = Modifier.testTag(IntroTestTags.ACKNOWLEDGE_LINK),
            style = MaterialTheme.typography.bodyLarge,
            color = content,
        )
    }
}

//region Previews

@PreviewLightDark
@Composable
private fun IntroStepItemPrivacyPreview() = KTravelTheme {
    Surface {
        IntroStepItem(
            step = previewPrivacyStep,
            isAcknowledged = false,
            onAcknowledgedChange = {},
            selectedConsent = TelemetryConsent.Denied,
            onConsentChange = {},
            onPolicyOpen = {},
        )
    }
}

@PreviewLightDark
@Composable
private fun IntroStepItemPrivacyAcknowledgedPreview() = KTravelTheme {
    Surface {
        IntroStepItem(
            step = previewPrivacyStep,
            isAcknowledged = true,
            onAcknowledgedChange = {},
            selectedConsent = TelemetryConsent.Denied,
            onConsentChange = {},
            onPolicyOpen = {},
        )
    }
}
//endregion Previews
