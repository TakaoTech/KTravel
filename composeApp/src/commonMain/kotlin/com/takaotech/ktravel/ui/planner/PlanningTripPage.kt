package com.takaotech.ktravel.ui.planner

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.datasource.LoremIpsum
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.takaotech.ktravel.presentation.planner.Place
import com.takaotech.ktravel.presentation.planner.PlanHeader
import com.takaotech.ktravel.presentation.planner.PlanningViewModel
import com.takaotech.ktravel.presentation.planner.TravelDay
import com.takaotech.ktravel.ui.place.PlaceItem
import com.takaotech.ktravel.ui.planning.PlanningHeader
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable
import ktravel.composeapp.generated.resources.Res
import ktravel.composeapp.generated.resources.edit
import org.jetbrains.compose.resources.painterResource
import kotlin.time.ExperimentalTime

@Serializable
object PlanningTripPageNavigation

@Composable
fun PlanningTripPage(
    viewModel: PlanningViewModel,
    modifier: Modifier = Modifier,
    onAddPlaceClicked: () -> Unit,
    onDateClicked: (id: String) -> Unit,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val planHeader = uiState.planHeader
    val days = uiState.days

    PlanningTripPage(
        modifier = modifier,
        planHeader = planHeader,
        places = uiState.places,
        days = days,
        onPlanNameChange = {
            viewModel.onPlanNameChanged(it)
        },
        onPlanDateRangeChanged = { start, end ->
            viewModel.onPlanDateChanged(start, end)
        },
        onAddPlaceClicked = onAddPlaceClicked,
        onDateClicked = onDateClicked
    )
}

@OptIn(ExperimentalTime::class)
@Composable
private fun PlanningTripPage(
    planHeader: PlanHeader,
    places: PersistentList<Place>,
    days: ImmutableList<TravelDay>,
    modifier: Modifier = Modifier,
    onPlanNameChange: (TextFieldValue) -> Unit,
    onPlanDateRangeChanged: (start: Long, end: Long) -> Unit,

    onAddPlaceClicked: () -> Unit,

    onDateClicked: (id: String) -> Unit,
) {
    LazyColumn(
        modifier = modifier,
    ) {
        item {
            PlanningHeader(
                name = planHeader.name,
                onNameChange = onPlanNameChange,
                startDateMillis = planHeader.period.start.toEpochMilliseconds(),
                endDateMillis = planHeader.period.end.toEpochMilliseconds(),
                onPlanDateRangeChanged = onPlanDateRangeChanged
            )
        }

        itemsIndexed(
            items = places,
            key = { _, place ->
                place.id
            }
        ) { _, place ->
            PlaceItem(
                modifier = Modifier.padding(16.dp),
                name = place.name,
                onDeleteClicked = {

                }
            )
        }

        item {
            TextButton(
                onClick = onAddPlaceClicked
            ) {
                //TODO Change icon with add
                Icon(
                    painter = painterResource(Res.drawable.edit),
                    contentDescription = null
                )
                Text("Add place")
            }
        }

        item {
            Text(
                modifier = Modifier.padding(16.dp),
                text = "Itinerario",
                style = MaterialTheme.typography.labelLarge
            )
        }

        itemsIndexed(
            items = days,
            key = { _: Int, day: TravelDay ->
                day.date.toEpochDays()
            }
        ) { _, day ->
            PlanDayItem(
                day = day.date,
                onDateClicked = {
                    onDateClicked(day.id)
                }
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun PlanningPagePreview() {
    LoremIpsum(10).values.first()
    PlanningTripPage(
        planHeader = PlanHeader(
            name = TextFieldValue("Viaggio in Italia"),
        ),
        places = persistentListOf(),
        days = persistentListOf(
            TravelDay(
                date = LocalDate(2024, 6, 15),
                steps = persistentListOf(
                    TravelDay.Step.Place(location = "Roma - Colosseo"),
                    TravelDay.Step.Place(location = "Fontana di Trevi"),
                    TravelDay.Step.Place(location = "Pantheon")
                )
            ),
            TravelDay(
                date = LocalDate(2024, 6, 16),
                steps = persistentListOf(
                    TravelDay.Step.Place(location = "Musei Vaticani"),
                    TravelDay.Step.Place(location = "Cappella Sistina"),
                    TravelDay.Step.Place(location = "Piazza San Pietro")
                )
            ),
            TravelDay(
                date = LocalDate(2024, 6, 17),
                steps = persistentListOf(
                    TravelDay.Step.Place(location = "Firenze - Duomo"),
                    TravelDay.Step.Place(location = "Galleria degli Uffizi"),
                    TravelDay.Step.Place(location = "Ponte Vecchio"),
                    TravelDay.Step.Place(location = "Piazzale Michelangelo")
                )
            )
        ),
        onPlanNameChange = {},
        onPlanDateRangeChanged = { start, end -> },
        onAddPlaceClicked = {},
        onDateClicked = {},
    )
}