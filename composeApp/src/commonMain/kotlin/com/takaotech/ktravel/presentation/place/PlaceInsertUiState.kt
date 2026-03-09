package com.takaotech.ktravel.presentation.place

import androidx.compose.runtime.Stable
import com.takaotech.ktravel.core.ui.KFieldState
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime

enum class PlaceInputMode { LAT_LNG, SEARCH }

@Stable
data class PlaceInsertUiState(
    val inputMode: PlaceInputMode = PlaceInputMode.LAT_LNG,
    val placeName: KFieldState = KFieldState(),
    val placeLat: KFieldState = KFieldState(),
    val placeLng: KFieldState = KFieldState(),
    val searchQuery: KFieldState = KFieldState(),
    val selectedDate: LocalDate? = null,
    val selectedTime: LocalTime = LocalTime(0, 0),
    val isBulk: Boolean = false,
)
