package com.takaotech.ktravel.ui.plan.place.component

import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.takaotech.ktravel.domain.search.model.PlaceCandidate
import com.takaotech.ktravel.ui.plan.place.previewCoordinates
import com.takaotech.ktravel.ui.plan.place.previewTorrazzo
import com.takaotech.ktravel.ui.theme.KTravelTheme
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import ktravel.composeapp.generated.resources.Res
import ktravel.composeapp.generated.resources.checklist_24dp
import ktravel.composeapp.generated.resources.close
import ktravel.composeapp.generated.resources.place_insert_cd_remove
import ktravel.composeapp.generated.resources.place_insert_inventory_empty
import ktravel.composeapp.generated.resources.place_insert_inventory_header
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

/**
 * The places picked so far, whichever list they were picked from; confirming adds exactly these.
 *
 * Places can only be taken out here: names are edited once the places are in the trip.
 */
@Composable
internal fun SelectedPlacesInventory(
    selected: ImmutableList<PlaceCandidate>,
    onPlaceClick: (PlaceCandidate) -> Unit,
    onDetailsClick: (PlaceCandidate) -> Unit,
    onRemove: (PlaceCandidate) -> Unit,
    modifier: Modifier = Modifier,
) {
    PlaceListSection(
        title = stringResource(Res.string.place_insert_inventory_header),
        places = selected,
        isLoading = false,
        modifier = modifier,
        empty = {
            PlaceListMessage(
                icon = Res.drawable.checklist_24dp,
                text = stringResource(Res.string.place_insert_inventory_empty),
            )
        },
    ) { place ->
        PlaceCandidateRow(candidate = place, isSelected = false, onClick = { onPlaceClick(place) }) {
            PlaceDetailsButton(candidate = place, onClick = { onDetailsClick(place) })
            IconButton(onClick = { onRemove(place) }) {
                Icon(
                    painter = painterResource(Res.drawable.close),
                    contentDescription = stringResource(Res.string.place_insert_cd_remove, place.title),
                )
            }
        }
    }
}

//region Previews
@Preview(showBackground = true)
@Composable
private fun SelectedPlacesInventoryPreview() = KTravelTheme {
    Surface {
        SelectedPlacesInventory(
            selected = persistentListOf(previewTorrazzo, previewCoordinates),
            onPlaceClick = {},
            onDetailsClick = {},
            onRemove = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun SelectedPlacesInventoryEmptyPreview() = KTravelTheme {
    Surface {
        SelectedPlacesInventory(
            selected = persistentListOf(),
            onPlaceClick = {},
            onDetailsClick = {},
            onRemove = {},
        )
    }
}
//endregion Previews
