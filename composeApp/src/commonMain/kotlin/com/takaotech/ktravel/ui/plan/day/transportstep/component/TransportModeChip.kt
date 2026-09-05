package com.takaotech.ktravel.ui.plan.day.transportstep.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import com.takaotech.ktravel.domain.model.TransportType
import com.takaotech.ktravel.domain.routing.RoutingMode
import com.takaotech.ktravel.ui.plan.day.toIcon
import com.takaotech.ktravel.ui.shared.format.labelOrNull
import com.takaotech.ktravel.ui.theme.KTravelTheme
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import com.takaotech.ktravel.presentation.plan.day.TransportStepUi as TransportStepUiModel

/** The vehicle the leg is covered in, named the way the navigator named it when it can be. */
@Composable
internal fun ModeChip(transport: TransportStepUiModel) {
    val label = transport.answer.principalMode?.let { RoutingMode(it.uppercase()).labelOrNull() }
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(MaterialTheme.colorScheme.primaryContainer)
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Icon(
            modifier = Modifier.size(16.dp),
            painter = painterResource(transport.type.toIcon()),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onPrimaryContainer,
        )
        Text(
            text = label?.let { stringResource(it) } ?: transport.type.name,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
        )
    }
}

//region Previews

@Preview(showBackground = true)
@Composable
private fun ModeChipPreview(@PreviewParameter(ModeChipPreviewParams::class) transport: TransportStepUiModel) =
    KTravelTheme {
        Surface {
            Box(modifier = Modifier.padding(12.dp)) {
                ModeChip(transport)
            }
        }
    }

/**
 * A mode the project has a word for, one that comes from a timetable, and one it has never heard of.
 *
 * The last is the reason the label falls back to the type: the navigator names the vehicle in its
 * own vocabulary, which is open, and a chip with an empty label would be worse than a coarse one.
 */
internal class ModeChipPreviewParams : PreviewParameterProvider<TransportStepUiModel> {
    override val values = sequenceOf(
        previewRoadStep(),
        previewTransitStep(),
        previewRoadStep(mode = "funicular", type = TransportType.TRAIN),
    )
}
//endregion Previews
