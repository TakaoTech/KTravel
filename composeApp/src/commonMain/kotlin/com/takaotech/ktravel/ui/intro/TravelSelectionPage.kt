package com.takaotech.ktravel.ui.intro

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.material3.*
import androidx.compose.material3.SearchBarDefaults.InputField
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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

@Composable
fun TravelSelectionPage(
    viewModel: TravelSelectionViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    TravelSelectionPage(
        travelList = uiState.travelList,
        newTravelClick = {
//            viewModel.createTravelPlan(
//                name = "",
//                periodStart = 0L,
//                periodEnd = 0L
//            )
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TravelSelectionPage(
    travelList: PersistentList<TravelSummaryUiState>,
    modifier: Modifier = Modifier,
    newTravelClick: () -> Unit
) {
    Scaffold(
        modifier = modifier,
        floatingActionButton = {
            FloatingActionButton(
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
                        .padding(horizontal = 16.dp),
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
                        .padding(horizontal = 16.dp),
                    name = travel.name,
                    startDate = travel.periodStart.toString(),
                    endDate = travel.periodEnd.toString(),
                    onClick = {}
                )
            }
        }
    }


}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun TravelItem(
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
        newTravelClick = {}
    )
}
