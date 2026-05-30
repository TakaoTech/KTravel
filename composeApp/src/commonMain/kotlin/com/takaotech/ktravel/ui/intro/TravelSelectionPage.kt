package com.takaotech.ktravel.ui.intro

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults.InputField
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSearchBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.takaotech.ktravel.core.toLocalDate
import com.takaotech.ktravel.presentation.intro.TravelSelectionViewModel
import com.takaotech.ktravel.presentation.intro.TravelSummaryUiState
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.serialization.Serializable
import ktravel.composeapp.generated.resources.Res
import ktravel.composeapp.generated.resources.add
import ktravel.composeapp.generated.resources.date_range
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.viewmodel.koinViewModel
import kotlin.time.Clock

@Serializable
object TravelSelectionPage

internal object TravelSelectionTestTags {
    const val SEARCH_BAR = "travel_selection_search_bar"
    const val FAB_NEW_TRAVEL = "travel_selection_fab"
    fun travelItemTag(id: String) = "travel_item_$id"
}

@Composable
fun TravelSelectionPage(
    viewModel: TravelSelectionViewModel = koinViewModel(),
    onTravelClick: (id: String) -> Unit,
    onNewTravelClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    TravelSelectionPage(
        travelList = uiState.travelList,
        onTravelClick = onTravelClick,
        newTravelClick = onNewTravelClick
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun TravelSelectionPage(
    travelList: PersistentList<TravelSummaryUiState>,
    modifier: Modifier = Modifier,
    onTravelClick: (id: String) -> Unit,
    newTravelClick: () -> Unit
) {
    Scaffold(
        modifier = modifier,
        floatingActionButton = {
            FloatingActionButton(
                modifier = Modifier.testTag(TravelSelectionTestTags.FAB_NEW_TRAVEL),
                onClick = newTravelClick
            ) {
                Icon(
                    painter = painterResource(Res.drawable.add),
                    contentDescription = null
                )
            }
        }
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(it),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            stickyHeader {
                val searchBarState = rememberSearchBarState()
                var textFieldState by remember { mutableStateOf(TextFieldState()) }

                SearchBar(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .testTag(TravelSelectionTestTags.SEARCH_BAR),
                    state = searchBarState,
                    inputField = {
                        InputField(
                            placeholder = { Text("Cerca un viaggio creato") },
                            textFieldState = textFieldState,
                            searchBarState = searchBarState,
                            onSearch = {

                            }
                        )
                    }
                )
            }

            items(
                key = { travel -> travel.id },
                items = travelList
            ) { travel ->
                //TODO Format date correctly
                TravelItem(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .testTag(TravelSelectionTestTags.travelItemTag(travel.id)),
                    name = travel.name,
                    startDate = travel.periodStart.toString(),
                    endDate = travel.periodEnd.toString(),
                    onClick = {
                        onTravelClick(travel.id)
                    }
                )
            }
        }
    }


}

@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun TravelItem(
    name: String,
    startDate: String?,
    endDate: String?,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier,
        onClick = onClick
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        Text(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .padding(bottom = 8.dp),
            text = name,
            style = MaterialTheme.typography.titleLarge,
        )

        Row(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(Res.drawable.date_range),
                contentDescription = null,
            )

            Text(
                modifier = Modifier.padding(start = 8.dp),
                text = "$startDate - $endDate",
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@PreviewScreenSizes
@Composable
private fun TravelSelectionPagePreview() {
    TravelSelectionPage(
        travelList = persistentListOf(
            TravelSummaryUiState(
                id = "1",
                name = "Viaggio a Tokyo",
                periodStart = Clock.System.now().toLocalDate(),
                periodEnd = Clock.System.now().toLocalDate()
            ),
            TravelSummaryUiState(
                id = "2",
                name = "Weekend a Roma",
                periodStart = Clock.System.now().toLocalDate(),
                periodEnd = Clock.System.now().toLocalDate()
            )
        ),
        onTravelClick = {},
        newTravelClick = {}
    )
}
