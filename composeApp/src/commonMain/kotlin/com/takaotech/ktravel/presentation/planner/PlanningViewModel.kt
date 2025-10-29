package com.takaotech.ktravel.presentation.planner

import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@OptIn(ExperimentalTime::class)
class PlanningViewModel : ViewModel() {

    private val mUiState = MutableStateFlow(PlanningUiState())
    val uiState: StateFlow<PlanningUiState> = mUiState.asStateFlow()

    init {
        with(mUiState.value) {
            onPlanDateChanged(planHeader.period.start.toEpochMilliseconds(), planHeader.period.end.toEpochMilliseconds())
        }
    }


    fun onPlanNameChanged(name: TextFieldValue) {
        mUiState.value = with(uiState.value) {
            copy(
                planHeader = planHeader.copy(
                    name = name,
                )
            )
        }
    }

    @OptIn(ExperimentalTime::class)
    fun onPlanDateChanged(start: Long, end: Long) {
        mUiState.update {
            it.setPeriod(start = Instant.fromEpochMilliseconds(start), end = Instant.fromEpochMilliseconds(end))
        }
    }


}