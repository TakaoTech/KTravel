package com.takaotech.ktravel.presentation.place

import androidx.compose.ui.text.input.TextFieldValue
import com.takaotech.ktravel.domain.repository.TravelPlanRepository
import com.takaotech.ktravel.presentation.planner.Place
import com.takaotech.ktravel.presentation.planner.PlanningUiState
import com.takaotech.ktravel.presentation.planner.TravelDay
import io.kotest.assertions.nondeterministic.eventually
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlin.time.Duration.Companion.seconds

class PlaceInsertViewModelTest : BehaviorSpec({

    given("a PlaceInsertViewModel with initial state") {
        val fakeRepository = FakeTravelPlanRepository()
        val viewModel = PlaceInsertViewModel(fakeRepository)

        then("should have default input mode as LAT_LNG") {
            viewModel.uiState.value.inputMode shouldBe PlaceInputMode.LAT_LNG
        }

        then("should have empty place name") {
            viewModel.uiState.value.placeName.text shouldBe ""
        }

        then("should have empty place lat") {
            viewModel.uiState.value.placeLat.text shouldBe ""
        }

        then("should have empty place lng") {
            viewModel.uiState.value.placeLng.text shouldBe ""
        }

        then("should have empty search query") {
            viewModel.uiState.value.searchQuery.text shouldBe ""
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
                viewModel.uiState.value.placeName shouldBe newName
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
                viewModel.uiState.value.placeLat shouldBe validLat
            }
        }

        `when`("onPlaceLatChanged is called with valid latitude 90") {
            val fakeRepository = FakeTravelPlanRepository()
            val viewModel = PlaceInsertViewModel(fakeRepository)
            val validLat = TextFieldValue("90")

            viewModel.onPlaceLatChanged(validLat)

            then("should update place lat") {
                viewModel.uiState.value.placeLat shouldBe validLat
            }
        }

        `when`("onPlaceLatChanged is called with valid latitude -90") {
            val fakeRepository = FakeTravelPlanRepository()
            val viewModel = PlaceInsertViewModel(fakeRepository)
            val validLat = TextFieldValue("-90")

            viewModel.onPlaceLatChanged(validLat)

            then("should update place lat") {
                viewModel.uiState.value.placeLat shouldBe validLat
            }
        }

        `when`("onPlaceLatChanged is called with valid latitude 0") {
            val fakeRepository = FakeTravelPlanRepository()
            val viewModel = PlaceInsertViewModel(fakeRepository)
            val validLat = TextFieldValue("0")

            viewModel.onPlaceLatChanged(validLat)

            then("should update place lat") {
                viewModel.uiState.value.placeLat shouldBe validLat
            }
        }

        `when`("onPlaceLatChanged is called with valid latitude with decimals") {
            val fakeRepository = FakeTravelPlanRepository()
            val viewModel = PlaceInsertViewModel(fakeRepository)
            val validLat = TextFieldValue("41.890251")

            viewModel.onPlaceLatChanged(validLat)

            then("should update place lat") {
                viewModel.uiState.value.placeLat shouldBe validLat
            }
        }

        `when`("onPlaceLatChanged is called with invalid latitude 91") {
            val fakeRepository = FakeTravelPlanRepository()
            val viewModel = PlaceInsertViewModel(fakeRepository)
            val invalidLat = TextFieldValue("91")

            viewModel.onPlaceLatChanged(invalidLat)

            then("should not update place lat") {
                viewModel.uiState.value.placeLat.text shouldBe ""
            }
        }

        `when`("onPlaceLatChanged is called with invalid latitude -91") {
            val fakeRepository = FakeTravelPlanRepository()
            val viewModel = PlaceInsertViewModel(fakeRepository)
            val invalidLat = TextFieldValue("-91")

            viewModel.onPlaceLatChanged(invalidLat)

            then("should not update place lat") {
                viewModel.uiState.value.placeLat.text shouldBe ""
            }
        }

        `when`("onPlaceLatChanged is called with invalid text") {
            val fakeRepository = FakeTravelPlanRepository()
            val viewModel = PlaceInsertViewModel(fakeRepository)
            val invalidLat = TextFieldValue("abc")

            viewModel.onPlaceLatChanged(invalidLat)

            then("should not update place lat") {
                viewModel.uiState.value.placeLat.text shouldBe ""
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
                viewModel.uiState.value.placeLng shouldBe validLng
            }
        }

        `when`("onPlaceLngChanged is called with valid longitude 180") {
            val fakeRepository = FakeTravelPlanRepository()
            val viewModel = PlaceInsertViewModel(fakeRepository)
            val validLng = TextFieldValue("180")

            viewModel.onPlaceLngChanged(validLng)

            then("should update place lng") {
                viewModel.uiState.value.placeLng shouldBe validLng
            }
        }

        `when`("onPlaceLngChanged is called with valid longitude -180") {
            val fakeRepository = FakeTravelPlanRepository()
            val viewModel = PlaceInsertViewModel(fakeRepository)
            val validLng = TextFieldValue("-180")

            viewModel.onPlaceLngChanged(validLng)

            then("should update place lng") {
                viewModel.uiState.value.placeLng shouldBe validLng
            }
        }

        `when`("onPlaceLngChanged is called with valid longitude 0") {
            val fakeRepository = FakeTravelPlanRepository()
            val viewModel = PlaceInsertViewModel(fakeRepository)
            val validLng = TextFieldValue("0")

            viewModel.onPlaceLngChanged(validLng)

            then("should update place lng") {
                viewModel.uiState.value.placeLng shouldBe validLng
            }
        }

        `when`("onPlaceLngChanged is called with valid longitude with decimals") {
            val fakeRepository = FakeTravelPlanRepository()
            val viewModel = PlaceInsertViewModel(fakeRepository)
            val validLng = TextFieldValue("12.492373")

            viewModel.onPlaceLngChanged(validLng)

            then("should update place lng") {
                viewModel.uiState.value.placeLng shouldBe validLng
            }
        }

        `when`("onPlaceLngChanged is called with invalid longitude 181") {
            val fakeRepository = FakeTravelPlanRepository()
            val viewModel = PlaceInsertViewModel(fakeRepository)
            val invalidLng = TextFieldValue("181")

            viewModel.onPlaceLngChanged(invalidLng)

            then("should not update place lng") {
                viewModel.uiState.value.placeLng.text shouldBe ""
            }
        }

        `when`("onPlaceLngChanged is called with invalid longitude -181") {
            val fakeRepository = FakeTravelPlanRepository()
            val viewModel = PlaceInsertViewModel(fakeRepository)
            val invalidLng = TextFieldValue("-181")

            viewModel.onPlaceLngChanged(invalidLng)

            then("should not update place lng") {
                viewModel.uiState.value.placeLng.text shouldBe ""
            }
        }

        `when`("onPlaceLngChanged is called with invalid text") {
            val fakeRepository = FakeTravelPlanRepository()
            val viewModel = PlaceInsertViewModel(fakeRepository)
            val invalidLng = TextFieldValue("xyz")

            viewModel.onPlaceLngChanged(invalidLng)

            then("should not update place lng") {
                viewModel.uiState.value.placeLng.text shouldBe ""
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
                viewModel.uiState.value.searchQuery shouldBe query
            }
        }
    }

    given("a PlaceInsertViewModel for place selection") {
        `when`("onPlaceSelected is called with name, lat and lng") {
            val fakeRepository = FakeTravelPlanRepository()
            val viewModel = PlaceInsertViewModel(fakeRepository)

            viewModel.onPlaceSelected("Colosseo", 41.890251, 12.492373)

            then("should update place name") {
                viewModel.uiState.value.placeName.text shouldBe "Colosseo"
            }

            then("should update place lat") {
                viewModel.uiState.value.placeLat.text shouldBe "41.890251"
            }

            then("should update place lng") {
                viewModel.uiState.value.placeLng.text shouldBe "12.492373"
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
        }

        `when`("savePlace is called with invalid lat/lng") {
            val fakeRepository = FakeTravelPlanRepository()
            val viewModel = PlaceInsertViewModel(fakeRepository)

            viewModel.onPlaceNameChanged(TextFieldValue("Test Place"))

            viewModel.savePlace()

            then("should use default values for lat and lng") {
                eventually(duration = 1.seconds) {
                    fakeRepository.savedPlace?.name shouldBe "Test Place"
                    fakeRepository.savedPlace?.lat shouldBe 0.0
                    fakeRepository.savedPlace?.lng shouldBe 0.0
                }
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
