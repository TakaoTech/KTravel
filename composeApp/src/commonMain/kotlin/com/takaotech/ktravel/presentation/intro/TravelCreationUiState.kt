package com.takaotech.ktravel.presentation.intro

import androidx.compose.runtime.Stable
import com.takaotech.ktravel.core.ui.KFieldState
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.todayIn
import kotlin.time.Clock

@Stable
data class TravelCreationUiState(
    val travelName: KFieldState = KFieldState(),
    val startDateMillis: Long = Clock.System.todayIn(TimeZone.currentSystemDefault())
        .atStartOfDayIn(TimeZone.UTC).toEpochMilliseconds(),
    val endDateMillis: Long = Clock.System.todayIn(TimeZone.currentSystemDefault())
        .atStartOfDayIn(TimeZone.UTC).toEpochMilliseconds(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val createdTravelId: String? = null
)
