package com.takaotech.ktravel.presentation.place

import androidx.compose.ui.text.input.TextFieldValue
import com.takaotech.ktravel.core.ui.FieldValidationState
import com.takaotech.ktravel.di.PlanningScope
import com.takaotech.ktravel.domain.usecase.SavePlaceUseCase
import dev.mokkery.MockMode
import dev.mokkery.matcher.any
import dev.mokkery.mock
import dev.mokkery.verify.VerifyMode
import dev.mokkery.verifySuspend
import io.kotest.assertions.nondeterministic.eventually
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import ktravel.composeapp.generated.resources.Res
import ktravel.composeapp.generated.resources.place_insert_error_lat_empty
import ktravel.composeapp.generated.resources.place_insert_error_lat_invalid_format
import ktravel.composeapp.generated.resources.place_insert_error_lng_empty
import ktravel.composeapp.generated.resources.place_insert_error_lng_invalid_format
import ktravel.composeapp.generated.resources.place_insert_error_name_empty
import org.koin.core.component.KoinComponent
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import kotlin.time.Duration.Companion.seconds

private const val TRAVEL_ID = "TRAVEL_ID"

class PlaceInsertViewModelTest : BehaviorSpec(), KoinComponent {

    init {
        beforeSpec {
            startKoin {
                modules(module {
                    scope<PlanningScope> { }
                })
            }
        }
        afterSpec {
            stopKoin()
        }
        afterEach {
            getKoin().getScopeOrNull(TRAVEL_ID)?.close()
        }

        given("a PlaceInsertViewModel with initial state") {
            val viewModel = PlaceInsertViewModel(TRAVEL_ID, null)

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
                val viewModel = PlaceInsertViewModel(TRAVEL_ID, null)

                viewModel.onInputModeChanged(PlaceInputMode.SEARCH)

                then("should update input mode to SEARCH") {
                    viewModel.uiState.value.inputMode shouldBe PlaceInputMode.SEARCH
                }
            }

            `when`("onInputModeChanged is called with LAT_LNG mode") {
                val viewModel = PlaceInsertViewModel(TRAVEL_ID, null)
                viewModel.onInputModeChanged(PlaceInputMode.SEARCH)

                viewModel.onInputModeChanged(PlaceInputMode.LAT_LNG)

                then("should update input mode to LAT_LNG") {
                    viewModel.uiState.value.inputMode shouldBe PlaceInputMode.LAT_LNG
                }
            }
        }

        given("a PlaceInsertViewModel for place name changes") {
            `when`("onPlaceNameChanged is called with a name") {
                val viewModel = PlaceInsertViewModel(TRAVEL_ID, null)
                val newName = TextFieldValue("Colosseo")

                viewModel.onPlaceNameChanged(newName)

                then("should update place name") {
                    viewModel.uiState.value.placeName.value shouldBe newName
                }
            }
        }

        given("a PlaceInsertViewModel for latitude validation") {
            `when`("onPlaceLatChanged is called with valid latitude 45.0") {
                val viewModel = PlaceInsertViewModel(TRAVEL_ID, null)
                val validLat = TextFieldValue("45.0")

                viewModel.onPlaceLatChanged(validLat)

                then("should update place lat") {
                    viewModel.uiState.value.placeLat.value shouldBe validLat
                }
            }

            `when`("onPlaceLatChanged is called with valid latitude 90") {
                val viewModel = PlaceInsertViewModel(TRAVEL_ID, null)
                val validLat = TextFieldValue("90")

                viewModel.onPlaceLatChanged(validLat)

                then("should update place lat") {
                    viewModel.uiState.value.placeLat.value shouldBe validLat
                }
            }

            `when`("onPlaceLatChanged is called with valid latitude -90") {
                val viewModel = PlaceInsertViewModel(TRAVEL_ID, null)
                val validLat = TextFieldValue("-90")

                viewModel.onPlaceLatChanged(validLat)

                then("should update place lat") {
                    viewModel.uiState.value.placeLat.value shouldBe validLat
                }
            }

            `when`("onPlaceLatChanged is called with valid latitude 0") {
                val viewModel = PlaceInsertViewModel(TRAVEL_ID, null)
                val validLat = TextFieldValue("0")

                viewModel.onPlaceLatChanged(validLat)

                then("should update place lat") {
                    viewModel.uiState.value.placeLat.value shouldBe validLat
                }
            }

            `when`("onPlaceLatChanged is called with valid latitude with decimals") {
                val viewModel = PlaceInsertViewModel(TRAVEL_ID, null)
                val validLat = TextFieldValue("41.890251")

                viewModel.onPlaceLatChanged(validLat)

                then("should update place lat") {
                    viewModel.uiState.value.placeLat.value shouldBe validLat
                }
            }
        }

