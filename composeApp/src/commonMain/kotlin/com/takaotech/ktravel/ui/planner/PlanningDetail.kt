package com.takaotech.ktravel.ui.planner

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.takaotech.ktravel.presentation.planner.Place
import com.takaotech.ktravel.presentation.planner.TravelDay
import com.takaotech.ktravel.ui.place.PlaceItem
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.serialization.Serializable
import ktravel.composeapp.generated.resources.Res
import ktravel.composeapp.generated.resources.delete
import org.jetbrains.compose.resources.painterResource

@Serializable
data class PlanningDetailPageNavigation(val id: String)

@Deprecated("Deprecated")
@Composable
fun PlanningDetailPage(
    steps: ImmutableList<TravelDay.Step>,
    places: PersistentList<Place>,
    modifier: Modifier = Modifier,
    onAddPlaceClick: () -> Unit,
    onStepDeleteClicked: (String) -> Unit,
) {
    LazyColumn(modifier = modifier) {
        item {
            AddPlaceButton(
                onClick = onAddPlaceClick
            )
        }

        items(places) { place ->
            // TODO Add Hours
            // TODO Add image
            PlaceItem(
                modifier = Modifier.padding(16.dp),
                name = place.name,
                onDeleteClicked = {
                    // TODO Function for remove place
                    //  Move to all place list or permanent delete?
                }
            )
        }

        itemsIndexed(steps) { index, step ->
            when (step) {
                is TravelDay.Step.Place -> {
                    Row {
                        Card(
                            modifier = Modifier
                                .weight(1f)
                                .padding(
                                    vertical = 12.dp,
                                ),
                        ) {
                            Text(
                                modifier = Modifier.padding(16.dp),
                                text = step.location
                            )
                        }

                        IconButton(
                            modifier = Modifier.padding(top = 8.dp),
                            onClick = {
                                onStepDeleteClicked(step.id)
                            }
                        ) {
                            Icon(
                                painter = painterResource(Res.drawable.delete),
                                contentDescription = null,
                            )
                        }
                    }

                    if ((index < steps.lastIndex) && steps.getOrNull(index + 1) !is TravelDay.Step.Transport) {
                        TextButton(
                            onClick = {

                            }
                        ) {
                            Text("Add Transport")
                        }
                    }
                }

                is TravelDay.Step.Transport -> {
                    Icon(painter = painterResource(step.type.icon), contentDescription = null)
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun PlanningDetailPagereview() {
    PlanningDetailPage(
        steps = persistentListOf(
            TravelDay.Step.Place(location = "Roma - Colosseo"),
            TravelDay.Step.Transport(type = TravelDay.Step.Transport.Type.BUS),
            TravelDay.Step.Place(location = "Fontana di Trevi"),
            TravelDay.Step.Transport(type = TravelDay.Step.Transport.Type.CAR),
            TravelDay.Step.Place(location = "Pantheon"),
            TravelDay.Step.Transport(type = TravelDay.Step.Transport.Type.TRAIN),
            TravelDay.Step.Place(location = "Piazza Navona"),
            TravelDay.Step.Place(location = "Piazza Navona"),
        ),
        onAddPlaceClick = {},
        onStepDeleteClicked = {},
        places = persistentListOf()
    )
}