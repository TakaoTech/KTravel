package com.takaotech.ktravel.ui.plan.day.placestep

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.slack.circuit.codegen.annotations.CircuitInject
import com.takaotech.ktravel.di.AppScope
import com.takaotech.ktravel.presentation.plan.day.PlaceStepEvent
import com.takaotech.ktravel.presentation.plan.day.PlaceStepScreen
import com.takaotech.ktravel.presentation.plan.day.PlaceStepUiState
import com.takaotech.ktravel.ui.shared.component.BackButton

/**
 * Draws one place of an itinerary: its name, a map with the marker on it, and its notes.
 *
 * The notes are Markdown, and the files attached to the place can be referenced from inside them,
 * which is why the two are shown together rather than on separate screens.
 */
@CircuitInject(PlaceStepScreen::class, AppScope::class)
@Composable
fun PlaceStepUi(state: PlaceStepUiState, modifier: Modifier = Modifier) {
    val sink = state.eventSink
    val place = state.place
    if (place == null) {
        PlaceStepLoading(modifier = modifier, onBack = { sink(PlaceStepEvent.NavigateBack) })
    } else {
        PlaceStepContent(
            place = place,
            notes = state.notes,
            modifier = modifier,
            onBack = { sink(PlaceStepEvent.NavigateBack) },
            onNotesEvent = { sink(PlaceStepEvent.Notes(it)) },
            onSetStartTime = { sink(PlaceStepEvent.SetStartTime(it)) },
            onSetEndTime = { sink(PlaceStepEvent.SetEndTime(it)) },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun PlaceStepLoading(onBack: () -> Unit, modifier: Modifier = Modifier) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = {
                    BackButton(
                        onClick = onBack,
                        modifier = Modifier.testTag(PlaceStepTestTags.BACK_BUTTON),
                    )
                },
            )
        },
    ) { padding ->
        Box(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentAlignment = Alignment.Center,
        ) {
            CircularProgressIndicator()
        }
    }
}
