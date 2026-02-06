package com.takaotech.ktravel.presentation.place

import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.takaotech.ktravel.core.FieldValidationState
import com.takaotech.ktravel.core.toTextPayload
import com.takaotech.ktravel.domain.repository.TravelPlanRepository
import com.takaotech.ktravel.presentation.planner.Place
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import ktravel.composeapp.generated.resources.*
import org.koin.android.annotation.KoinViewModel
import org.koin.core.annotation.InjectedParam

@KoinViewModel
class PlaceInsertViewModel(
    @InjectedParam private val dayId: String?,
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
        _uiState.update {
            it.copy(
                placeName = it.placeName.copy(
                    value = name,
                    validationState = FieldValidationState.None
                )
            )
        }
    }

    fun onPlaceLatChanged(lat: TextFieldValue) {
        _uiState.update {
            it.copy(
                placeLat = it.placeLat.copy(
                    value = lat,
                    validationState = FieldValidationState.None
                )
            )
        }
    }

    fun onPlaceLngChanged(lng: TextFieldValue) {
        _uiState.update {
            it.copy(
                placeLng = it.placeLng.copy(
                    value = lng,
                    validationState = FieldValidationState.None
                )
            )
        }
    }

    fun onSearchQueryChanged(query: TextFieldValue) {
        _uiState.update {
            it.copy(
                searchQuery = it.searchQuery.copy(
                    value = query,
                    validationState = FieldValidationState.None
                )
            )
        }
    }

    fun onPlaceSelected(name: String, lat: Double, lng: Double) {
//        _uiState.update {
//            it.copy(
//                placeName = TextFieldValue(name),
//                placeLat = TextFieldValue(lat.toString()),
//                placeLng = TextFieldValue(lng.toString())
//            )
//        }
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
            // Update state with errors
            val newUiState = _uiState.updateAndGet {
                it.copy(
                    placeName = with(it.placeName) {
                        copy(
                            validationState = if (value.text.isBlank()) {
                                FieldValidationState.BaseNotValid(
                                    Res.string.place_insert_error_name_empty.toTextPayload()
                                )
                            } else {
                                FieldValidationState.Valid
                            }
                        )
                    },
                    placeLat = with(it.placeLat) {
                        copy(
                            validationState = if (value.text.isBlank()) {
                                FieldValidationState.BaseNotValid(
                                    Res.string.place_insert_error_lat_empty.toTextPayload()
                                )
                            } else if (!latRegex.matches(value.text)) {
                                FieldValidationState.BaseNotValid(
                                    Res.string.place_insert_error_lat_invalid_format.toTextPayload()
                                )
                            } else {
                                FieldValidationState.Valid
                            }
                        )
                    },
                    placeLng = with(it.placeLng) {
                        copy(
                            validationState = if (value.text.isBlank()) {
                                FieldValidationState.BaseNotValid(
                                    Res.string.place_insert_error_lng_empty.toTextPayload()
                                )
                            } else if (!lngRegex.matches(value.text)) {
                                FieldValidationState.BaseNotValid(
                                    Res.string.place_insert_error_lng_invalid_format.toTextPayload()
                                )
                            } else {
                                FieldValidationState.Valid
                            }
                        )
                    },
                )
            }

            val name = newUiState.placeName.takeIf { it.validationState is FieldValidationState.Valid }
            val lat = newUiState.placeLat.takeIf { it.validationState is FieldValidationState.Valid }
            val lng = newUiState.placeLng.takeIf { it.validationState is FieldValidationState.Valid }

            // Only save if there are no errors
            if (name != null && lat != null && lng != null) {
                val place = Place(
                    name = name.value.text,
                    lat = lat.value.text.toDoubleOrNull() ?: 0.0,
                    lng = lng.value.text.toDoubleOrNull() ?: 0.0,
                )
                repository.savePlace(place, dayId)
            }
        }
    }
}
