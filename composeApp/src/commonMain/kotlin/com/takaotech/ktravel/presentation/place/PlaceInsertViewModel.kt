package com.takaotech.ktravel.presentation.place

import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.takaotech.ktravel.domain.repository.TravelPlanRepository
import com.takaotech.ktravel.presentation.planner.Place
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import org.koin.android.annotation.KoinViewModel

@KoinViewModel
class PlaceInsertViewModel(
    private val repository: TravelPlanRepository
) : ViewModel() {
    private val latRegex = Regex("^(\\+|-)?(?:90(?:(?:\\.0{1,6})?)|(?:[0-9]|[1-8][0-9])(?:(?:\\.[0-9]{1,6})?))$")
    private val lngRegex =
        Regex("^(\\+|-)?(?:180(?:(?:\\.0{1,6})?)|(?:[0-9]|[1-9][0-9]|1[0-7][0-9])(?:(?:\\.[0-9]{1,6})?))\$")

    private val _uiState = MutableStateFlow(PlaceInsertUiState())
    val uiState: StateFlow<PlaceInsertUiState> = _uiState.asStateFlow()

    fun onInputModeChanged(mode: PlaceInputMode) {
        _uiState.update { it.copy(inputMode = mode) }
    }

    fun onPlaceNameChanged(name: TextFieldValue) {
        _uiState.update { it.copy(placeName = name) }
    }

    fun onPlaceLatChanged(lat: TextFieldValue) {
        _uiState.update { it.copy(placeLat = lat) }
    }

    fun onPlaceLngChanged(lng: TextFieldValue) {
        _uiState.update { it.copy(placeLng = lng) }
    }

    fun onSearchQueryChanged(query: TextFieldValue) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun onPlaceSelected(name: String, lat: Double, lng: Double) {
        _uiState.update {
            it.copy(
                placeName = TextFieldValue(name),
                placeLat = TextFieldValue(lat.toString()),
                placeLng = TextFieldValue(lng.toString())
            )
        }
    }

    fun searchPlaces(query: String) {
        // TODO: Implementare la ricerca effettiva tramite servizio di geocoding
        // Questo metodo verrà chiamato quando l'utente cerca un luogo
        // I risultati dovranno essere esposti tramite un nuovo campo nello stato UI
    }

    fun onTimeSelected(hour: Int, minute: Int) {
        _uiState.update { it.copy(selectedTime = LocalTime(hour, minute)) }
    }

    fun onDateSelected(date: LocalDate?) {
        _uiState.update { it.copy(selectedDate = date) }
    }

    fun savePlace() {
        viewModelScope.launch {
            // TODO Validate Inputs
            //  name cannot be empty
            //  lat lng if selected can not be empty
            //  validate lat lng

            val currentState = _uiState.value
            val place = Place(
                name = currentState.placeName.text,
                lat = currentState.placeLat.text.toDoubleOrNull() ?: 0.0,
                lng = currentState.placeLng.text.toDoubleOrNull() ?: 0.0,
            )
            repository.savePlace(place)
        }
    }
}
