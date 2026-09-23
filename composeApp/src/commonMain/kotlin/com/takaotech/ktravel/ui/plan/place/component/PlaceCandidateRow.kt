package com.takaotech.ktravel.ui.plan.place.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.takaotech.ktravel.domain.search.model.PlaceCandidate
import com.takaotech.ktravel.domain.search.model.PlaceCandidateSource
import com.takaotech.ktravel.ui.plan.place.previewCoordinates
import com.takaotech.ktravel.ui.plan.place.previewLongAddress
import com.takaotech.ktravel.ui.plan.place.previewTorrazzo
import com.takaotech.ktravel.ui.theme.KTravelTheme
import ktravel.composeapp.generated.resources.Res
import ktravel.composeapp.generated.resources.add_circle
import ktravel.composeapp.generated.resources.check_circle
import ktravel.composeapp.generated.resources.info
import ktravel.composeapp.generated.resources.place_insert_cd_add
import ktravel.composeapp.generated.resources.place_insert_cd_details
import ktravel.composeapp.generated.resources.place_insert_cd_remove
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

/** Side of the tile that stands in for a place's picture in a row. */
private val ROW_TILE_SIZE = 44.dp

/**
 * One place in a list: its tile, its name, what it is and how far, and the actions on the right.
 *
 * Highlighted when [isSelected], so a place picked in one list is recognisable in another.
 *
 * @param onClick Called when the row itself is tapped, rather than one of its actions.
 * @param trailing The actions at the end of the row, such as [PlaceDetailsButton] and
 *   [PlaceSelectionButton].
 */
@Composable
internal fun PlaceCandidateRow(
    candidate: PlaceCandidate,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    trailing: @Composable RowScope.() -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    Row(
        modifier = modifier
            .clip(MaterialTheme.shapes.large)
            .background(if (isSelected) colors.primaryContainer else colors.surfaceContainerLow)
            .clickable(onClick = onClick)
            .padding(start = 8.dp, top = 8.dp, bottom = 8.dp, end = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        PlaceCandidateTile(candidate = candidate, size = ROW_TILE_SIZE)
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = candidate.title,
                style = MaterialTheme.typography.titleSmall,
                color = if (isSelected) colors.onPrimaryContainer else colors.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            val supporting = candidate.supportingText()
            if (supporting.isNotEmpty()) {
                Text(
                    text = supporting,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isSelected) colors.onPrimaryContainer else colors.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
        Row(verticalAlignment = Alignment.CenterVertically, content = trailing)
    }
}

/**
 * The square standing in for a place's picture: the symbol of its category.
 *
 * Typed coordinates get the tertiary colours, so a point without a name stands apart from the places
 * a provider knows.
 */
@Composable
internal fun PlaceCandidateTile(candidate: PlaceCandidate, size: Dp, modifier: Modifier = Modifier) {
    val colors = MaterialTheme.colorScheme
    val isCoordinate = candidate.source == PlaceCandidateSource.COORDINATES
    Surface(
        modifier = modifier.size(size),
        shape = MaterialTheme.shapes.medium,
        color = if (isCoordinate) colors.tertiaryContainer else colors.secondaryContainer,
        contentColor = if (isCoordinate) colors.onTertiaryContainer else colors.onSecondaryContainer,
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(painter = painterResource(candidate.icon), contentDescription = null)
        }
    }
}

/** Opens the card of [candidate]. */
@Composable
internal fun PlaceDetailsButton(candidate: PlaceCandidate, onClick: () -> Unit, modifier: Modifier = Modifier) {
    IconButton(onClick = onClick, modifier = modifier) {
        Icon(
            painter = painterResource(Res.drawable.info),
            contentDescription = stringResource(Res.string.place_insert_cd_details, candidate.title),
        )
    }
}

/** Adds [candidate] to the selection, or takes it out when [isSelected]. */
@Composable
internal fun PlaceSelectionButton(
    candidate: PlaceCandidate,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    if (isSelected) {
        FilledIconButton(onClick = onClick, modifier = modifier) {
            Icon(
                painter = painterResource(Res.drawable.check_circle),
                contentDescription = stringResource(Res.string.place_insert_cd_remove, candidate.title),
            )
        }
    } else {
        IconButton(onClick = onClick, modifier = modifier) {
            Icon(
                painter = painterResource(Res.drawable.add_circle),
                contentDescription = stringResource(Res.string.place_insert_cd_add, candidate.title),
            )
        }
    }
}

//region Previews
@Preview(showBackground = true)
@Composable
private fun PlaceCandidateRowPreview() = KTravelTheme {
    Surface {
        Column(
            modifier = Modifier
                .padding(12.dp)
                .width(360.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            listOf(previewTorrazzo to true, previewLongAddress to false, previewCoordinates to false)
                .forEach { (candidate, selected) ->
                    PlaceCandidateRow(candidate = candidate, isSelected = selected, onClick = {}) {
                        PlaceDetailsButton(candidate = candidate, onClick = {})
                        PlaceSelectionButton(candidate = candidate, isSelected = selected, onClick = {})
                    }
                }
        }
    }
}
//endregion Previews
