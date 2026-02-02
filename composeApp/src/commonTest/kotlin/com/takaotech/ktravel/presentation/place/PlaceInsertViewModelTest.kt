package com.takaotech.ktravel.presentation.place

import androidx.compose.ui.text.input.TextFieldValue
import com.takaotech.ktravel.core.FieldValidationState
import com.takaotech.ktravel.domain.repository.TravelPlanRepository
import com.takaotech.ktravel.presentation.planner.Place
import com.takaotech.ktravel.presentation.planner.PlanningUiState
import com.takaotech.ktravel.presentation.planner.TravelDay
import io.kotest.assertions.nondeterministic.eventually
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import ktravel.composeapp.generated.resources.*
import kotlin.time.Duration.Companion.seconds

class PlaceInsertViewModelTest : BehaviorSpec({

    given("a PlaceInsertViewModel with initial state") {
        val fakeRepository = FakeTravelPlanRepository()
        val viewModel = PlaceInsertViewModel(fakeRepository)

        then("should have default input mode as LAT_LNG") {
            viewModel.uiState.value.inputMode shouldBe PlaceInputMode.LAT_LNG
        }

        then("should have empty place name") {
            viewModel.uiState.value.placeName.value.text shouldBe ""
        }

        then("should have empty place lat") {
            viewModel.uiState.value.placeLat.value.text shouldBe ""
        }

        then("should have empty place lng") {
            viewModel.uiState.value.placeLng.value.text shouldBe ""
        }

        then("should have empty search query") {
            viewModel.uiState.value.searchQuery.value.text shouldBe ""
        }

        then("should have null selected date") {
            viewModel.uiState.value.selectedDate shouldBe null
        }

        then("should have default selected time as 00:00") {
            viewModel.uiState.value.selectedTime shouldBe LocalTime(0, 0)
        }
    }

    given("a PlaceInsertViewModel") {
        `when`("onInputModeChanged is called with SEARCH mode") {
            val fakeRepository = FakeTravelPlanRepository()
            val viewModel = PlaceInsertViewModel(fakeRepository)

            viewModel.onInputModeChanged(PlaceInputMode.SEARCH)

            then("should update input mode to SEARCH") {
                viewModel.uiState.value.inputMode shouldBe PlaceInputMode.SEARCH
            }
        }

        `when`("onInputModeChanged is called with LAT_LNG mode") {
            val fakeRepository = FakeTravelPlanRepository()
            val viewModel = PlaceInsertViewModel(fakeRepository)
            viewModel.onInputModeChanged(PlaceInputMode.SEARCH)

            viewModel.onInputModeChanged(PlaceInputMode.LAT_LNG)

            then("should update input mode to LAT_LNG") {
                viewModel.uiState.value.inputMode shouldBe PlaceInputMode.LAT_LNG
            }
        }
    }

    given("a PlaceInsertViewModel for place name changes") {
        `when`("onPlaceNameChanged is called with a name") {
            val fakeRepository = FakeTravelPlanRepository()
            val viewModel = PlaceInsertViewModel(fakeRepository)
            val newName = TextFieldValue("Colosseo")

            viewModel.onPlaceNameChanged(newName)

            then("should update place name") {
                viewModel.uiState.value.placeName.value shouldBe newName
            }
        }
    }

    given("a PlaceInsertViewModel for latitude validation") {
        `when`("onPlaceLatChanged is called with valid latitude 45.0") {
            val fakeRepository = FakeTravelPlanRepository()
            val viewModel = PlaceInsertViewModel(fakeRepository)
            val validLat = TextFieldValue("45.0")

            viewModel.onPlaceLatChanged(validLat)

            then("should update place lat") {
                viewModel.uiState.value.placeLat.value shouldBe validLat
            }
        }

        `when`("onPlaceLatChanged is called with valid latitude 90") {
            val fakeRepository = FakeTravelPlanRepository()
            val viewModel = PlaceInsertViewModel(fakeRepository)
            val validLat = TextFieldValue("90")

            viewModel.onPlaceLatChanged(validLat)

            then("should update place lat") {
                viewModel.uiState.value.placeLat.value shouldBe validLat
            }
        }

        `when`("onPlaceLatChanged is called with valid latitude -90") {
            val fakeRepository = FakeTravelPlanRepository()
            val viewModel = PlaceInsertViewModel(fakeRepository)
            val validLat = TextFieldValue("-90")

            viewModel.onPlaceLatChanged(validLat)

            then("should update place lat") {
                viewModel.uiState.value.placeLat.value shouldBe validLat
            }
        }

        `when`("onPlaceLatChanged is called with valid latitude 0") {
            val fakeRepository = FakeTravelPlanRepository()
            val viewModel = PlaceInsertViewModel(fakeRepository)
            val validLat = TextFieldValue("0")

            viewModel.onPlaceLatChanged(validLat)

            then("should update place lat") {
                viewModel.uiState.value.placeLat.value shouldBe validLat
            }
        }

        `when`("onPlaceLatChanged is called with valid latitude with decimals") {
            val fakeRepository = FakeTravelPlanRepository()
            val viewModel = PlaceInsertViewModel(fakeRepository)
            val validLat = TextFieldValue("41.890251")

            viewModel.onPlaceLatChanged(validLat)

            then("should update place lat") {
                viewModel.uiState.value.placeLat.value shouldBe validLat
            }
        }
    }

    given("a PlaceInsertViewModel for longitude validation") {
        `when`("onPlaceLngChanged is called with valid longitude 45.0") {
            val fakeRepository = FakeTravelPlanRepository()
            val viewModel = PlaceInsertViewModel(fakeRepository)
            val validLng = TextFieldValue("45.0")

            viewModel.onPlaceLngChanged(validLng)

            then("should update place lng") {
                viewModel.uiState.value.placeLng.value shouldBe validLng
            }
        }

        `when`("onPlaceLngChanged is called with valid longitude -180") {
            val fakeRepository = FakeTravelPlanRepository()
            val viewModel = PlaceInsertViewModel(fakeRepository)
            val validLng = TextFieldValue("-180")

            viewModel.onPlaceLngChanged(validLng)

            then("should update place lng") {
                viewModel.uiState.value.placeLng.value shouldBe validLng
            }
        }

        `when`("onPlaceLngChanged is called with valid longitude 0") {
            val fakeRepository = FakeTravelPlanRepository()
            val viewModel = PlaceInsertViewModel(fakeRepository)
            val validLng = TextFieldValue("0")

            viewModel.onPlaceLngChanged(validLng)

            then("should update place lng") {
                viewModel.uiState.value.placeLng.value shouldBe validLng
            }
        }

        `when`("onPlaceLngChanged is called with valid longitude with decimals") {
            val fakeRepository = FakeTravelPlanRepository()
            val viewModel = PlaceInsertViewModel(fakeRepository)
            val validLng = TextFieldValue("12.492373")

            viewModel.onPlaceLngChanged(validLng)

            then("should update place lng") {
                viewModel.uiState.value.placeLng.value shouldBe validLng
            }
        }
    }

    given("a PlaceInsertViewModel for search query changes") {
        `when`("onSearchQueryChanged is called with a query") {
            val fakeRepository = FakeTravelPlanRepository()
            val viewModel = PlaceInsertViewModel(fakeRepository)
            val query = TextFieldValue("Roma")

            viewModel.onSearchQueryChanged(query)

            then("should update search query") {
                viewModel.uiState.value.searchQuery.value shouldBe query
            }
        }
    }

    given("a PlaceInsertViewModel for time selection") {
        `when`("onTimeSelected is called with hour and minute") {
            val fakeRepository = FakeTravelPlanRepository()
            val viewModel = PlaceInsertViewModel(fakeRepository)

            viewModel.onTimeSelected(14, 30)

            then("should update selected time") {
                viewModel.uiState.value.selectedTime shouldBe LocalTime(14, 30)
            }
        }
    }

    given("a PlaceInsertViewModel for date selection") {
        `when`("onDateSelected is called with a date") {
            val fakeRepository = FakeTravelPlanRepository()
            val viewModel = PlaceInsertViewModel(fakeRepository)
            val date = LocalDate(2024, 6, 15)

            viewModel.onDateSelected(date)

            then("should update selected date") {
                viewModel.uiState.value.selectedDate shouldBe date
            }
        }

        `when`("onDateSelected is called with null") {
            val fakeRepository = FakeTravelPlanRepository()
            val viewModel = PlaceInsertViewModel(fakeRepository)
            viewModel.onDateSelected(LocalDate(2024, 6, 15))

            viewModel.onDateSelected(null)

            then("should set selected date to null") {
                viewModel.uiState.value.selectedDate shouldBe null
            }
        }
    }

    given("a PlaceInsertViewModel for saving a place") {
        `when`("savePlace is called with valid data") {
            val fakeRepository = FakeTravelPlanRepository()
            val viewModel = PlaceInsertViewModel(fakeRepository)

            viewModel.onPlaceNameChanged(TextFieldValue("Colosseo"))
            viewModel.onPlaceLatChanged(TextFieldValue("41.890251"))
            viewModel.onPlaceLngChanged(TextFieldValue("12.492373"))

            viewModel.savePlace()

            then("should call repository savePlace with correct data") {
                eventually(duration = 1.seconds) {
                    fakeRepository.savedPlace?.name shouldBe "Colosseo"
                    fakeRepository.savedPlace?.lat shouldBe 41.890251
                    fakeRepository.savedPlace?.lng shouldBe 12.492373
                }
            }

            then("should not have any errors") {
                eventually(duration = 1.seconds) {
                    viewModel.uiState.value.placeName.validationState shouldBe FieldValidationState.Valid
                    viewModel.uiState.value.placeLat.validationState shouldBe FieldValidationState.Valid
                    viewModel.uiState.value.placeLng.validationState shouldBe FieldValidationState.Valid
                }
            }
        }

        `when`("savePlace is called with empty name") {
            val fakeRepository = FakeTravelPlanRepository()
            val viewModel = PlaceInsertViewModel(fakeRepository)

            viewModel.onPlaceLatChanged(TextFieldValue("41.890251"))
            viewModel.onPlaceLngChanged(TextFieldValue("12.492373"))

            viewModel.savePlace()

            then("should set name error") {
                eventually(duration = 1.seconds) {
                    viewModel.uiState.value.placeName.validationState.shouldBeInstanceOf<FieldValidationState.BaseNotValid>()
                    viewModel.uiState.value.placeName.validationState.errorText?.text shouldBe Res.string.place_insert_error_name_empty
                }
            }

            then("should not save place") {
                eventually(duration = 1.seconds) {
                    fakeRepository.savedPlace shouldBe null
                }
            }
        }

        `when`("savePlace is called with empty latitude in LAT_LNG mode") {
            val fakeRepository = FakeTravelPlanRepository()
            val viewModel = PlaceInsertViewModel(fakeRepository)

            viewModel.onPlaceNameChanged(TextFieldValue("Test Place"))
            viewModel.onPlaceLngChanged(TextFieldValue("12.492373"))

            viewModel.savePlace()

            then("should set lat error") {
                eventually(duration = 1.seconds) {
                    viewModel.uiState.value.placeLat.validationState.shouldBeInstanceOf<FieldValidationState.BaseNotValid>()
                    viewModel.uiState.value.placeLat.validationState.errorText?.text shouldBe Res.string.place_insert_error_lat_empty
                }
            }

            then("should not save place") {
                eventually(duration = 1.seconds) {
                    fakeRepository.savedPlace shouldBe null
                }
            }
        }

        `when`("savePlace is called with empty longitude in LAT_LNG mode") {
            val fakeRepository = FakeTravelPlanRepository()
            val viewModel = PlaceInsertViewModel(fakeRepository)

            viewModel.onPlaceNameChanged(TextFieldValue("Test Place"))
            viewModel.onPlaceLatChanged(TextFieldValue("41.890251"))

            viewModel.savePlace()

            then("should set lng error") {
                eventually(duration = 1.seconds) {
                    viewModel.uiState.value.placeLng.validationState.shouldBeInstanceOf<FieldValidationState.BaseNotValid>()
                    viewModel.uiState.value.placeLng.validationState.errorText?.text shouldBe Res.string.place_insert_error_lng_empty
                }
            }

            then("should not save place") {
                eventually(duration = 1.seconds) {
                    fakeRepository.savedPlace shouldBe null
                }
            }
        }

        `when`("savePlace is called with invalid latitude format") {
            val fakeRepository = FakeTravelPlanRepository()
            val viewModel = PlaceInsertViewModel(fakeRepository)

            viewModel.onPlaceNameChanged(TextFieldValue("Test Place"))
            viewModel.onPlaceLatChanged(TextFieldValue("invalid"))
            viewModel.onPlaceLngChanged(TextFieldValue("12.492373"))

            viewModel.savePlace()

            then("should set lat error") {
                eventually(duration = 1.seconds) {
                    viewModel.uiState.value.placeLat.validationState.shouldBeInstanceOf<FieldValidationState.BaseNotValid>()
                    viewModel.uiState.value.placeLat.validationState.errorText?.text shouldBe Res.string.place_insert_error_lat_invalid_format
                }
            }

            then("should not save place") {
                eventually(duration = 1.seconds) {
                    fakeRepository.savedPlace shouldBe null
                }
            }
        }

        `when`("savePlace is called with invalid longitude format") {
            val fakeRepository = FakeTravelPlanRepository()
            val viewModel = PlaceInsertViewModel(fakeRepository)

            viewModel.onPlaceNameChanged(TextFieldValue("Test Place"))
            viewModel.onPlaceLatChanged(TextFieldValue("41.890251"))
            viewModel.onPlaceLngChanged(TextFieldValue("invalid"))

            viewModel.savePlace()

            then("should set lng error") {
                eventually(duration = 1.seconds) {
                    viewModel.uiState.value.placeLng.validationState.shouldBeInstanceOf<FieldValidationState.BaseNotValid>()
                    viewModel.uiState.value.placeLng.validationState.errorText?.text shouldBe Res.string.place_insert_error_lng_invalid_format
                }
            }

            then("should not save place") {
                eventually(duration = 1.seconds) {
                    fakeRepository.savedPlace shouldBe null
                }
            }
        }

        `when`("savePlace is called with latitude out of range") {
            val fakeRepository = FakeTravelPlanRepository()
            val viewModel = PlaceInsertViewModel(fakeRepository)

            viewModel.onPlaceNameChanged(TextFieldValue("Test Place"))
            viewModel.onPlaceLatChanged(TextFieldValue("91"))
            viewModel.onPlaceLngChanged(TextFieldValue("12.492373"))

            viewModel.savePlace()

            then("should set lat error") {
                eventually(duration = 1.seconds) {
                    viewModel.uiState.value.placeLat.validationState.shouldBeInstanceOf<FieldValidationState.BaseNotValid>()
                    viewModel.uiState.value.placeLat.validationState.errorText?.text shouldBe Res.string.place_insert_error_lat_invalid_format
                }
            }

            then("should not save place") {
                eventually(duration = 1.seconds) {
                    fakeRepository.savedPlace shouldBe null
                }
            }
        }

        `when`("savePlace is called with longitude out of range") {
            val fakeRepository = FakeTravelPlanRepository()
            val viewModel = PlaceInsertViewModel(fakeRepository)

            viewModel.onPlaceNameChanged(TextFieldValue("Test Place"))
            viewModel.onPlaceLatChanged(TextFieldValue("41.890251"))
            viewModel.onPlaceLngChanged(TextFieldValue("181"))

            viewModel.savePlace()

            then("should set lng error") {
                eventually(duration = 1.seconds) {
                    viewModel.uiState.value.placeLng.validationState.shouldBeInstanceOf<FieldValidationState.BaseNotValid>()
                    viewModel.uiState.value.placeLng.validationState.errorText?.text shouldBe Res.string.place_insert_error_lng_invalid_format
                }
            }

            then("should not save place") {
                eventually(duration = 1.seconds) {
                    fakeRepository.savedPlace shouldBe null
                }
            }
        }

    }

    given("a PlaceInsertViewModel for error clearing") {
        `when`("onPlaceNameChanged is called after name error") {
            val fakeRepository = FakeTravelPlanRepository()
            val viewModel = PlaceInsertViewModel(fakeRepository)

            viewModel.savePlace()
            eventually(duration = 1.seconds) {
                viewModel.uiState.value.placeName.validationState.shouldBeInstanceOf<FieldValidationState.BaseNotValid>()
            }

            viewModel.onPlaceNameChanged(TextFieldValue("New Name"))

            then("should clear name error") {
                viewModel.uiState.value.placeName.validationState shouldBe FieldValidationState.None
            }
        }

        `when`("onPlaceLatChanged is called after lat error") {
            val fakeRepository = FakeTravelPlanRepository()
            val viewModel = PlaceInsertViewModel(fakeRepository)

            viewModel.onPlaceNameChanged(TextFieldValue("Test"))
            viewModel.onPlaceLngChanged(TextFieldValue("12.0"))
            viewModel.savePlace()
            eventually(duration = 1.seconds) {
                viewModel.uiState.value.placeLat.validationState.shouldBeInstanceOf<FieldValidationState.BaseNotValid>()
            }

            viewModel.onPlaceLatChanged(TextFieldValue("41.0"))

            then("should clear lat error") {
                viewModel.uiState.value.placeLat.validationState shouldBe FieldValidationState.None
            }
        }

        `when`("onPlaceLngChanged is called after lng error") {
            val fakeRepository = FakeTravelPlanRepository()
            val viewModel = PlaceInsertViewModel(fakeRepository)

            viewModel.onPlaceNameChanged(TextFieldValue("Test"))
            viewModel.onPlaceLatChanged(TextFieldValue("41.0"))
            viewModel.savePlace()
            eventually(duration = 1.seconds) {
                viewModel.uiState.value.placeLng.validationState.shouldBeInstanceOf<FieldValidationState.BaseNotValid>()
            }

            viewModel.onPlaceLngChanged(TextFieldValue("12.0"))

            then("should clear lng error") {
                viewModel.uiState.value.placeLng.validationState shouldBe FieldValidationState.None
            }
        }
    }
})

private class FakeTravelPlanRepository : TravelPlanRepository {
    var savedPlace: Place? = null

    private val _planningState = MutableStateFlow(PlanningUiState())
    override val planningState: StateFlow<PlanningUiState> = _planningState

    override fun getTravelDayFlow(dayId: String): Flow<TravelDay?> = flowOf(null)

    override suspend fun updatePeriod(startMillis: Long, endMillis: Long) = Unit

    override suspend fun removeStepFromDay(dayId: String, stepId: String) = Unit

    override suspend fun updateStep(dayId: String, stepId: String, updatedStep: TravelDay.Step) = Unit

    override fun updatePlanName(name: TextFieldValue) = Unit

    override suspend fun savePlace(place: Place) {
        savedPlace = place
    }

    override suspend fun movePlaceToDay(placeId: String, dayId: String) = Unit
}
