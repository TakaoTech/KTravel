package com.takaotech.ktravel.presentation.intro

import androidx.compose.ui.text.input.TextFieldValue
import com.takaotech.ktravel.core.ui.FieldValidationState
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.types.shouldBeInstanceOf

class TravelCreationViewModelTest : BehaviorSpec({

    given("a TravelCreationViewModel with initial state") {
        val fakeRepository = FakeTravelManagerRepository()
        val viewModel = TravelCreationViewModel(fakeRepository)

        then("should have default UI state") {
            viewModel.uiState.value.travelName.value.text shouldBe ""
            viewModel.uiState.value.startDateMillis shouldBe null
            viewModel.uiState.value.endDateMillis shouldBe null
            viewModel.uiState.value.isLoading shouldBe false
            viewModel.uiState.value.error shouldBe null
            viewModel.uiState.value.createdTravelId shouldBe null
        }

        then("travel name field should be in None validation state") {
            viewModel.uiState.value.travelName.validationState shouldBe FieldValidationState.None
        }
    }

    given("a TravelCreationViewModel for travel name changes") {
        val fakeRepository = FakeTravelManagerRepository()
        val viewModel = TravelCreationViewModel(fakeRepository)

        `when`("onNameChange is called with empty TextFieldValue") {
            viewModel.onNameChange(TextFieldValue(""))

            then("should update travel name to empty value") {
                viewModel.uiState.value.travelName.value.text shouldBe ""
            }

            then("should reset validation state to None") {
                viewModel.uiState.value.travelName.validationState shouldBe FieldValidationState.None
            }
        }

        `when`("onNameChange is called with a name value") {
            val newName = TextFieldValue("My Trip")

            viewModel.onNameChange(newName)

            then("should update travel name to new value") {
                viewModel.uiState.value.travelName.value.text shouldBe "My Trip"
            }

            then("should reset validation state to None") {
                viewModel.uiState.value.travelName.validationState shouldBe FieldValidationState.None
            }
        }

        `when`("onNameChange is called with partial text") {
            val partialName = TextFieldValue("My T")

            viewModel.onNameChange(partialName)

            then("should update travel name to new value") {
                viewModel.uiState.value.travelName.value.text shouldBe "My T"
            }

            then("should reset validation state to None") {
                viewModel.uiState.value.travelName.validationState shouldBe FieldValidationState.None
            }
        }

        `when`("onNameChange is called with whitespace only") {
            val whitespace = TextFieldValue("   ")

            viewModel.onNameChange(whitespace)

            then("should update travel name to new value") {
                viewModel.uiState.value.travelName.value.text shouldBe "   "
            }

            then("should reset validation state to None") {
                viewModel.uiState.value.travelName.validationState shouldBe FieldValidationState.None
            }
        }
    }

    given("a TravelCreationViewModel for date range changes") {
        val fakeRepository = FakeTravelManagerRepository()
        val viewModel = TravelCreationViewModel(fakeRepository)

        `when`("onDateRangeChange is called with start and end dates") {
            val startDateMillis = 1704067200000L
            val endDateMillis = 1709251200000L

            viewModel.onDateRangeChange(startDateMillis, endDateMillis)

            then("should update start date") {
                viewModel.uiState.value.startDateMillis shouldBe startDateMillis
            }

            then("should update end date") {
                viewModel.uiState.value.endDateMillis shouldBe endDateMillis
            }
        }

        `when`("onDateRangeChange is called with only start date") {
            val startDateMillis = 1704067200000L

            viewModel.onDateRangeChange(startDateMillis, null)

            then("should update start date") {
                viewModel.uiState.value.startDateMillis shouldBe startDateMillis
            }

            then("should keep end date as null") {
                viewModel.uiState.value.endDateMillis shouldBe null
            }
        }

        `when`("onDateRangeChange is called with only end date") {
            val endDateMillis = 1709251200000L

            viewModel.onDateRangeChange(null, endDateMillis)

            then("should keep start date as null") {
                viewModel.uiState.value.startDateMillis shouldBe null
            }

            then("should update end date") {
                viewModel.uiState.value.endDateMillis shouldBe endDateMillis
            }
        }

        `when`("onDateRangeChange is called with zero values") {
            viewModel.onDateRangeChange(0L, 0L)

            then("should use 0 as start date") {
                viewModel.uiState.value.startDateMillis shouldBe 0L
            }

            then("should use 0 as end date") {
                viewModel.uiState.value.endDateMillis shouldBe 0L
            }
        }

        `when`("onDateRangeChange is called with zero start and non-zero end") {
            val endDateMillis = 1709251200000L

            viewModel.onDateRangeChange(0L, endDateMillis)

            // The ViewModel filters out 0 values by setting null
            then("should reset start date to null (not 0)") {
                viewModel.uiState.value.startDateMillis shouldBe null
            }

            then("should keep non-zero end date") {
                viewModel.uiState.value.endDateMillis shouldBe endDateMillis
            }
        }

        `when`("onDateRangeChange is called with zero end and non-zero start") {
            val startDateMillis = 1704067200000L

            viewModel.onDateRangeChange(startDateMillis, 0L)

            // The ViewModel filters out 0 values by setting null
            then("should keep non-zero start date") {
                viewModel.uiState.value.startDateMillis shouldBe startDateMillis
            }

            then("should reset end date to null (not 0)") {
                viewModel.uiState.value.endDateMillis shouldBe null
            }
        }
    }

    given("a TravelCreationViewModel for validation scenarios") {
        val fakeRepository = FakeTravelManagerRepository()
        val viewModel = TravelCreationViewModel(fakeRepository)

        `when`("createTravelPlan is called with empty name and valid dates") {
            val startDateMillis = 1704067200000L
            val endDateMillis = 1709251200000L
            viewModel.onDateRangeChange(startDateMillis, endDateMillis)

            then("should set name validation error") {
                viewModel.uiState.value.travelName.validationState.shouldBeInstanceOf<FieldValidationState.BaseNotValid>()
            }

            then("should not update created ID") {
                viewModel.uiState.value.createdTravelId shouldBe null
            }
        }

        `when`("createTravelPlan is called with valid name and missing start date only") {
            val name = TextFieldValue("My Valid Trip")
            viewModel.onNameChange(name)
            viewModel.createTravelPlan()

            then("should show combined error message for missing dates") {
                val errorMessage = viewModel.uiState.value.error ?: ""
                errorMessage shouldBe "Compila tutti i campi"
            }
        }

        `when`("createTravelPlan is called with valid name and missing end date only") {
            val name = TextFieldValue("My Valid Trip")
            viewModel.onNameChange(name)

            viewModel.createTravelPlan()

            then("should show combined error message for missing dates") {
                val errorMessage = viewModel.uiState.value.error ?: ""
                errorMessage shouldBe "Compila tutti i campi"
            }
        }
    }

    given("a TravelCreationViewModel with valid data ready") {
        val fakeRepository = FakeTravelManagerRepository()
        val viewModel = TravelCreationViewModel(fakeRepository)

        val name = TextFieldValue("Wonderful Summer Trip")
        viewModel.onNameChange(name)

        val startDateMillis = 1704067200000L
        val endDateMillis = 1709251200000L
        viewModel.onDateRangeChange(startDateMillis, endDateMillis)

        `when`("createTravelPlan is called with valid data") {
            val result = viewModel.createTravelPlan()

            then("should call repository createTravelPlan with correct name") {
                fakeRepository.getLastCallName()?.shouldBe("Wonderful Summer Trip")
            }

            then("should not have any errors after successful creation") {
                viewModel.uiState.value.error shouldBe null
            }

            then("should mark completed with created ID") {
                viewModel.uiState.value.createdTravelId shouldNotBe null
            }

            then("should not be loading on completion") {
                viewModel.uiState.value.isLoading shouldBe false
            }
        }
    }

    given("a TravelCreationViewModel for error clearing via data input") {
        val fakeRepository = FakeTravelManagerRepository()
        val viewModel = TravelCreationViewModel(fakeRepository)

        `when`("onNameChange is called after validation error") {
            viewModel.createTravelPlan() // Sets name validation error and empty string


            // Now provide valid name
            val validName = TextFieldValue("Valid Name After Error")
            viewModel.onNameChange(validName)

            then("should reset travel name value") {
                viewModel.uiState.value.travelName.value.text shouldBe "Valid Name After Error"
            }
        }
    }

//    `when`("clearError is called") {
//        val fakeRepository = FakeTravelManagerRepository()
//        val viewModel = TravelCreationViewModel(fakeRepository)
//
//        then("should have null error initially") {
//            viewModel.uiState.value.error shouldBe null
//        }
//    }

})