package com.takaotech.ktravel.presentation.intro

import androidx.compose.runtime.Stable
import com.takaotech.ktravel.core.ui.KFieldState

@Stable
data class TravelCreationUiState(
    val travelName: KFieldState = KFieldState(),
    val startDateMillis: Long? = null,
    val endDateMillis: Long? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val createdTravelId: String? = null
)
