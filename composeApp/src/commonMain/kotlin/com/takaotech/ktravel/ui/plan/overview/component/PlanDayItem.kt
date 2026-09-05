package com.takaotech.ktravel.ui.plan.overview.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.takaotech.ktravel.presentation.plan.StepUi
import com.takaotech.ktravel.presentation.plan.TravelDayUi
import com.takaotech.ktravel.ui.plan.preview.TravelDayStepPreviewParameterProvider
import com.takaotech.ktravel.ui.shared.format.formatWeekdayDayMonthYear
import com.takaotech.ktravel.ui.theme.KTravelTheme
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toPersistentList
import kotlinx.datetime.LocalDate
import ktravel.composeapp.generated.resources.Res
import ktravel.composeapp.generated.resources.keyboard_arrow_down
import ktravel.composeapp.generated.resources.place
import ktravel.composeapp.generated.resources.planning_trip_cd_collapse_day
import ktravel.composeapp.generated.resources.planning_trip_cd_expand_day
import ktravel.composeapp.generated.resources.planning_trip_day_empty
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

internal const val MIN_ITEM_COLLAPSE = 5

/**
 * A single day of the itinerary: the date followed by the places already planned for it.
 * The whole card is clickable and opens the day detail.
 */
@Composable
fun PlanDayItem(
    day: LocalDate,
    placeSteps: ImmutableList<StepUi.Place>,
    modifier: Modifier = Modifier,
    onDateClicked: () -> Unit,
) {
    val day by remember(day) {
        derivedStateOf {
            day.formatWeekdayDayMonthYear()
        }
    }

    var isExpanded by rememberSaveable { mutableStateOf(true) }

    // The collapse affordance is offered only when the day holds enough places to be worth hiding.
    val isCollapsible by remember(placeSteps) {
        derivedStateOf { placeSteps.size >= MIN_ITEM_COLLAPSE }
    }
    val chevronRotation by animateFloatAsState(
        targetValue = if (isExpanded) 180f else 0f,
        label = "PlanDayItemChevronRotation",
    )

    OutlinedCard(
        modifier = modifier.fillMaxWidth(),
        onClick = onDateClicked,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        ),
        shape = MaterialTheme.shapes.large,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .minimumInteractiveComponentSize(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    modifier = Modifier.weight(1f),
                    text = day,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary,
                )

                if (isCollapsible) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        AnimatedVisibility(
                            visible = !isExpanded,
                            enter = scaleIn(),
                            exit = scaleOut(),
                        ) {
                            val containerColor = MaterialTheme.colorScheme.tertiaryContainer

                            Text(
                                modifier = Modifier
                                    .padding(horizontal = 16.dp)
                                    .drawBehind {
                                        drawCircle(
                                            color = containerColor,
                                            radius = (this.size.maxDimension / 4) * 3,
                                        )
                                    },
                                text = placeSteps.size.toString(),
                            )
                        }

                        IconButton(onClick = { isExpanded = !isExpanded }) {
                            Icon(
                                modifier = Modifier
                                    .size(18.dp)
                                    .rotate(chevronRotation),
                                painter = painterResource(Res.drawable.keyboard_arrow_down),
                                contentDescription = stringResource(
                                    if (isExpanded) {
                                        Res.string.planning_trip_cd_collapse_day
                                    } else {
                                        Res.string.planning_trip_cd_expand_day
                                    },
                                ),
                                tint = MaterialTheme.colorScheme.primary,
                            )
                        }
                    }
                }
            }

            if (placeSteps.isEmpty()) {
                Text(
                    text = stringResource(Res.string.planning_trip_day_empty),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else {
                AnimatedVisibility(
                    visible = isExpanded || !isCollapsible,
                    enter = expandVertically(),
                    exit = shrinkVertically(),
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        placeSteps.forEach { step ->
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    modifier = Modifier.size(18.dp),
                                    painter = painterResource(Res.drawable.place),
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                )

                                Text(
                                    modifier = Modifier.padding(start = 8.dp),
                                    text = step.name,
                                    style = MaterialTheme.typography.bodyMedium,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

//region Previews
@Preview
@Composable
private fun PlanItemPreview() = KTravelTheme {
    PlanDayItem(
        day = LocalDate(2023, 1, 1),
        // The provider alternates place and transport steps, so it takes 2x steps to get x places.
        placeSteps = TravelDayUi(
            date = LocalDate(2023, 1, 1),
            steps = TravelDayStepPreviewParameterProvider(MIN_ITEM_COLLAPSE * 2)
                .values.toList().toPersistentList(),
        ).placeSteps,
        onDateClicked = {},
    )
}

@Preview
@Composable
private fun PlanItemNotCollapsiblePreview() = KTravelTheme {
    PlanDayItem(
        day = LocalDate(2023, 1, 1),
        placeSteps = TravelDayUi(
            date = LocalDate(2023, 1, 1),
            steps = TravelDayStepPreviewParameterProvider((MIN_ITEM_COLLAPSE - 1) * 2)
                .values.toList().toPersistentList(),
        ).placeSteps,
        onDateClicked = {},
    )
}

@Preview
@Composable
private fun PlanItemEmptyPreview() = KTravelTheme {
    PlanDayItem(
        day = LocalDate(2023, 1, 1),
        placeSteps = persistentListOf(),
        onDateClicked = {},
    )
}
//endregion Previews
