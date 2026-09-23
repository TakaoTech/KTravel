package com.takaotech.ktravel.ui.plan.place.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.takaotech.ktravel.domain.search.model.PlaceCandidate
import com.takaotech.ktravel.presentation.place.CoordinateParser
import com.takaotech.ktravel.ui.plan.place.previewTorrazzo
import com.takaotech.ktravel.ui.theme.KTravelTheme
import ktravel.composeapp.generated.resources.Res
import ktravel.composeapp.generated.resources.add
import ktravel.composeapp.generated.resources.close
import ktravel.composeapp.generated.resources.delete
import ktravel.composeapp.generated.resources.my_location
import ktravel.composeapp.generated.resources.place_insert_add
import ktravel.composeapp.generated.resources.place_insert_cd_close
import ktravel.composeapp.generated.resources.place_insert_center
import ktravel.composeapp.generated.resources.place_insert_remove
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

/** Side of the tile at the top of the card. */
private val CARD_TILE_SIZE = 56.dp

/**
 * The card of one place, floating over the bottom of the map: what it is, where it is, and the
 * buttons to centre the map on it and to add it to the selection or take it out.
 */
@Composable
internal fun PlaceDetailCard(
    candidate: PlaceCandidate,
    isSelected: Boolean,
    onDismiss: () -> Unit,
    onCenterClick: () -> Unit,
    onToggleSelection: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.extraLarge,
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        shadowElevation = FLOATING_ELEVATION,
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 14.dp, top = 14.dp, end = 8.dp, bottom = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                PlaceCandidateTile(candidate = candidate, size = CARD_TILE_SIZE)
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = candidate.title,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        text = candidate.supportingText(),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                IconButton(onClick = onDismiss) {
                    Icon(
                        painter = painterResource(Res.drawable.close),
                        contentDescription = stringResource(Res.string.place_insert_cd_close),
                    )
                }
            }
            candidate.addressLabel?.let {
                Text(
                    text = it,
                    modifier = Modifier.padding(horizontal = 16.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Text(
                text = CoordinateParser.format(candidate.coordinate),
                modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 8.dp),
                style = MaterialTheme.typography.labelMedium.copy(fontFamily = FontFamily.Monospace),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            DetailActions(isSelected = isSelected, onCenterClick = onCenterClick, onToggleSelection = onToggleSelection)
        }
    }
}

@Composable
private fun DetailActions(isSelected: Boolean, onCenterClick: () -> Unit, onToggleSelection: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 12.dp, end = 12.dp, top = 8.dp, bottom = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
    ) {
        TextButton(onClick = onCenterClick, contentPadding = ButtonDefaults.TextButtonWithIconContentPadding) {
            Icon(
                painter = painterResource(Res.drawable.my_location),
                contentDescription = null,
                modifier = Modifier.size(ButtonDefaults.IconSize),
            )
            Text(
                text = stringResource(Res.string.place_insert_center),
                modifier = Modifier.padding(start = ButtonDefaults.IconSpacing),
            )
        }
        FilledTonalButton(onClick = onToggleSelection, contentPadding = ButtonDefaults.ButtonWithIconContentPadding) {
            Icon(
                painter = painterResource(if (isSelected) Res.drawable.delete else Res.drawable.add),
                contentDescription = null,
                modifier = Modifier.size(ButtonDefaults.IconSize),
            )
            Text(
                text = stringResource(if (isSelected) Res.string.place_insert_remove else Res.string.place_insert_add),
                modifier = Modifier.padding(start = ButtonDefaults.IconSpacing),
            )
        }
    }
}

//region Previews
@Preview(showBackground = true)
@Composable
private fun PlaceDetailCardPreview() = KTravelTheme {
    Surface {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            listOf(false, true).forEach { selected ->
                PlaceDetailCard(
                    candidate = previewTorrazzo,
                    isSelected = selected,
                    onDismiss = {},
                    onCenterClick = {},
                    onToggleSelection = {},
                    modifier = Modifier.width(328.dp),
                )
            }
        }
    }
}
//endregion Previews
