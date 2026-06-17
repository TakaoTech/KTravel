package com.takaotech.ktravel.ui.planning.detail

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.takaotech.ktravel.presentation.planning.TravelDay
import ktravel.composeapp.generated.resources.Res
import ktravel.composeapp.generated.resources.arrow_downward
import ktravel.composeapp.generated.resources.arrow_upward
import ktravel.composeapp.generated.resources.delete
import org.jetbrains.compose.resources.painterResource

@Composable
internal fun TravelStepPlace(
    step: TravelDay.Step.Place,
    onStepDeleteClicked: () -> Unit,
    onStepMoveUp: (String) -> Unit,
    onStepMoveDown: (String) -> Unit
) {
    Column {
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
                onClick = onStepDeleteClicked
            ) {
                Icon(
                    painter = painterResource(Res.drawable.delete),
                    contentDescription = null,
                )
            }

            IconButton(
                modifier = Modifier.padding(top = 8.dp),
                onClick = {
                    onStepMoveUp(step.id)
                }
            ) {
                Icon(
                    painter = painterResource(Res.drawable.arrow_upward),
                    contentDescription = null,
                )
            }

            IconButton(
                modifier = Modifier.padding(top = 8.dp),
                onClick = {
                    onStepMoveDown(step.id)
                }
            ) {
                Icon(
                    painter = painterResource(Res.drawable.arrow_downward),
                    contentDescription = null,
                )
            }
        }
    }
}

@Preview
@Composable
private fun TravelStepPlacePreview() {
    TravelStepPlace(
        step = TravelDay.Step.Place(
            location = "Tokyo Tower",
            lat = 0.0,
            lng = 0.0
        ),
        onStepDeleteClicked = {},
        onStepMoveDown = {},
        onStepMoveUp = {}
    )
}

@Composable
fun TravelTransportStepAdd(
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    TextButton(
        modifier = modifier,
        onClick = onClick
    ) {
        Text("Add Transport")
    }
}