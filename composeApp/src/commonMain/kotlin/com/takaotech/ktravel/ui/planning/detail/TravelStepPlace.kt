package com.takaotech.ktravel.ui.planning.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.datasource.LoremIpsum
import androidx.compose.ui.unit.dp
import com.takaotech.ktravel.core.ui.component.PillChip
import com.takaotech.ktravel.core.ui.component.PillChipStyle
import com.takaotech.ktravel.presentation.planning.StepUi
import com.takaotech.ktravel.presentation.planning.VisitScheduleUi
import com.takaotech.ktravel.ui.theme.KTravelTheme
import kotlinx.datetime.LocalTime
import ktravel.composeapp.generated.resources.Res
import ktravel.composeapp.generated.resources.arrow_downward
import ktravel.composeapp.generated.resources.arrow_upward
import ktravel.composeapp.generated.resources.delete
import ktravel.composeapp.generated.resources.north_east
import ktravel.composeapp.generated.resources.photo_camera
import ktravel.composeapp.generated.resources.planning_detail_cd_delete_step
import ktravel.composeapp.generated.resources.planning_detail_cd_move_step_down
import ktravel.composeapp.generated.resources.planning_detail_cd_move_step_up
import ktravel.composeapp.generated.resources.south_east
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

private val ThumbnailSize = 44.dp

/**
 * Content (right-hand side of the timeline) of a place: the gutter node is drawn by the calling
 * timeline row. The card carries the identity of the stop — thumbnail, name, description — and its
 * two schedule chips, so the whole visit reads without opening the detail.
 *
 * [isDestination] marks the arrival point of the journey: it holds no visit schedule, so it shows
 * no chips.
 */
@Composable
internal fun TravelStepPlace(
    step: StepUi.Place,
    isDestination: Boolean,
    onStepClick: (String) -> Unit,
    onStepDeleteClicked: () -> Unit,
    onStepMoveUp: (String) -> Unit,
    onStepMoveDown: (String) -> Unit,
    onSetArrivalTime: (LocalTime) -> Unit,
    onSetDepartureTime: (LocalTime) -> Unit,
    modifier: Modifier = Modifier,
) {
    // TODO Replace with the real place description once the model carries one.
    val description = remember(step.id) { LoremIpsum(words = 6).values.first() }

    Card(
        onClick = { onStepClick(step.id) },
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        ),
    ) {
        Row(
            modifier = Modifier.padding(start = 8.dp, end = 4.dp, top = 8.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            PlaceThumbnail(modifier = Modifier.align(Alignment.Top))

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 12.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Text(
                    modifier = Modifier.basicMarquee(),
                    text = step.name,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                )

                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )

                if (!isDestination) {
                    ScheduleChips(
                        stepId = step.id,
                        schedule = step.schedule,
                        onSetArrivalTime = onSetArrivalTime,
                        onSetDepartureTime = onSetDepartureTime,
                    )
                }
            }

            IconButton(
                modifier = Modifier.testTag(StepsPaneTestTags.deleteStepTag(step.id)),
                onClick = onStepDeleteClicked,
            ) {
                Icon(
                    painter = painterResource(Res.drawable.delete),
                    contentDescription = stringResource(Res.string.planning_detail_cd_delete_step),
                )
            }

            Column {
                IconButton(
                    modifier = Modifier.testTag(StepsPaneTestTags.moveStepUpTag(step.id)),
                    onClick = { onStepMoveUp(step.id) },
                ) {
                    Icon(
                        painter = painterResource(Res.drawable.arrow_upward),
                        contentDescription = stringResource(
                            Res.string.planning_detail_cd_move_step_up,
                        ),
                    )
                }

                IconButton(
                    modifier = Modifier.testTag(StepsPaneTestTags.moveStepDownTag(step.id)),
                    onClick = { onStepMoveDown(step.id) },
                ) {
                    Icon(
                        painter = painterResource(Res.drawable.arrow_downward),
                        contentDescription = stringResource(
                            Res.string.planning_detail_cd_move_step_down,
                        ),
                    )
                }
            }

//            TODO add support D&D
//            Icon(
//                modifier = Modifier.padding(horizontal = 4.dp).size(20.dp),
//                painter = painterResource(Res.drawable.drag_indicator),
//                contentDescription = stringResource(Res.string.planning_detail_cd_reorder_step),
//                tint = MaterialTheme.colorScheme.onSurfaceVariant,
//            )
        }
    }
}

/** Arrival and departure of the visit, side by side, each opening its own time picker. */
@Composable
private fun ScheduleChips(
    stepId: String,
    schedule: VisitScheduleUi?,
    onSetArrivalTime: (LocalTime) -> Unit,
    onSetDepartureTime: (LocalTime) -> Unit,
) {
    ScheduleTimeEditor(
        startTime = schedule?.startTime,
        endTime = schedule?.endTime,
        onStartConfirm = onSetArrivalTime,
        onEndConfirm = onSetDepartureTime,
    ) { scope ->
        // Wrapping keeps both chips readable when the actions squeeze the card on a narrow phone.
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            TimeChip(
                modifier = Modifier.testTag(StepsPaneTestTags.arrivalChipTag(stepId)),
                display = scope.startDisplay,
                isSet = schedule?.startTime != null,
                icon = Res.drawable.south_east,
                contentDescription = scope.startContentDescription,
                onClick = scope.openStartPicker,
            )
            TimeChip(
                modifier = Modifier.testTag(StepsPaneTestTags.departureChipTag(stepId)),
                display = scope.endDisplay,
                isSet = schedule?.endTime != null,
                icon = Res.drawable.north_east,
                contentDescription = scope.endContentDescription,
                onClick = scope.openEndPicker,
            )
        }
    }
}

/**
 * Compact time pill: filled once the time is set, dashed outline while it still reads `--:--`, so
 * an unplanned stop is visible at a glance without another label.
 */
@Composable
private fun TimeChip(
    display: String,
    isSet: Boolean,
    icon: DrawableResource,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    PillChip(
        modifier = modifier,
        text = display,
        icon = icon,
        onClick = onClick,
        style = if (isSet) PillChipStyle.Filled else PillChipStyle.Dashed,
        contentColor = if (isSet) {
            MaterialTheme.colorScheme.onSurface
        } else {
            MaterialTheme.colorScheme.onSurfaceVariant
        },
        contentDescription = contentDescription,
    )
}

/** Placeholder of the place picture: tonal square with a camera glyph. */
@Composable
private fun PlaceThumbnail(modifier: Modifier = Modifier) {
    // TODO Show the place photo once places carry one.
    Box(
        modifier = modifier
            .size(ThumbnailSize)
            .clip(MaterialTheme.shapes.medium)
            .background(MaterialTheme.colorScheme.surfaceContainerHighest),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            modifier = Modifier.size(22.dp),
            painter = painterResource(Res.drawable.photo_camera),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
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
                endTime = LocalTime(11, 0),
            ),
        ),
        isDestination = false,
        onStepClick = {},
        onStepDeleteClicked = {},
        onStepMoveDown = {},
        onStepMoveUp = {},
        onSetArrivalTime = {},
        onSetDepartureTime = {},
    )
}
