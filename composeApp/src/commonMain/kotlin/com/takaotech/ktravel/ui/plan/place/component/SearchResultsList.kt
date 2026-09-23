package com.takaotech.ktravel.ui.plan.place.component

import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.takaotech.ktravel.domain.search.model.PlaceCandidate
import com.takaotech.ktravel.presentation.place.PlaceListState
import com.takaotech.ktravel.presentation.place.PlaceSearchProblem
import com.takaotech.ktravel.ui.plan.place.previewLongAddress
import com.takaotech.ktravel.ui.plan.place.previewTorrazzo
import com.takaotech.ktravel.ui.theme.KTravelTheme
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import ktravel.composeapp.generated.resources.Res
import ktravel.composeapp.generated.resources.error
import ktravel.composeapp.generated.resources.place_insert_results_empty
import ktravel.composeapp.generated.resources.place_insert_results_header
import ktravel.composeapp.generated.resources.search
import org.jetbrains.compose.resources.stringResource

/**
 * The full answer to what is typed in the search bar; the dropdown under the bar only shows its
 * first rows.
 */
@Composable
internal fun SearchResultsList(
    state: PlaceListState,
    selected: ImmutableList<PlaceCandidate>,
    onPlaceClick: (PlaceCandidate) -> Unit,
    onDetailsClick: (PlaceCandidate) -> Unit,
    onToggleSelection: (PlaceCandidate) -> Unit,
    modifier: Modifier = Modifier,
) {
    val selectedIds = remember(selected) { selected.mapTo(HashSet()) { it.id } }
    PlaceListSection(
        title = stringResource(Res.string.place_insert_results_header),
        places = state.places,
        isLoading = state.isLoading,
        modifier = modifier,
        empty = {
            if (state.problem != null) {
                PlaceListMessage(icon = Res.drawable.error, text = stringResource(state.problem.message))
            } else {
                PlaceListMessage(
                    icon = Res.drawable.search,
                    text = stringResource(Res.string.place_insert_results_empty),
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
private fun SearchResultsListPreview() = KTravelTheme {
    Surface {
        SearchResultsList(
            state = PlaceListState(places = persistentListOf(previewTorrazzo, previewLongAddress), isLoading = true),
            selected = persistentListOf(previewTorrazzo),
            onPlaceClick = {},
            onDetailsClick = {},
            onToggleSelection = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun SearchResultsListProblemPreview() = KTravelTheme {
    Surface {
        SearchResultsList(
            state = PlaceListState(problem = PlaceSearchProblem.RATE_LIMITED),
            selected = persistentListOf(),
            onPlaceClick = {},
            onDetailsClick = {},
            onToggleSelection = {},
        )
    }
}
//endregion Previews
