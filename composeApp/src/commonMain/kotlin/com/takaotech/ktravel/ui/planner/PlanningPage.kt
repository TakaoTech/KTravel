package com.takaotech.ktravel.ui.planner

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.datasource.LoremIpsum
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.takaotech.ktravel.presentation.planner.PlanHeader
import com.takaotech.ktravel.presentation.planner.PlanningViewModel
import com.takaotech.ktravel.presentation.planner.TravelDay
import com.takaotech.ktravel.ui.components.TDaySection
import kotlinx.collections.immutable.persistentListOf
import kotlinx.datetime.LocalDate
import kotlin.time.ExperimentalTime

@Composable
fun PlanningPage(
    viewModel: PlanningViewModel,
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val planHeader = uiState.planHeader
    val days = uiState.days

    PlanningPage(
        modifier = modifier,
        planHeader = planHeader,
        days = days,
        onPlanNameChange = {
            viewModel.onPlanNameChanged(it)
        },
        onPlanDateRangeChanged = { start, end ->
            viewModel.onPlanDateChanged(start, end)
        },
        onNewStepAddRequested = { day, name ->
            viewModel.onTStepCreateRequested(day, name)
        }
    )
}

@OptIn(ExperimentalTime::class)
@Composable
fun PlanningPage(
    planHeader: PlanHeader,
    days: List<TravelDay>,
    modifier: Modifier = Modifier,
    onPlanNameChange: (TextFieldValue) -> Unit,
    onPlanDateRangeChanged: (start: Long, end: Long) -> Unit,
    onNewStepAddRequested: (LocalDate, String) -> Unit,
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

        items(
            days,
            key = {
                it.date.toEpochDays()
            }
        ) {
            var isOpen by rememberSaveable { mutableStateOf(false) }

            TDaySection(
                day = it.toString(),
                steps = it.steps,
                isOpen = isOpen,
                onDayCollapseClicked = {
                    isOpen = !isOpen
                },
                onNewStepAddRequested = { location ->
                    onNewStepAddRequested(it.date, location)
                }
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun PlanningPagePreview() {
    val loremIpsum = LoremIpsum(10).values.first()
    PlanningPage(
        planHeader = PlanHeader(
            name = TextFieldValue(loremIpsum),
        ),
        days = persistentListOf(),
        onPlanNameChange = {},
        onPlanDateRangeChanged = { start, end -> },
        onNewStepAddRequested = { _day, name -> }
    )
}