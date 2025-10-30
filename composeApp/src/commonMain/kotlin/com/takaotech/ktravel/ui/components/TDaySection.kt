package com.takaotech.ktravel.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.takaotech.ktravel.presentation.planner.TravelDay
import kotlinx.collections.immutable.persistentListOf
import org.jetbrains.compose.resources.painterResource

@Composable
fun TDaySection(
    day: String,
    steps: List<TravelDay.Step>,
    modifier: Modifier = Modifier,
    isOpen: Boolean,
    onDayCollapseClicked: () -> Unit,
    onNewStepAddRequested: (String) -> Unit,
) {
    Column(
        modifier = modifier
    ) {
        TextButton(
            onClick = onDayCollapseClicked,
        ) {
            Text(text = day)
        }

        AnimatedVisibility(visible = isOpen) {
            Column(modifier = Modifier.padding(start = 32.dp)) {
                var locationField by remember { mutableStateOf(TextFieldValue("")) }

                TextField(
                    value = locationField,
                    onValueChange = { locationField = it },
                    keyboardOptions = KeyboardOptions(
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            onNewStepAddRequested(locationField.text)
                            locationField = TextFieldValue("")
                        }
                    ),
                    singleLine = true,
                )

                for ((index, step) in steps.withIndex()) {
                    when (step) {
                        is TravelDay.Step.Place -> {
                            Card(
                                modifier = Modifier.fillMaxWidth()
                                    .padding(
                                        vertical = 12.dp,
                                    ),
                            ) {
                                Text(
                                    modifier = Modifier.padding(16.dp),
                                    text = step.location
                                )
                            }

                            if ((index < steps.lastIndex) && steps.getOrNull(index + 1) !is TravelDay.Step.Transport) {
                                TextButton(
                                    onClick = {

                                    }
                                ) {
                                    Text("Add")
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
    }
}

@Preview(showBackground = true)
@Composable
private fun TDaySectionPreview() {
    var isOpen by remember { mutableStateOf(true) }

    TDaySection(
        day = "21 giugno 2025",
        isOpen = isOpen,
        onDayCollapseClicked = {
            isOpen = !isOpen
        },
        steps = persistentListOf(
            TravelDay.Step.Place(location = "Roma - Colosseo"),
            TravelDay.Step.Transport(type = TravelDay.Step.Transport.Type.BUS),
            TravelDay.Step.Place(location = "Fontana di Trevi"),
            TravelDay.Step.Transport(type = TravelDay.Step.Transport.Type.CAR),
            TravelDay.Step.Place(location = "Pantheon"),
            TravelDay.Step.Transport(type = TravelDay.Step.Transport.Type.TRAIN),
            TravelDay.Step.Place(location = "Piazza Navona")
        ),
        onNewStepAddRequested = {

        }
    )
}