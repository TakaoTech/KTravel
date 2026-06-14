package com.takaotech.ktravel.presentation.place

import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.takaotech.ktravel.core.ui.FieldValidationState
import com.takaotech.ktravel.core.ui.KFieldState
import com.takaotech.ktravel.core.ui.toTextPayload
import com.takaotech.ktravel.di.PlanningGraphStore
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.flow.updateAndGet
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import ktravel.composeapp.generated.resources.Res
import ktravel.composeapp.generated.resources.place_insert_error_lat_empty
import ktravel.composeapp.generated.resources.place_insert_error_lat_invalid_format
import ktravel.composeapp.generated.resources.place_insert_error_lng_empty
import ktravel.composeapp.generated.resources.place_insert_error_lng_invalid_format
import ktravel.composeapp.generated.resources.place_insert_error_name_empty

class PlaceInsertViewModel @AssistedInject constructor(
    @Assisted private val travelId: String,
    @Assisted private val dayId: String?,
    private val planningGraphStore: PlanningGraphStore
) : ViewModel() {

    @AssistedFactory
    fun interface Factory {
        fun create(travelId: String, dayId: String?): PlaceInsertViewModel
    }

    private val latRegex =
        Regex("^(\\+|-)?(?:90(?:(?:\\.0{1,6})?)|(?:[0-9]|[1-8][0-9])(?:(?:\\.[0-9]{1,6})?))$")
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
                    value = name, validationState = FieldValidationState.None
                )
            )
        }
    }

    fun onPlaceLatChanged(lat: TextFieldValue) {
        if (tryParseAndSetLatLng(lat.text)) return
        _uiState.update {
            it.copy(
                placeLat = it.placeLat.copy(
                    value = lat, validationState = FieldValidationState.None
                )
            )
        }
    }

    fun onPlaceLngChanged(lng: TextFieldValue) {
        if (tryParseAndSetLatLng(lng.text)) return
        _uiState.update {
            it.copy(
                placeLng = it.placeLng.copy(
                    value = lng, validationState = FieldValidationState.None
                )
            )
        }
    }

    private fun tryParseAndSetLatLng(text: String): Boolean {
        val parsed = parseLatLng(text) ?: return false
        val (lat, lng) = parsed
        _uiState.update {
            it.copy(
                placeLat = it.placeLat.copy(
                    value = TextFieldValue(lat), validationState = FieldValidationState.None
                ), placeLng = it.placeLng.copy(
                    value = TextFieldValue(lng), validationState = FieldValidationState.None
                )
            )
        }
        return true
    }

    private fun parseLatLng(text: String): Pair<String, String>? {
        val coordinatePattern = Regex("""[+-]?[0-9]+(?:\.[0-9]+)?""")
        val parts = coordinatePattern.findAll(text.trim()).map { it.value }.toList()
        if (parts.size < 2) return null

        val separatorPattern = Regex("""^[+-]?[0-9]+(?:\.[0-9]+)?[\s,]+[+-]?[0-9]+(?:\.[0-9]+)?$""")
        if (!separatorPattern.matches(text.trim())) return null

        return Pair(parts[0], parts[1])
    }

    fun onSearchQueryChanged(query: TextFieldValue) {
        _uiState.update {
            it.copy(
                searchQuery = it.searchQuery.copy(
                    value = query, validationState = FieldValidationState.None
                )
            )
        }
    }

    fun onPlaceSelected(name: String, lat: Double, lng: Double) {}

    fun searchPlaces(query: String) {}

    fun onTimeSelected(hour: Int, minute: Int) {
        _uiState.update { it.copy(selectedTime = LocalTime(hour, minute)) }
    }

    fun onDateSelected(date: LocalDate?) {
        _uiState.update { it.copy(selectedDate = date) }
    }

    fun onBulkChanged(isBulk: Boolean) {
        _uiState.update { it.copy(isBulk = isBulk) }
    }

    fun savePlace() {
        viewModelScope.launch {
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

            val name =
                newUiState.placeName.takeIf { it.validationState is FieldValidationState.Valid }
            val lat =
                newUiState.placeLat.takeIf { it.validationState is FieldValidationState.Valid }
            val lng =
                newUiState.placeLng.takeIf { it.validationState is FieldValidationState.Valid }

            if (name != null && lat != null && lng != null) {
                planningGraphStore.getOrCreate(travelId)
                    .savePlaceUseCase
                    .invoke(
                        name = name.value.text,
                        lat = lat.value.text.toDoubleOrNull() ?: 0.0,
                        lng = lng.value.text.toDoubleOrNull() ?: 0.0,
                        dayId = dayId
                    )

                if (newUiState.isBulk) {
                    _uiState.update {
                        it.copy(
                            placeName = KFieldState(),
                            placeLat = KFieldState(),
                            placeLng = KFieldState(),
                            searchQuery = KFieldState()
                        )
                    }
                }
            }
        }
    }
}
