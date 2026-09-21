package com.takaotech.ktravel.ui.intro

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.takaotech.ktravel.core.telemetry.TelemetryConsent
import com.takaotech.ktravel.domain.staticflows.IntroStep
import com.takaotech.ktravel.ui.theme.KTravelTheme
import ktravel.composeapp.generated.resources.Res
import ktravel.composeapp.generated.resources.bug_report
import ktravel.composeapp.generated.resources.check
import ktravel.composeapp.generated.resources.cloud_off
import ktravel.composeapp.generated.resources.cloud_upload
import ktravel.composeapp.generated.resources.gavel
import ktravel.composeapp.generated.resources.intro_decision_note_settings
import ktravel.composeapp.generated.resources.intro_decision_overline
import ktravel.composeapp.generated.resources.intro_diagnostics_off_detail
import ktravel.composeapp.generated.resources.intro_diagnostics_off_title
import ktravel.composeapp.generated.resources.intro_diagnostics_on_detail
import ktravel.composeapp.generated.resources.intro_diagnostics_on_title
import ktravel.composeapp.generated.resources.intro_diagnostics_sent_app
import ktravel.composeapp.generated.resources.intro_diagnostics_sent_crash
import ktravel.composeapp.generated.resources.intro_diagnostics_sent_installation
import ktravel.composeapp.generated.resources.intro_diagnostics_sent_logs
import ktravel.composeapp.generated.resources.intro_diagnostics_sent_title
import ktravel.composeapp.generated.resources.intro_legal_basis_detail
import ktravel.composeapp.generated.resources.intro_legal_basis_label
import ktravel.composeapp.generated.resources.settings
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

/** The icon that names the state of the switch, and how far it sits from the label beside it. */
private val TOGGLE_ICON_SIZE = 20.dp
private val TOGGLE_ICON_SPACING = 8.dp

/** How much the explanation under a switched-on row steps back from its own title. */
private const val TOGGLE_DETAIL_ALPHA = 0.85f

/** How far the list of what is sent fades once nothing is being sent. */
private const val DIMMED_ALPHA = 0.55f

/** A line of the list of what is sent: its tick, and how far the tick sits from the line. */
private val DATA_ICON_SIZE = 18.dp
private val DATA_ICON_SPACING = 8.dp
private val DATA_LINE_SPACING = 6.dp

/** How far the overline of a box sits from the lines under it. */
private val OVERLINE_SPACING = 8.dp

/** How wide the letters of the overline inside a box are set. */
private val BOX_OVERLINE_TRACKING = 1.sp

/** A footnote's icon, and how far it sits from its line. */
private val NOTE_ICON_SIZE = 18.dp
private val NOTE_ICON_SPACING = 10.dp

/**
 * The last step: diagnostics are already on, and this is where they are turned off.
 *
 * Nothing is asked here, because nothing is being consented to. Diagnostics run on the developer's
 * legitimate interest — crashes and technical log lines, so an error can be fixed without the user
 * having to describe it — and what the user has instead is the right to object. So the step states
 * what is happening rather than posing a question: one switch that is already on, the list of what
 * leaves the device underneath it, and the legal basis said out loud with the way out named in the
 * same breath. The switch is the objection, and it can be thrown here or in the settings, forever.
 *
 * @param step The heading and the paragraph, as the packaged introduction words them.
 * @param selectedConsent Where diagnostics currently stand.
 * @param onConsentChange Called with the new value when the switch is thrown.
 * @param modifier The modifier applied to the step.
 */
@Composable
internal fun IntroDecisionStep(
    step: IntroStep.Decision,
    selectedConsent: TelemetryConsent,
    onConsentChange: (TelemetryConsent) -> Unit,
    modifier: Modifier = Modifier,
) {
    // Anything that is not an objection leaves diagnostics on, which is what the introduction is
    // showing the user: the switch has two positions, and Unknown is not one of them.
    val isSending = selectedConsent != TelemetryConsent.Denied

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
            DiagnosticsToggleRow(
                isSending = isSending,
                onSendingChange = { sending ->
                    onConsentChange(if (sending) TelemetryConsent.Granted else TelemetryConsent.Denied)
                },
                modifier = Modifier.testTag(IntroTestTags.DIAGNOSTICS_TOGGLE),
            )

            DiagnosticsDataList(isSending = isSending)

            LegalBasisStrip()
        }

        IntroNote(
            icon = Res.drawable.settings,
            text = stringResource(Res.string.intro_decision_note_settings),
        )
    }
}

/**
 * The switch itself, and what its current position means.
 *
 * The whole box is the target and the switch inside it answers to nothing of its own: one control
 * offered to a screen reader, and the full width to a thumb. On, it fills with the accent container,
 * so what is happening is readable from across the page rather than from the position of a knob.
 */
