package com.takaotech.ktravel.ui.travels.list

import com.takaotech.ktravel.core.toLocalDate
import com.takaotech.ktravel.presentation.travels.TravelSummaryUiState
import kotlinx.collections.immutable.persistentListOf
import kotlin.time.Clock

internal val previewTravelList = persistentListOf(
    TravelSummaryUiState(
        id = "1",
        name = "Viaggio a Tokyo",
        periodStart = Clock.System.now().toLocalDate(),
        periodEnd = Clock.System.now().toLocalDate(),
    ),
    TravelSummaryUiState(
        id = "2",
        name = "Weekend a Roma",
        periodStart = Clock.System.now().toLocalDate(),
        periodEnd = Clock.System.now().toLocalDate(),
    ),
)