        given("a PlaceInsertViewModel for longitude validation") {
            `when`("onPlaceLngChanged is called with valid longitude 45.0") {
                val viewModel = PlaceInsertViewModel(TRAVEL_ID, null)
                val validLng = TextFieldValue("45.0")

                viewModel.onPlaceLngChanged(validLng)

                then("should update place lng") {
                    viewModel.uiState.value.placeLng.value shouldBe validLng
                }
            }

            `when`("onPlaceLngChanged is called with valid longitude -180") {
                val viewModel = PlaceInsertViewModel(TRAVEL_ID, null)
                val validLng = TextFieldValue("-180")

                viewModel.onPlaceLngChanged(validLng)

                then("should update place lng") {
                    viewModel.uiState.value.placeLng.value shouldBe validLng
                }
            }

            `when`("onPlaceLngChanged is called with valid longitude 0") {
                val viewModel = PlaceInsertViewModel(TRAVEL_ID, null)
                val validLng = TextFieldValue("0")

                viewModel.onPlaceLngChanged(validLng)

                then("should update place lng") {
                    viewModel.uiState.value.placeLng.value shouldBe validLng
                }
            }

            `when`("onPlaceLngChanged is called with valid longitude with decimals") {
                val viewModel = PlaceInsertViewModel(TRAVEL_ID, null)
                val validLng = TextFieldValue("12.492373")

                viewModel.onPlaceLngChanged(validLng)

                then("should update place lng") {
                    viewModel.uiState.value.placeLng.value shouldBe validLng
                }
            }
        }

        given("a PlaceInsertViewModel for search query changes") {
            `when`("onSearchQueryChanged is called with a query") {
                val viewModel = PlaceInsertViewModel(TRAVEL_ID, null)
                val query = TextFieldValue("Roma")

                viewModel.onSearchQueryChanged(query)

                then("should update search query") {
                    viewModel.uiState.value.searchQuery.value shouldBe query
                }
            }
        }

        given("a PlaceInsertViewModel for time selection") {
            `when`("onTimeSelected is called with hour and minute") {
                val viewModel = PlaceInsertViewModel(TRAVEL_ID, null)

                viewModel.onTimeSelected(14, 30)

                then("should update selected time") {
                    viewModel.uiState.value.selectedTime shouldBe LocalTime(14, 30)
                }
            }
        }

        given("a PlaceInsertViewModel for date selection") {
            `when`("onDateSelected is called with a date") {
                val viewModel = PlaceInsertViewModel(TRAVEL_ID, null)
                val date = LocalDate(2024, 6, 15)

                viewModel.onDateSelected(date)

                then("should update selected date") {
                    viewModel.uiState.value.selectedDate shouldBe date
                }
            }

            `when`("onDateSelected is called with null") {
                val viewModel = PlaceInsertViewModel(TRAVEL_ID, null)
                viewModel.onDateSelected(LocalDate(2024, 6, 15))

                viewModel.onDateSelected(null)

                then("should set selected date to null") {
                    viewModel.uiState.value.selectedDate shouldBe null
                }
            }
        }

