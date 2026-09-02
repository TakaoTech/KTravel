package com.takaotech.ktravel.core.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.indication
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.takaotech.ktravel.ui.theme.KTravelTheme
import ktravel.composeapp.generated.resources.Res
import ktravel.composeapp.generated.resources.add
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource

/** How the pill is drawn: filled once it carries a value, dashed while it is still to be defined. */
@Immutable
enum class PillChipStyle {
    /** Solid background: the chip carries a value. */
    Filled,

    /** Dashed outline: the value is still to be defined, and the chip invites the user to do it. */
    Dashed,
}

/**
 * Metrics of a [PillChip]. [Small] reads a value inside a dense card, [Medium] reads an action and
 * sits next to body text.
 */
@Immutable
enum class PillChipSize(internal val iconSize: Dp, internal val gap: Dp, internal val horizontalPadding: Dp) {
    /** Reads as a value inside a dense card. */
    Small(iconSize = 14.dp, gap = 4.dp, horizontalPadding = 8.dp),

    /** Reads as an action, next to body text. */
    Medium(iconSize = 16.dp, gap = 6.dp, horizontalPadding = 12.dp),
    ;

    internal val textStyle: TextStyle
        @Composable get() = when (this) {
            Small -> MaterialTheme.typography.labelMedium
            Medium -> MaterialTheme.typography.labelLarge
        }
}

/** Metrics a caller has to agree with to line a [PillChip] up with something else. */
object PillChipDefaults {
    /** Height of the pill itself, which is shorter than the 48dp touch target around it. */
    val Height = 32.dp
}

/**
 * Compact stadium-shaped chip of an icon and a label, clickable as a whole.
 *
 * The 48dp touch target lives on the outer box while the pill keeps its compact height; the ripple
 * is drawn on the pill rather than on the target, so the indication follows the shape the user
 * sees — the filled background or the dashed outline — instead of overflowing it.
 *
 * @param text The label, kept to one line.
 * @param icon Drawn before the label, and described by [contentDescription] rather than on its own.
 * @param onClick What tapping the chip does.
 * @param style Whether the chip reads as filled or as still to be defined.
 * @param size Which metrics it is drawn with.
 * @param contentColor Colour of the icon and of the label.
 * @param contentDescription What the whole chip announces, null to leave it to the label alone.
 * @param contentAlignment Where the pill sits inside its touch target, which matters only when the
 * target is the larger of the two.
 */
@Composable
fun PillChip(
    text: String,
    icon: DrawableResource,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    style: PillChipStyle = PillChipStyle.Dashed,
    size: PillChipSize = PillChipSize.Small,
    contentColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    contentDescription: String? = null,
    contentAlignment: Alignment = Alignment.Center,
) {
    val outline = MaterialTheme.colorScheme.outlineVariant
    val interactionSource = remember { MutableInteractionSource() }

    Box(
        modifier = modifier
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick,
            )
            .then(
                if (contentDescription == null) {
                    Modifier
                } else {
                    Modifier.semantics { this.contentDescription = contentDescription }
                },
            )
            .minimumInteractiveComponentSize(),
        contentAlignment = contentAlignment,
    ) {
        Row(
            modifier = Modifier
                .heightIn(min = PillChipDefaults.Height)
                .clip(CircleShape)
                .then(
                    when (style) {
                        PillChipStyle.Filled ->
                            Modifier.background(MaterialTheme.colorScheme.surfaceContainerHighest)

                        PillChipStyle.Dashed -> Modifier.dashedOutline(outline)
                    },
                )
                .indication(interactionSource, ripple())
                .padding(horizontal = size.horizontalPadding, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                modifier = Modifier.size(size.iconSize),
                painter = painterResource(icon),
                contentDescription = null,
                tint = contentColor,
            )
            Spacer(Modifier.width(size.gap))
            Text(
                text = text,
                style = size.textStyle,
                color = contentColor,
                maxLines = 1,
            )
        }
    }
}

private val DashLength = 4.dp
private val DashGap = 5.dp

/** Stadium-shaped dashed outline: the "still to be defined" treatment of the design. */
internal fun Modifier.dashedOutline(color: Color, width: Dp = 1.dp): Modifier = drawBehind {
    val stroke = width.toPx()
    val radius = CornerRadius(size.height / 2f, size.height / 2f)
    drawRoundRect(
        color = color,
        topLeft = Offset(stroke / 2f, stroke / 2f),
        size = Size(size.width - stroke, size.height - stroke),
        cornerRadius = radius,
        style = Stroke(
            width = stroke,
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(DashLength.toPx(), DashGap.toPx())),
        ),
    )
}

@PreviewLightDark
@Composable
private fun PillChipPreview() = KTravelTheme {
    Surface {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            PillChipSize.entries.forEach { size ->
                PillChipStyle.entries.forEach { style ->
                    PillChip(
                        text = "${size.name} ${style.name}",
                        icon = Res.drawable.add,
                        onClick = {},
                        style = style,
                        size = size,
                    )
                }
            }
        }
    }
}