@Composable
private fun DiagnosticsToggleRow(
    isSending: Boolean,
    onSendingChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    val shape = MaterialTheme.shapes.large
    val container by animateColorAsState(
        targetValue = if (isSending) {
            MaterialTheme.colorScheme.primaryContainer
        } else {
            MaterialTheme.colorScheme.surfaceContainerLow
        },
        label = "diagnostics toggle container",
    )
    val outline by animateColorAsState(
        targetValue = if (isSending) Color.Transparent else MaterialTheme.colorScheme.outlineVariant,
        label = "diagnostics toggle outline",
    )
    val content = if (isSending) {
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
            .toggleable(value = isSending, onValueChange = onSendingChange, role = Role.Switch)
            .padding(horizontal = CARD_HORIZONTAL_PADDING, vertical = CARD_VERTICAL_PADDING),
        horizontalArrangement = Arrangement.spacedBy(CARD_CONTENT_SPACING),
        verticalAlignment = Alignment.Top,
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(TOGGLE_ICON_SPACING),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    painter = painterResource(
                        if (isSending) Res.drawable.cloud_upload else Res.drawable.cloud_off,
                    ),
                    contentDescription = null,
                    modifier = Modifier.size(TOGGLE_ICON_SIZE),
                    tint = content,
                )

                Text(
                    text = stringResource(
                        if (isSending) {
                            Res.string.intro_diagnostics_on_title
                        } else {
                            Res.string.intro_diagnostics_off_title
                        },
                    ),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium,
                    color = content,
                )
            }

            Text(
                text = stringResource(
                    if (isSending) {
                        Res.string.intro_diagnostics_on_detail
                    } else {
                        Res.string.intro_diagnostics_off_detail
                    },
                ),
                style = MaterialTheme.typography.bodyMedium,
                color = if (isSending) {
                    content.copy(alpha = TOGGLE_DETAIL_ALPHA)
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
            )
        }

        Switch(checked = isSending, onCheckedChange = null)
    }
}

/**
 * What actually leaves the device, listed line by line.
 *
 * It is the same five categories the privacy policy enumerates in section 2.2, grouped into four
 * lines: a claim about what is sent is only worth making next to the list it can be checked against.
 * The list fades rather than disappears when the switch is thrown, because what it says is still
 * what would be sent if the switch came back.
 */
@Composable
private fun DiagnosticsDataList(isSending: Boolean, modifier: Modifier = Modifier) {
    val shape = MaterialTheme.shapes.large
    val dim by animateFloatAsState(
        targetValue = if (isSending) 1f else DIMMED_ALPHA,
        label = "diagnostics data list alpha",
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .graphicsLayer { alpha = dim }
            .clip(shape)
            .background(MaterialTheme.colorScheme.surfaceContainerLow)
            .border(
                width = OUTLINE_WIDTH,
                color = MaterialTheme.colorScheme.outlineVariant,
                shape = shape,
            )
            .padding(horizontal = CARD_HORIZONTAL_PADDING, vertical = CARD_VERTICAL_PADDING),
        verticalArrangement = Arrangement.spacedBy(OVERLINE_SPACING),
    ) {
        BoxOverline(text = stringResource(Res.string.intro_diagnostics_sent_title))

        Column(verticalArrangement = Arrangement.spacedBy(DATA_LINE_SPACING)) {
            SENT_DATA.forEach { line -> DataLine(text = stringResource(line)) }
        }
    }
}

/** The four lines of [DiagnosticsDataList], in the order the policy lists them. */
private val SENT_DATA: List<StringResource> = listOf(
    Res.string.intro_diagnostics_sent_app,
    Res.string.intro_diagnostics_sent_crash,
    Res.string.intro_diagnostics_sent_logs,
    Res.string.intro_diagnostics_sent_installation,
)

/** One line of the list: the tick that says it is sent, and the thing that is. */
@Composable
private fun DataLine(text: String, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(DATA_ICON_SPACING),
        verticalAlignment = Alignment.Top,
    ) {
        Icon(
            painter = painterResource(Res.drawable.check),
            contentDescription = null,
            modifier = Modifier.size(DATA_ICON_SIZE),
            tint = MaterialTheme.colorScheme.primary,
        )

        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

/**
 * The legal basis, in the words the regulation uses, next to the control that acts on it.
 *
 * Its own colour rather than another outlined box: it is the one thing on the page the user cannot
 * change, and the tertiary container is what says so without a heading announcing "legal".
 */
@Composable
private fun LegalBasisStrip(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.large)
            .background(MaterialTheme.colorScheme.tertiaryContainer)
            .padding(horizontal = CARD_HORIZONTAL_PADDING, vertical = CARD_VERTICAL_PADDING),
        horizontalArrangement = Arrangement.spacedBy(CARD_CONTENT_SPACING),
        verticalAlignment = Alignment.Top,
    ) {
        Icon(
            painter = painterResource(Res.drawable.gavel),
            contentDescription = null,
            modifier = Modifier.size(DATA_ICON_SIZE),
            tint = MaterialTheme.colorScheme.onTertiaryContainer,
        )

        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            BoxOverline(
                text = stringResource(Res.string.intro_legal_basis_label),
                color = MaterialTheme.colorScheme.onTertiaryContainer,
            )

            Text(
                text = stringResource(Res.string.intro_legal_basis_detail),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onTertiaryContainer,
            )
        }
    }
}

/** The small capitals that title a box, set the way the overline of a step is. */
@Composable
private fun BoxOverline(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.onSurfaceVariant,
) {
    Text(
        text = text.uppercase(),
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.Bold,
        letterSpacing = BOX_OVERLINE_TRACKING,
        color = color,
        modifier = modifier,
    )
}

/** A footnote: where the switch can be thrown again, said quietly under it. */
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
private fun IntroStepItemDecisionOnPreview() = KTravelTheme {
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

@PreviewLightDark
@Composable
private fun IntroStepItemDecisionOffPreview() = KTravelTheme {
    Surface {
        IntroStepItem(
            step = previewDecisionStep,
            isAcknowledged = true,
            onAcknowledgedChange = {},
            selectedConsent = TelemetryConsent.Denied,
            onConsentChange = {},
            onPolicyOpen = {},
        )
    }
}
//endregion Previews
