package com.takaotech.ktravel.ui.planning.detail

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.takaotech.ktravel.presentation.planning.StepUi
import com.takaotech.ktravel.presentation.planning.VisitScheduleUi
import com.takaotech.ktravel.ui.theme.KTravelTheme
import kotlinx.datetime.LocalTime
import ktravel.composeapp.generated.resources.Res
import ktravel.composeapp.generated.resources.arrow_downward
import ktravel.composeapp.generated.resources.arrow_upward
import ktravel.composeapp.generated.resources.delete
import ktravel.composeapp.generated.resources.planning_detail_add_transport
import ktravel.composeapp.generated.resources.planning_detail_cd_delete_step
import ktravel.composeapp.generated.resources.planning_detail_cd_move_step_down
import ktravel.composeapp.generated.resources.planning_detail_cd_move_step_up
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

/**
 * Content (right-hand side of the timeline) of a place: the gutter node is drawn by the calling
 * timeline row. Shows the place name in a clickable card together with the reorder/delete actions.
 */
@Composable
internal fun TravelStepPlace(
    step: StepUi.Place,
    onStepClick: (String) -> Unit,
    onStepDeleteClicked: () -> Unit,
    onStepMoveUp: (String) -> Unit,
    onStepMoveDown: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = { onStepClick(step.id) },
        modifier = modifier.fillMaxWidth()
    ) {
        Column {
            Row(
                modifier = Modifier.padding(start = 16.dp, end = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    modifier = Modifier.weight(1f).padding(vertical = 12.dp),
                    text = step.name,
                    style = MaterialTheme.typography.titleMedium
                )

                IconButton(onClick = onStepDeleteClicked) {
                    Icon(
                        painter = painterResource(Res.drawable.delete),
                        contentDescription = stringResource(Res.string.planning_detail_cd_delete_step),
                    )
                }

                IconButton(onClick = { onStepMoveUp(step.id) }) {
                    Icon(
                        painter = painterResource(Res.drawable.arrow_upward),
                        contentDescription = stringResource(Res.string.planning_detail_cd_move_step_up),
                    )
                }

                IconButton(onClick = { onStepMoveDown(step.id) }) {
                    Icon(
                        painter = painterResource(Res.drawable.arrow_downward),
                        contentDescription = stringResource(Res.string.planning_detail_cd_move_step_down),
                    )
                }
            }
        }
    }
}

@Preview
@Composable
private fun TravelStepPlacePreview() = KTravelTheme {
    TravelStepPlace(
        step = StepUi.Place(
            name = "Tokyo Tower",
            lat = 0.0,
            lng = 0.0,
            schedule = VisitScheduleUi(
                startTime = LocalTime(9, 30),
                endTime = LocalTime(11, 0)
            )
        ),
        onStepClick = {},
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
        Text(stringResource(Res.string.planning_detail_add_transport))
    }
}
