package com.takaotech.ktravel.ui.intro

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.takaotech.ktravel.core.telemetry.TelemetryConsent
import com.takaotech.ktravel.domain.staticflows.IntroStep
import com.takaotech.ktravel.ui.theme.KTravelTheme
import ktravel.composeapp.generated.resources.Res
import ktravel.composeapp.generated.resources.bug_report
import ktravel.composeapp.generated.resources.cloud_upload
import ktravel.composeapp.generated.resources.devices
import ktravel.composeapp.generated.resources.intro_allow_detail
import ktravel.composeapp.generated.resources.intro_allow_diagnostic
import ktravel.composeapp.generated.resources.intro_decision_note_renewal
import ktravel.composeapp.generated.resources.intro_decision_note_settings
import ktravel.composeapp.generated.resources.intro_decision_overline
import ktravel.composeapp.generated.resources.intro_deny_detail
import ktravel.composeapp.generated.resources.intro_deny_diagnostic
import ktravel.composeapp.generated.resources.schedule
import ktravel.composeapp.generated.resources.settings
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

/** The icon that names a consent choice, and how far it sits from the label beside it. */
private val CHOICE_ICON_SIZE = 20.dp
private val CHOICE_ICON_SPACING = 8.dp

/** How much a selected choice's explanation steps back from its own title. */
private const val CHOICE_DETAIL_ALPHA = 0.85f

/** A footnote's icon, how far it sits from its line, and how far two footnotes sit apart. */
private val NOTE_ICON_SIZE = 18.dp
private val NOTE_ICON_SPACING = 10.dp
private val NOTE_SPACING = 12.dp

/**
 * The question: what it is asking, the two answers, and what neither of them costs.
 *
 * The answers are picked here and committed from the bar underneath, rather than being two buttons
 * down there. Two buttons make the reader choose from labels alone; a choice that stays on the page
 * can carry the sentence that says what each one actually sends, and can be changed as many times
 * as the reader likes before it is given. Nothing is preselected in favour of sending: the step
 * opens resting on the answer that keeps everything here.
 */
@Composable
internal fun IntroDecisionStep(
    step: IntroStep.Decision,
    selectedConsent: TelemetryConsent,
    onConsentChange: (TelemetryConsent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.spacedBy(SECTION_SPACING),
    ) {
        IntroBadge(
            icon = Res.drawable.bug_report,
            container = MaterialTheme.colorScheme.tertiaryContainer,
            content = MaterialTheme.colorScheme.onTertiaryContainer,
        )

        IntroStepHeading(
            overline = stringResource(Res.string.intro_decision_overline),
            title = step.title,
            body = step.body,
        )

        Column(verticalArrangement = Arrangement.spacedBy(BLOCK_SPACING)) {
            ConsentChoice(
                icon = Res.drawable.cloud_upload,
                title = stringResource(Res.string.intro_allow_diagnostic),
                detail = stringResource(Res.string.intro_allow_detail),
                isSelected = selectedConsent == TelemetryConsent.Granted,
                onSelect = { onConsentChange(TelemetryConsent.Granted) },
                modifier = Modifier.testTag(IntroTestTags.ALLOW),
            )

            ConsentChoice(
                icon = Res.drawable.devices,
                title = stringResource(Res.string.intro_deny_diagnostic),
                detail = stringResource(Res.string.intro_deny_detail),
                isSelected = selectedConsent == TelemetryConsent.Denied,
                onSelect = { onConsentChange(TelemetryConsent.Denied) },
                modifier = Modifier.testTag(IntroTestTags.DENY),
            )
        }

        Column(verticalArrangement = Arrangement.spacedBy(NOTE_SPACING)) {
            IntroNote(
                icon = Res.drawable.settings,
                text = stringResource(Res.string.intro_decision_note_settings),
            )

            IntroNote(
                icon = Res.drawable.schedule,
                text = stringResource(Res.string.intro_decision_note_renewal),
            )
        }
    }
}

/**
 * One of the two answers, and what picking it means.
 *
 * The whole box is the target and the radio inside it answers to nothing of its own, the way the
 * acknowledgement is built: one control offered to a screen reader, and the full width to a thumb.
 * Selecting fills it with the accent container, so which answer is about to be given is readable
 * from across the page rather than from a dot.
 */
@Composable
private fun ConsentChoice(
    icon: DrawableResource,
    title: String,
    detail: String,
    isSelected: Boolean,
    onSelect: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val shape = MaterialTheme.shapes.large
    val container by animateColorAsState(
        targetValue = if (isSelected) {
            MaterialTheme.colorScheme.primaryContainer
        } else {
            MaterialTheme.colorScheme.surfaceContainerLow
        },
        label = "consent choice container",
    )
    val outline by animateColorAsState(
        targetValue = if (isSelected) {
            Color.Transparent
        } else {
            MaterialTheme.colorScheme.outlineVariant
        },
        label = "consent choice outline",
    )
    val content = if (isSelected) {
        MaterialTheme.colorScheme.onPrimaryContainer
    } else {
        MaterialTheme.colorScheme.onSurface
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(container)
            .border(width = OUTLINE_WIDTH, color = outline, shape = shape)
            .selectable(selected = isSelected, onClick = onSelect, role = Role.RadioButton)
            .padding(
                horizontal = CARD_HORIZONTAL_PADDING,
                vertical = CARD_VERTICAL_PADDING,
            ),
        horizontalArrangement = Arrangement.spacedBy(CARD_CONTENT_SPACING),
        verticalAlignment = Alignment.Top,
    ) {
        RadioButton(selected = isSelected, onClick = null)

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(CHOICE_ICON_SPACING),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    painter = painterResource(icon),
                    contentDescription = null,
                    modifier = Modifier.size(CHOICE_ICON_SIZE),
                    tint = content,
                )

                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium,
                    color = content,
                )
            }

            Text(
                text = detail,
                style = MaterialTheme.typography.bodyMedium,
                color = if (isSelected) {
                    content.copy(alpha = CHOICE_DETAIL_ALPHA)
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
            )
        }
    }
}

/** A footnote: something the answer above does not cost, said quietly under it. */
@Composable
private fun IntroNote(icon: DrawableResource, text: String, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(NOTE_ICON_SPACING),
        verticalAlignment = Alignment.Top,
    ) {
        Icon(
            painter = painterResource(icon),
            contentDescription = null,
            modifier = Modifier.size(NOTE_ICON_SIZE),
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

//region Previews

@PreviewLightDark
@Composable
private fun IntroStepItemDecisionPreview() = KTravelTheme {
    Surface {
        IntroStepItem(
            step = previewDecisionStep,
            isAcknowledged = true,
            onAcknowledgedChange = {},
            selectedConsent = TelemetryConsent.Unknown,
            onConsentChange = {},
            onPolicyOpen = {},
        )
    }
}

@PreviewLightDark
@Composable
private fun IntroStepItemDecisionGrantedPreview() = KTravelTheme {
    Surface {
        IntroStepItem(
            step = previewDecisionStep,
            isAcknowledged = true,
            onAcknowledgedChange = {},
            selectedConsent = TelemetryConsent.Granted,
            onConsentChange = {},
            onPolicyOpen = {},
        )
    }
}
//endregion Previews