        given("a PlaceInsertViewModel for saving a place") {
            `when`("savePlace is called with valid data") {
                val mockSavePlaceUseCase = mock<SavePlaceUseCase>(MockMode.autoUnit)
                getKoin().getOrCreateScope<PlanningScope>(TRAVEL_ID)
                    .declare<SavePlaceUseCase>(mockSavePlaceUseCase)
                val viewModel = PlaceInsertViewModel(TRAVEL_ID, null)

                viewModel.onPlaceNameChanged(TextFieldValue("Colosseo"))
                viewModel.onPlaceLatChanged(TextFieldValue("41.890251"))
                viewModel.onPlaceLngChanged(TextFieldValue("12.492373"))

                viewModel.savePlace()

                then("should call repository savePlace with correct data") {
                    eventually(duration = 1.seconds) {
                        verifySuspend(VerifyMode.soft) {
                            mockSavePlaceUseCase.invoke("Colosseo", 41.890251, 12.492373, null)
                        }
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
                val mockSavePlaceUseCase = mock<SavePlaceUseCase>(MockMode.autoUnit)
                getKoin().getOrCreateScope<PlanningScope>(TRAVEL_ID)
                    .declare<SavePlaceUseCase>(mockSavePlaceUseCase)
                val viewModel = PlaceInsertViewModel(TRAVEL_ID, null)

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
                        verifySuspend(VerifyMode.exactly(0)) {
                            mockSavePlaceUseCase.invoke(any(), any(), any(), any())
                        }
                    }
                }
            }

            `when`("savePlace is called with empty latitude in LAT_LNG mode") {
                val mockSavePlaceUseCase = mock<SavePlaceUseCase>(MockMode.autoUnit)
                getKoin().getOrCreateScope<PlanningScope>(TRAVEL_ID)
                    .declare<SavePlaceUseCase>(mockSavePlaceUseCase)
                val viewModel = PlaceInsertViewModel(TRAVEL_ID, null)

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
                        verifySuspend(VerifyMode.exactly(0)) {
                            mockSavePlaceUseCase.invoke(any(), any(), any(), any())
                        }
                    }
                }
            }

            `when`("savePlace is called with empty longitude in LAT_LNG mode") {
                val mockSavePlaceUseCase = mock<SavePlaceUseCase>(MockMode.autoUnit)
                getKoin().getOrCreateScope<PlanningScope>(TRAVEL_ID)
                    .declare<SavePlaceUseCase>(mockSavePlaceUseCase)
                val viewModel = PlaceInsertViewModel(TRAVEL_ID, null)

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
                        verifySuspend(VerifyMode.exactly(0)) {
                            mockSavePlaceUseCase.invoke(any(), any(), any(), any())
                        }
                    }
                }
            }

            `when`("savePlace is called with invalid latitude format") {
                val mockSavePlaceUseCase = mock<SavePlaceUseCase>(MockMode.autoUnit)
                getKoin().getOrCreateScope<PlanningScope>(TRAVEL_ID)
                    .declare<SavePlaceUseCase>(mockSavePlaceUseCase)
                val viewModel = PlaceInsertViewModel(TRAVEL_ID, null)

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
                        verifySuspend(VerifyMode.exactly(0)) {
                            mockSavePlaceUseCase.invoke(any(), any(), any(), any())
                        }
                    }
                }
            }

            `when`("savePlace is called with invalid longitude format") {
                val mockSavePlaceUseCase = mock<SavePlaceUseCase>(MockMode.autoUnit)
                getKoin().getOrCreateScope<PlanningScope>(TRAVEL_ID)
                    .declare<SavePlaceUseCase>(mockSavePlaceUseCase)
                val viewModel = PlaceInsertViewModel(TRAVEL_ID, null)

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
                        verifySuspend(VerifyMode.exactly(0)) {
                            mockSavePlaceUseCase.invoke(any(), any(), any(), any())
                        }
                    }
                }
            }

            `when`("savePlace is called with latitude out of range") {
                val mockSavePlaceUseCase = mock<SavePlaceUseCase>(MockMode.autoUnit)
                getKoin().getOrCreateScope<PlanningScope>(TRAVEL_ID)
                    .declare<SavePlaceUseCase>(mockSavePlaceUseCase)
                val viewModel = PlaceInsertViewModel(TRAVEL_ID, null)

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
                        verifySuspend(VerifyMode.exactly(0)) {
                            mockSavePlaceUseCase.invoke(any(), any(), any(), any())
                        }
                    }
                }
            }

            `when`("savePlace is called with longitude out of range") {
                val mockSavePlaceUseCase = mock<SavePlaceUseCase>(MockMode.autoUnit)
                getKoin().getOrCreateScope<PlanningScope>(TRAVEL_ID)
                    .declare<SavePlaceUseCase>(mockSavePlaceUseCase)
                val viewModel = PlaceInsertViewModel(TRAVEL_ID, null)

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
                        verifySuspend(VerifyMode.exactly(0)) {
                            mockSavePlaceUseCase.invoke(any(), any(), any(), any())
                        }
                    }
                }
            }
        }

        given("a PlaceInsertViewModel with dayId passed") {
            `when`("savePlace is called with valid data and dayId is provided") {
                val mockSavePlaceUseCase = mock<SavePlaceUseCase>(MockMode.autoUnit)
                getKoin().getOrCreateScope<PlanningScope>(TRAVEL_ID)
                    .declare<SavePlaceUseCase>(mockSavePlaceUseCase)
                val testDayId = "day-123"
                val viewModel = PlaceInsertViewModel(TRAVEL_ID, testDayId)

                viewModel.onPlaceNameChanged(TextFieldValue("Colosseo"))
                viewModel.onPlaceLatChanged(TextFieldValue("41.890251"))
                viewModel.onPlaceLngChanged(TextFieldValue("12.492373"))

                viewModel.savePlace()

                then("should call repository savePlace with correct place data") {
                    eventually(duration = 1.seconds) {
                        verifySuspend(VerifyMode.soft) {
                            mockSavePlaceUseCase.invoke("Colosseo", 41.890251, 12.492373, testDayId)
                        }
                    }
                }
            }

            `when`("savePlace is called with valid data and dayId is null") {
                val mockSavePlaceUseCase = mock<SavePlaceUseCase>(MockMode.autoUnit)
                getKoin().getOrCreateScope<PlanningScope>(TRAVEL_ID)
                    .declare<SavePlaceUseCase>(mockSavePlaceUseCase)
                val viewModel = PlaceInsertViewModel(TRAVEL_ID, null)

                viewModel.onPlaceNameChanged(TextFieldValue("Fontana di Trevi"))
                viewModel.onPlaceLatChanged(TextFieldValue("41.900932"))
                viewModel.onPlaceLngChanged(TextFieldValue("12.483313"))

                viewModel.savePlace()

                then("should call repository savePlace with correct place data") {
                    eventually(duration = 1.seconds) {
                        verifySuspend(VerifyMode.soft) {
                            mockSavePlaceUseCase.invoke(
                                "Fontana di Trevi",
                                41.900932,
                                12.483313,
                                null
                            )
                        }
                    }
                }
            }

            `when`("savePlace is called with different dayIds") {
                val mockSavePlaceUseCase = mock<SavePlaceUseCase>(MockMode.autoUnit)
                getKoin().getOrCreateScope<PlanningScope>(TRAVEL_ID)
                    .declare<SavePlaceUseCase>(mockSavePlaceUseCase)
                val dayId1 = "day-abc"
                val dayId2 = "day-xyz"
                val viewModel1 = PlaceInsertViewModel(TRAVEL_ID, dayId1)
                val viewModel2 = PlaceInsertViewModel(TRAVEL_ID, dayId2)

                viewModel1.onPlaceNameChanged(TextFieldValue("Place 1"))
                viewModel1.onPlaceLatChanged(TextFieldValue("45.0"))
                viewModel1.onPlaceLngChanged(TextFieldValue("10.0"))
                viewModel1.savePlace()

                viewModel2.onPlaceNameChanged(TextFieldValue("Place 2"))
                viewModel2.onPlaceLatChanged(TextFieldValue("46.0"))
                viewModel2.onPlaceLngChanged(TextFieldValue("11.0"))
                viewModel2.savePlace()

                then("should pass correct dayId to each repository") {
                    eventually(duration = 1.seconds) {
                        verifySuspend(VerifyMode.soft) {
                            mockSavePlaceUseCase.invoke(any(), any(), any(), dayId1)
                            mockSavePlaceUseCase.invoke(any(), any(), any(), dayId2)
                        }
                    }
                }
            }

            `when`("savePlace is called with invalid data and dayId is provided") {
                val mockSavePlaceUseCase = mock<SavePlaceUseCase>(MockMode.autoUnit)
                getKoin().getOrCreateScope<PlanningScope>(TRAVEL_ID)
                    .declare<SavePlaceUseCase>(mockSavePlaceUseCase)
                val testDayId = "day-456"
                val viewModel = PlaceInsertViewModel(TRAVEL_ID, testDayId)

                viewModel.savePlace()

                then("should not save place") {
                    eventually(duration = 1.seconds) {
                        verifySuspend(VerifyMode.exactly(0)) {
                            mockSavePlaceUseCase.invoke(any(), any(), any(), any())
                        }
                    }
                }

                then("should not pass dayId to repository") {
                    eventually(duration = 1.seconds) {
                        verifySuspend(VerifyMode.exactly(0)) {
                            mockSavePlaceUseCase.invoke(any(), any(), any(), testDayId)
                        }
                    }
                }
            }
        }

        given("a PlaceInsertViewModel for lat/lng paste detection") {
            `when`("onPlaceLatChanged is called with 'lat, lng' format") {
                val viewModel = PlaceInsertViewModel(TRAVEL_ID, null)

                viewModel.onPlaceLatChanged(TextFieldValue("41.890251, 12.492373"))

                then("should split and set both lat and lng fields") {
                    viewModel.uiState.value.placeLat.value.text shouldBe "41.890251"
                    viewModel.uiState.value.placeLng.value.text shouldBe "12.492373"
                }
            }

            `when`("onPlaceLngChanged is called with 'lat, lng' format") {
                val viewModel = PlaceInsertViewModel(TRAVEL_ID, null)

                viewModel.onPlaceLngChanged(TextFieldValue("41.890251, 12.492373"))

                then("should split and set both lat and lng fields") {
                    viewModel.uiState.value.placeLat.value.text shouldBe "41.890251"
                    viewModel.uiState.value.placeLng.value.text shouldBe "12.492373"
                }
            }

            `when`("onPlaceLatChanged is called with 'lat lng' space-separated format") {
                val viewModel = PlaceInsertViewModel(TRAVEL_ID, null)

                viewModel.onPlaceLatChanged(TextFieldValue("41.890251 12.492373"))

                then("should split and set both lat and lng fields") {
                    viewModel.uiState.value.placeLat.value.text shouldBe "41.890251"
                    viewModel.uiState.value.placeLng.value.text shouldBe "12.492373"
                }
            }

            `when`("onPlaceLatChanged is called with a single lat value") {
                val viewModel = PlaceInsertViewModel(TRAVEL_ID, null)

                viewModel.onPlaceLatChanged(TextFieldValue("41.890251"))

                then("should update only lat field") {
                    viewModel.uiState.value.placeLat.value.text shouldBe "41.890251"
                    viewModel.uiState.value.placeLng.value.text shouldBe ""
                }
            }

            `when`("onPlaceLngChanged is called with a single lng value") {
                val viewModel = PlaceInsertViewModel(TRAVEL_ID, null)

                viewModel.onPlaceLngChanged(TextFieldValue("12.492373"))

                then("should update only lng field") {
                    viewModel.uiState.value.placeLat.value.text shouldBe ""
                    viewModel.uiState.value.placeLng.value.text shouldBe "12.492373"
                }
            }

            `when`("onPlaceLatChanged is called with negative coordinates 'lat, lng' format") {
                val viewModel = PlaceInsertViewModel(TRAVEL_ID, null)

                viewModel.onPlaceLatChanged(TextFieldValue("-33.865143, 151.209900"))

                then("should split and set both lat and lng fields") {
                    viewModel.uiState.value.placeLat.value.text shouldBe "-33.865143"
                    viewModel.uiState.value.placeLng.value.text shouldBe "151.209900"
                }
            }
        }

        given("a PlaceInsertViewModel with isBulk enabled") {
            `when`("savePlace is called with valid data and isBulk is true") {
                val mockSavePlaceUseCase = mock<SavePlaceUseCase>(MockMode.autoUnit)
                getKoin().getOrCreateScope<PlanningScope>(TRAVEL_ID)
                    .declare<SavePlaceUseCase>(mockSavePlaceUseCase)
                val viewModel = PlaceInsertViewModel(TRAVEL_ID, null)

                viewModel.onBulkChanged(true)
                viewModel.onPlaceNameChanged(TextFieldValue("Colosseo"))
                viewModel.onPlaceLatChanged(TextFieldValue("41.890251"))
                viewModel.onPlaceLngChanged(TextFieldValue("12.492373"))

                viewModel.savePlace()

                then("should save the place to the repository") {
                    eventually(duration = 1.seconds) {
                        verifySuspend(VerifyMode.soft) {
                            mockSavePlaceUseCase.invoke("Colosseo", 41.890251, 12.492373, null)
                        }
                    }
                }

                then("should reset placeName field after saving") {
                    eventually(duration = 1.seconds) {
                        viewModel.uiState.value.placeName.value.text shouldBe ""
                    }
                }

                then("should reset placeLat field after saving") {
                    eventually(duration = 1.seconds) {
                        viewModel.uiState.value.placeLat.value.text shouldBe ""
                    }
                }

                then("should reset placeLng field after saving") {
                    eventually(duration = 1.seconds) {
                        viewModel.uiState.value.placeLng.value.text shouldBe ""
                    }
                }

                then("should reset searchQuery field after saving") {
                    eventually(duration = 1.seconds) {
                        viewModel.uiState.value.searchQuery.value.text shouldBe ""
                    }
                }

                then("should keep isBulk as true after saving") {
                    eventually(duration = 1.seconds) {
                        viewModel.uiState.value.isBulk shouldBe true
                    }
                }
            }

            `when`("savePlace is called multiple times with isBulk true") {
                val mockSavePlaceUseCase = mock<SavePlaceUseCase>(MockMode.autoUnit)
                getKoin().getOrCreateScope<PlanningScope>(TRAVEL_ID)
                    .declare<SavePlaceUseCase>(mockSavePlaceUseCase)
                val viewModel = PlaceInsertViewModel(TRAVEL_ID, null)

                viewModel.onBulkChanged(true)

                viewModel.onPlaceNameChanged(TextFieldValue("Colosseo"))
                viewModel.onPlaceLatChanged(TextFieldValue("41.890251"))
                viewModel.onPlaceLngChanged(TextFieldValue("12.492373"))
                viewModel.savePlace()

                eventually(duration = 1.seconds) {
                    viewModel.uiState.value.placeName.value.text shouldBe ""
                }

                viewModel.onPlaceNameChanged(TextFieldValue("Pantheon"))
                viewModel.onPlaceLatChanged(TextFieldValue("41.898614"))
                viewModel.onPlaceLngChanged(TextFieldValue("12.476869"))
                viewModel.savePlace()

                then("should save the second place to the repository") {
                    eventually(duration = 1.seconds) {
                        verifySuspend(VerifyMode.soft) {
                            mockSavePlaceUseCase.invoke("Pantheon", 41.898614, 12.476869, null)
                        }
                    }
                }

                then("should reset fields after second save") {
                    eventually(duration = 1.seconds) {
                        viewModel.uiState.value.placeName.value.text shouldBe ""
                        viewModel.uiState.value.placeLat.value.text shouldBe ""
                        viewModel.uiState.value.placeLng.value.text shouldBe ""
                    }
                }
            }

            `when`("savePlace is called with invalid data and isBulk is true") {
                val mockSavePlaceUseCase = mock<SavePlaceUseCase>(MockMode.autoUnit)
                getKoin().getOrCreateScope<PlanningScope>(TRAVEL_ID)
                    .declare<SavePlaceUseCase>(mockSavePlaceUseCase)
                val viewModel = PlaceInsertViewModel(TRAVEL_ID, null)

                viewModel.onBulkChanged(true)

                viewModel.savePlace()

                then("should not save the place") {
                    eventually(duration = 1.seconds) {
                        verifySuspend(VerifyMode.exactly(0)) {
                            mockSavePlaceUseCase.invoke(any(), any(), any(), any())
                        }
                    }
                }

                then("should not reset fields when save fails") {
                    eventually(duration = 1.seconds) {
                        viewModel.uiState.value.placeName.validationState.shouldBeInstanceOf<FieldValidationState.BaseNotValid>()
                    }
                }
            }
        }

        given("a PlaceInsertViewModel with isBulk disabled") {
            `when`("savePlace is called with valid data and isBulk is false") {
                val mockSavePlaceUseCase = mock<SavePlaceUseCase>(MockMode.autoUnit)
                getKoin().getOrCreateScope<PlanningScope>(TRAVEL_ID)
                    .declare<SavePlaceUseCase>(mockSavePlaceUseCase)
                val viewModel = PlaceInsertViewModel(TRAVEL_ID, null)

                viewModel.onBulkChanged(false)
                viewModel.onPlaceNameChanged(TextFieldValue("Colosseo"))
                viewModel.onPlaceLatChanged(TextFieldValue("41.890251"))
                viewModel.onPlaceLngChanged(TextFieldValue("12.492373"))

                viewModel.savePlace()

                then("should save the place to the repository") {
                    eventually(duration = 1.seconds) {
                        verifySuspend(VerifyMode.soft) {
                            mockSavePlaceUseCase.invoke("Colosseo", 41.890251, 12.492373, null)
                        }
                    }
                }

                then("should not reset placeName field after saving") {
                    eventually(duration = 1.seconds) {
                        viewModel.uiState.value.placeName.value.text shouldBe "Colosseo"
                    }
                }

                then("should not reset placeLat field after saving") {
                    eventually(duration = 1.seconds) {
                        viewModel.uiState.value.placeLat.value.text shouldBe "41.890251"
                    }
                }

                then("should not reset placeLng field after saving") {
                    eventually(duration = 1.seconds) {
                        viewModel.uiState.value.placeLng.value.text shouldBe "12.492373"
                    }
                }
            }
        }

        given("a PlaceInsertViewModel for isBulk state management") {
            `when`("onBulkChanged is called with true") {
                val viewModel = PlaceInsertViewModel(TRAVEL_ID, null)

                viewModel.onBulkChanged(true)

                then("should set isBulk to true") {
                    viewModel.uiState.value.isBulk shouldBe true
                }
            }

            `when`("onBulkChanged is called with false") {
                val viewModel = PlaceInsertViewModel(TRAVEL_ID, null)

                viewModel.onBulkChanged(false)

                then("should set isBulk to false") {
                    viewModel.uiState.value.isBulk shouldBe false
                }
            }

            `when`("onBulkChanged is toggled from true to false") {
                val viewModel = PlaceInsertViewModel(TRAVEL_ID, null)

                viewModel.onBulkChanged(true)
                viewModel.onBulkChanged(false)

                then("should set isBulk to false") {
                    viewModel.uiState.value.isBulk shouldBe false
                }
            }
        }

        given("a PlaceInsertViewModel for error clearing") {
            `when`("onPlaceNameChanged is called after name error") {
                val mockSavePlaceUseCase = mock<SavePlaceUseCase>(MockMode.autoUnit)
                getKoin().getOrCreateScope<PlanningScope>(TRAVEL_ID)
                    .declare<SavePlaceUseCase>(mockSavePlaceUseCase)
                val viewModel = PlaceInsertViewModel(TRAVEL_ID, null)

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
                val mockSavePlaceUseCase = mock<SavePlaceUseCase>(MockMode.autoUnit)
                getKoin().getOrCreateScope<PlanningScope>(TRAVEL_ID)
                    .declare<SavePlaceUseCase>(mockSavePlaceUseCase)
                val viewModel = PlaceInsertViewModel(TRAVEL_ID, null)

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
                val mockSavePlaceUseCase = mock<SavePlaceUseCase>(MockMode.autoUnit)
                getKoin().getOrCreateScope<PlanningScope>(TRAVEL_ID)
                    .declare<SavePlaceUseCase>(mockSavePlaceUseCase)
                val viewModel = PlaceInsertViewModel(TRAVEL_ID, null)

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
    }
}
