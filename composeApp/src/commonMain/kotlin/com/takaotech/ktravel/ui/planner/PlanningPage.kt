package com.takaotech.ktravel.ui.planner

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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

        itemsIndexed(
            items = days,
            key = { _: Int, it: TravelDay ->
                it.date.toEpochDays()
            }
        ) { index, it ->
            var isOpen by rememberSaveable { mutableStateOf(true) }
            val day by remember(it.date) {
                derivedStateOf {
                    LocalDate.Formats.ISO.format(it.date)
                }
            }

            TDaySection(
                day = day,
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
            name = TextFieldValue("Viaggio in Italia"),
        ),
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
        onNewStepAddRequested = { _day, name -> }
    )
}