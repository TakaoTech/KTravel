package com.takaotech.ktravel.ui.planning.trip

import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import kotlinx.datetime.LocalDate
import kotlinx.datetime.format.DayOfWeekNames
import kotlinx.datetime.format.char

@Composable
fun PlanDayItem(
    day: LocalDate,
    modifier: Modifier = Modifier,
    onDateClicked: () -> Unit,
) {
    val day by remember(day) {
        derivedStateOf {
            LocalDate.Format {
                //TODO Add support for other languages
                // temporary candidate for fix https://github.com/adrcotfas/kotlinx-datetime-names
                dayOfWeek(DayOfWeekNames.ENGLISH_FULL); char(' '); day(); char('-'); monthNumber(); char('-'); year()
            }.format(
                day,
            )
        }
    }

    TextButton(
        modifier = modifier,
        onClick = onDateClicked,
    ) {
        Text(text = day)
    }
}

@Preview
@Composable
private fun PlanItemPreview() {
    PlanDayItem(
        day = LocalDate(2023, 1, 1),
        onDateClicked = {}
    )
}