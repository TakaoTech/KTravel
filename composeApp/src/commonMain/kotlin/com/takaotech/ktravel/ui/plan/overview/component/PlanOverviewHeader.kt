package com.takaotech.ktravel.ui.plan.overview.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDateRangePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.datasource.LoremIpsum
import androidx.compose.ui.unit.dp
import com.takaotech.ktravel.ui.shared.component.TravelDateRangePicker
import com.takaotech.ktravel.ui.theme.KTravelTheme
import ktravel.composeapp.generated.resources.Res
import ktravel.composeapp.generated.resources.edit
import ktravel.composeapp.generated.resources.planning_page_trip_name_label
import ktravel.composeapp.generated.resources.planning_trip_cd_edit_name
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import kotlin.time.Clock
import kotlin.time.Duration.Companion.days
import kotlin.time.DurationUnit
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class, ExperimentalMaterial3Api::class)
@Composable
fun PlanOverviewHeader(
    name: TextFieldValue,
    startDateMillis: Long,
    endDateMillis: Long,
    modifier: Modifier = Modifier,
    onNameChange: (TextFieldValue) -> Unit,
    onPlanDateRangeChanged: (start: Long, end: Long) -> Unit,
) {
    var showDateRangePicker by remember { mutableStateOf(false) }

    val dateRangePickerState = rememberDateRangePickerState(
        initialSelectedStartDateMillis = startDateMillis,
        initialSelectedEndDateMillis = endDateMillis,
    )

    Column(modifier = modifier) {
        OutlinedTextField(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 56.dp),
            value = name,
            onValueChange = onNameChange,
            singleLine = true,
            shape = MaterialTheme.shapes.medium,
            textStyle = MaterialTheme.typography.titleMedium,
            label = {
                Text(
                    text = stringResource(Res.string.planning_page_trip_name_label),
                )
            },
            trailingIcon = {
                Icon(
                    painter = painterResource(Res.drawable.edit),
                    contentDescription = stringResource(Res.string.planning_trip_cd_edit_name),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            },
            // The field reads as a plain surface row: the container carries the affordance, not an
            // underline.
//            colors = TextFieldDefaults.colors(
//                focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
//                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
//                focusedIndicatorColor = Color.Transparent,
//                unfocusedIndicatorColor = Color.Transparent,
//                disabledIndicatorColor = Color.Transparent,
//            ),
        )

        Spacer(Modifier.height(16.dp))

        TravelDateRangePicker(
            showDateRangePicker = showDateRangePicker,
            dateRangePickerState = dateRangePickerState,
            onShowDateRangePicker = {
                showDateRangePicker = it
            },
            onPlanDateRangeChanged = onPlanDateRangeChanged,
        )
    }
}

//region Previews
@OptIn(ExperimentalTime::class)
@Preview(showSystemUi = true)
@Composable
private fun PlanOverviewHeaderPreview() = KTravelTheme {
    val loremIpsum = LoremIpsum(10).values.first()

    var pickedDate by remember {
        val time = Clock.System.now().toEpochMilliseconds()
        mutableStateOf(time to time + 1.days.toLong(DurationUnit.MILLISECONDS))
    }

    PlanOverviewHeader(
        name = TextFieldValue(loremIpsum),
        startDateMillis = pickedDate.first,
        endDateMillis = pickedDate.second,
        modifier = Modifier.fillMaxWidth(),
        onNameChange = {},
        onPlanDateRangeChanged = { start, end ->
        },
    )
}
//endregion Previews
