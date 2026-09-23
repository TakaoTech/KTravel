package com.takaotech.ktravel.ui.plan.place.component

import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.takaotech.ktravel.domain.search.model.PlaceCandidate
import com.takaotech.ktravel.domain.search.model.PlaceCategory
import com.takaotech.ktravel.presentation.place.PlaceListState
import com.takaotech.ktravel.ui.plan.place.previewTorrazzo
import com.takaotech.ktravel.ui.plan.place.previewTrattoria
import com.takaotech.ktravel.ui.theme.KTravelTheme
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import ktravel.composeapp.generated.resources.Res
import ktravel.composeapp.generated.resources.error
import ktravel.composeapp.generated.resources.filter_alt_off
import ktravel.composeapp.generated.resources.place_insert_nearby_empty
import ktravel.composeapp.generated.resources.place_insert_nearby_empty_filtered
import ktravel.composeapp.generated.resources.place_insert_nearby_header
import ktravel.composeapp.generated.resources.place_insert_show_all
import ktravel.composeapp.generated.resources.travel_explore
import org.jetbrains.compose.resources.stringResource

/**
 * The places inside the area framed on the map.
 *
 * @param category The filter in force: an empty list says so, and offers to remove it.
 * @param onShowAllClick Removes the category filter.
 */
@Composable
internal fun NearbyPlacesList(
    state: PlaceListState,
    category: PlaceCategory?,
    selected: ImmutableList<PlaceCandidate>,
    onPlaceClick: (PlaceCandidate) -> Unit,
    onDetailsClick: (PlaceCandidate) -> Unit,
    onToggleSelection: (PlaceCandidate) -> Unit,
    onShowAllClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val selectedIds = remember(selected) { selected.mapTo(HashSet()) { it.id } }
    PlaceListSection(
        title = stringResource(Res.string.place_insert_nearby_header),
        places = state.places,
        isLoading = state.isLoading,
        modifier = modifier,
        empty = {
            when {
                state.problem != null -> PlaceListMessage(
                    icon = Res.drawable.error,
                    text = stringResource(state.problem.message),
                )

                category != null -> PlaceListMessage(
                    icon = Res.drawable.filter_alt_off,
                    text = stringResource(Res.string.place_insert_nearby_empty_filtered),
                    action = {
                        TextButton(onClick = onShowAllClick) {
                            Text(stringResource(Res.string.place_insert_show_all))
                        }
                    },
                )

                else -> PlaceListMessage(
                    icon = Res.drawable.travel_explore,
                    text = stringResource(Res.string.place_insert_nearby_empty),
                )
            }
        },
    ) { place ->
        val isSelected = place.id in selectedIds
        PlaceCandidateRow(candidate = place, isSelected = isSelected, onClick = { onPlaceClick(place) }) {
            PlaceDetailsButton(candidate = place, onClick = { onDetailsClick(place) })
            PlaceSelectionButton(candidate = place, isSelected = isSelected, onClick = { onToggleSelection(place) })
        }
    }
}

//region Previews
@Preview(showBackground = true)
@Composable
private fun NearbyPlacesListPreview() = KTravelTheme {
    Surface {
        NearbyPlacesList(
            state = PlaceListState(places = persistentListOf(previewTorrazzo, previewTrattoria)),
            category = null,
            selected = persistentListOf(previewTrattoria),
            onPlaceClick = {},
            onDetailsClick = {},
            onToggleSelection = {},
            onShowAllClick = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun NearbyPlacesListEmptyFilteredPreview() = KTravelTheme {
    Surface {
        NearbyPlacesList(
            state = PlaceListState(),
            category = PlaceCategory.NATURE,
            selected = persistentListOf(),
            onPlaceClick = {},
            onDetailsClick = {},
            onToggleSelection = {},
            onShowAllClick = {},
        )
    }
}
//endregion Previews
