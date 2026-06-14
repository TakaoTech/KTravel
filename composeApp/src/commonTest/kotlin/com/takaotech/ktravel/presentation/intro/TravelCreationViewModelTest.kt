package com.takaotech.ktravel.presentation.intro

import androidx.compose.ui.text.input.TextFieldValue
import com.takaotech.ktravel.core.ui.FieldValidationState
import com.takaotech.ktravel.di.PlanningGraph
import com.takaotech.ktravel.di.PlanningGraphStore
import com.takaotech.ktravel.domain.repository.TravelManagerRepository
import com.takaotech.ktravel.domain.repository.TravelPlanRepository
import dev.mokkery.answering.calls
import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.everySuspend
import dev.mokkery.matcher.any
import dev.mokkery.mock
import dev.mokkery.verifySuspend
import io.kotest.assertions.nondeterministic.eventually
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.types.shouldBeInstanceOf
import kotlin.time.Duration.Companion.seconds
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
class TravelCreationViewModelTest : BehaviorSpec() {
    init {
        coroutineTestScope = true
        coroutineDebugProbes = true

        val mockTravelPlanRepository: TravelPlanRepository = mock()
        val mockTravelManagerRepository: TravelManagerRepository = mock()

        val mockPlanningGraph: PlanningGraph = mock()
        every { mockPlanningGraph.travelPlanRepository } returns mockTravelPlanRepository

        val mockFactory: PlanningGraph.Factory = mock()
        every { mockFactory.create(any()) } returns mockPlanningGraph

        val planningGraphStore = PlanningGraphStore(mockFactory)

        fun createViewModel() =
            TravelCreationViewModel(mockTravelManagerRepository, planningGraphStore)

        given("a TravelCreationViewModel with initial state") {
            val viewModel = createViewModel()

            then("should have default UI state") {
                viewModel.uiState.value.travelName.value.text shouldBe ""
                viewModel.uiState.value.startDateMillis shouldNotBe null
                viewModel.uiState.value.endDateMillis shouldNotBe null
                viewModel.uiState.value.isLoading shouldBe false
                viewModel.uiState.value.error shouldBe null
                viewModel.uiState.value.createdTravelId shouldBe null
            }

            then("travel name field should be in None validation state") {
                viewModel.uiState.value.travelName.validationState shouldBe FieldValidationState.None
            }
        }

        given("a TravelCreationViewModel for travel name changes") {
            val viewModel = createViewModel()

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
            val viewModel = createViewModel()

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

                viewModel.onDateRangeChange(startDateMillis, 0L)

                then("should update start date") {
                    viewModel.uiState.value.startDateMillis shouldBe startDateMillis
                }

                then("should keep end date as 0L") {
                    viewModel.uiState.value.endDateMillis shouldBe 0L
                }
            }

            `when`("onDateRangeChange is called with only end date") {
                val endDateMillis = 1709251200000L

                viewModel.onDateRangeChange(0L, endDateMillis)

                then("should keep start date as null") {
                    viewModel.uiState.value.startDateMillis shouldBe 0L
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

                then("should reset start date to null (not 0)") {
                    viewModel.uiState.value.startDateMillis shouldBe 0
                }

                then("should keep non-zero end date") {
                    viewModel.uiState.value.endDateMillis shouldBe endDateMillis
                }
            }

            `when`("onDateRangeChange is called with zero end and non-zero start") {
                val startDateMillis = 1704067200000L

                viewModel.onDateRangeChange(startDateMillis, 0L)

                then("should keep non-zero start date") {
                    viewModel.uiState.value.startDateMillis shouldBe startDateMillis
                }

                then("should reset end date to 0L") {
                    viewModel.uiState.value.endDateMillis shouldBe 0L
                }
            }
        }

        given("a TravelCreationViewModel for validation scenarios") {
            val viewModel = createViewModel()

            `when`("createTravelPlan is called with empty name and valid dates") {
                val startDateMillis = 1704067200000L
                val endDateMillis = 1709251200000L
                viewModel.onDateRangeChange(startDateMillis, endDateMillis)
                viewModel.createTravelPlan()

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
                viewModel.onDateRangeChange(0L, 1709251200000L)
                viewModel.createTravelPlan()

                then("should show combined error message for missing dates") {
                    val errorMessage = viewModel.uiState.value.error ?: ""
                    errorMessage shouldBe "Compila tutti i campi"
                }
            }

            `when`("createTravelPlan is called with valid name and missing end date only") {
                val name = TextFieldValue("My Valid Trip")
                viewModel.onNameChange(name)
                viewModel.onDateRangeChange(1704067200000L, 0L)
                viewModel.createTravelPlan()

                then("should show combined error message for missing dates") {
                    val errorMessage = viewModel.uiState.value.error ?: ""
                    errorMessage shouldBe "Compila tutti i campi"
                }
            }
        }

        given("a TravelCreationViewModel with valid data ready") {
            val mockTravelId = Uuid.generateV4().toString()

            everySuspend { mockTravelPlanRepository.updatePeriod(any(), any()) } returns Unit
            everySuspend {
                mockTravelManagerRepository.createTravelPlan(any(), any(), any())
            } calls {
                mockTravelId
            }

            val viewModel = createViewModel()

            val name = TextFieldValue("Wonderful Summer Trip")
            viewModel.onNameChange(name)

            val startDateMillis = 1704067200000L
            val endDateMillis = 1709251200000L
            viewModel.onDateRangeChange(startDateMillis, endDateMillis)

            `when`("createTravelPlan is called with valid data") {
                viewModel.createTravelPlan()

                then("should not have any errors after successful creation") {
                    eventually(duration = 1.seconds) {
                        viewModel.uiState.value.error shouldBe null
                    }
                }

                then("should mark completed with created ID") {
                    eventually(duration = 1.seconds) {
                        val travelId = viewModel.uiState.value.createdTravelId
                        travelId shouldBe mockTravelId
                    }
                }

                then("should not be loading on completion") {
                    eventually(duration = 1.seconds) {
                        viewModel.uiState.value.isLoading shouldBe false
                    }
                }

                then("should call updatePeriod on TravelPlanRepository") {
                    eventually(duration = 1.seconds) {
                        verifySuspend {
                            mockTravelPlanRepository.updatePeriod(
                                startDateMillis,
                                endDateMillis
                            )
                        }
                    }
                }
            }
        }

        given("a TravelCreationViewModel for error clearing via data input") {
            val viewModel = createViewModel()

            `when`("onNameChange is called after validation error") {
                viewModel.createTravelPlan()

                val validName = TextFieldValue("Valid Name After Error")
                viewModel.onNameChange(validName)

                then("should reset travel name value") {
                    viewModel.uiState.value.travelName.value.text shouldBe "Valid Name After Error"
                }
            }
        }
    }
}
