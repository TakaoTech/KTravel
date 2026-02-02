package com.takaotech.ktravel.presentation.place

import androidx.compose.runtime.Stable
import com.takaotech.ktravel.core.KFieldState
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import ktravel.composeapp.generated.resources.Res
import ktravel.composeapp.generated.resources.place_insert_latlng_label
import ktravel.composeapp.generated.resources.place_insert_search_label
import org.jetbrains.compose.resources.StringResource

enum class PlaceInputMode(val label: StringResource) {
    LAT_LNG(Res.string.place_insert_latlng_label),
    SEARCH(Res.string.place_insert_search_label)
}

@Stable
data class PlaceInsertUiState(
    val inputMode: PlaceInputMode = PlaceInputMode.LAT_LNG,
    val placeName: KFieldState = KFieldState(),
    val placeLat: KFieldState = KFieldState(),
    val placeLng: KFieldState = KFieldState(),
    val searchQuery: KFieldState = KFieldState(),
    val selectedDate: LocalDate? = null,
    val selectedTime: LocalTime = LocalTime(0, 0),
)
