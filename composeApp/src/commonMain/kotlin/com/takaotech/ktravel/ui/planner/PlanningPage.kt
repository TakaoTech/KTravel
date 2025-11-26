package com.takaotech.ktravel.ui.planner

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.datasource.LoremIpsum
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.takaotech.ktravel.presentation.planner.PlanHeader
import com.takaotech.ktravel.presentation.planner.PlanningViewModel
import com.takaotech.ktravel.presentation.planner.TravelDay
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.datetime.LocalDate
import kotlinx.datetime.format.DayOfWeekNames
import kotlinx.datetime.format.char
import kotlinx.serialization.Serializable
import kotlin.time.ExperimentalTime

@Serializable
object PlanningPage

@Composable
fun PlanningPage(
    viewModel: PlanningViewModel,
    modifier: Modifier = Modifier,
    onDateClicked: (id: String) -> Unit,
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
        onDateClicked = onDateClicked
    )
}

@OptIn(ExperimentalTime::class)
@Composable
fun PlanningPage(
    planHeader: PlanHeader,
    days: ImmutableList<TravelDay>,
    modifier: Modifier = Modifier,
    onPlanNameChange: (TextFieldValue) -> Unit,
    onPlanDateRangeChanged: (start: Long, end: Long) -> Unit,

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
            items = days,
            key = { _: Int, it: TravelDay ->
                it.date.toEpochDays()
            }
        ) { _, it ->
            val day by remember(it.date) {
                derivedStateOf {
                    LocalDate.Format {
                        //TODO Add support for other languages
                        dayOfWeek(DayOfWeekNames.ENGLISH_FULL); char(' '); day(); char('-'); monthNumber(); char('-'); year();
                    }.format(
                        it.date,
                    )
                }
            }

            TextButton(
                onClick = {
                    onDateClicked(it.id)
                },
            ) {
                Text(text = day)
            }
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
        onDateClicked = {},
    )
}