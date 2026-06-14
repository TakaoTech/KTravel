package com.takaotech.ktravel.presentation.intro

import com.takaotech.ktravel.domain.model.TravelPlanSummary
import com.takaotech.ktravel.domain.repository.TravelManagerRepository
import dev.mokkery.answering.calls
import dev.mokkery.answering.returns
import dev.mokkery.everySuspend
import dev.mokkery.mock
import io.kotest.assertions.nondeterministic.eventually
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import kotlinx.datetime.LocalDate
import kotlin.time.Duration.Companion.seconds

class TravelSelectionViewModelTest : BehaviorSpec() {
    init {
        coroutineTestScope = true
        coroutineDebugProbes = true

        val mockRepository: TravelManagerRepository = mock()

        val samplePlan1 = TravelPlanSummary(
            id = "id-1",
            name = "Viaggio a Tokyo",
            periodStart = LocalDate(2024, 3, 1),
            periodEnd = LocalDate(2024, 3, 15)
        )
        val samplePlan2 = TravelPlanSummary(
            id = "id-2",
            name = "Weekend a Roma",
            periodStart = LocalDate(2024, 6, 10),
            periodEnd = LocalDate(2024, 6, 12)
        )
        val samplePlan3 = TravelPlanSummary(
            id = "id-3",
            name = "Vacanza al mare",
            periodStart = LocalDate(2024, 8, 1),
            periodEnd = LocalDate(2024, 8, 14)
        )

        given("a TravelSelectionViewModel when repository returns an empty list") {
            everySuspend { mockRepository.getAllTravelPlans() } returns emptyList()
            val viewModel = TravelSelectionViewModel(mockRepository)

            then("travelList should be empty after loading") {
                eventually(1.seconds) {
                    viewModel.uiState.value.travelList shouldHaveSize 0
                }
            }

            then("isLoading should be false after loading completes") {
                eventually(1.seconds) {
                    viewModel.uiState.value.isLoading shouldBe false
                }
            }

            then("error should be null after successful load") {
                eventually(1.seconds) {
                    viewModel.uiState.value.error shouldBe null
                }
            }
        }

        given("a TravelSelectionViewModel when repository returns a single travel plan") {
            everySuspend { mockRepository.getAllTravelPlans() } returns listOf(samplePlan1)
            val viewModel = TravelSelectionViewModel(mockRepository)

            then("travelList should contain one item") {
                eventually(1.seconds) {
                    viewModel.uiState.value.travelList shouldHaveSize 1
                }
            }

            then("the item should be correctly mapped from domain model") {
                eventually(1.seconds) {
                    val item = viewModel.uiState.value.travelList.first()
                    item.id shouldBe samplePlan1.id
                    item.name shouldBe samplePlan1.name
                    item.periodStart shouldBe samplePlan1.periodStart
                    item.periodEnd shouldBe samplePlan1.periodEnd
                }
            }

            then("isLoading should be false after loading") {
                eventually(1.seconds) {
                    viewModel.uiState.value.isLoading shouldBe false
                }
            }

            then("error should be null after successful load") {
                eventually(1.seconds) {
                    viewModel.uiState.value.error shouldBe null
                }
            }
        }

        given("a TravelSelectionViewModel when repository returns multiple travel plans") {
            everySuspend { mockRepository.getAllTravelPlans() } returns listOf(
                samplePlan1,
                samplePlan2,
                samplePlan3
            )
            val viewModel = TravelSelectionViewModel(mockRepository)

            then("travelList should contain all plans") {
                eventually(1.seconds) {
                    viewModel.uiState.value.travelList shouldHaveSize 3
                }
            }

            then("plans should be mapped in the correct order") {
                eventually(1.seconds) {
                    val travelList = viewModel.uiState.value.travelList
                    travelList[0].id shouldBe samplePlan1.id
                    travelList[1].id shouldBe samplePlan2.id
                    travelList[2].id shouldBe samplePlan3.id
                }
            }

            then("each plan should have its name correctly mapped") {
                eventually(1.seconds) {
                    val travelList = viewModel.uiState.value.travelList
                    travelList[0].name shouldBe samplePlan1.name
                    travelList[1].name shouldBe samplePlan2.name
                    travelList[2].name shouldBe samplePlan3.name
                }
            }
        }

        given("a TravelSelectionViewModel when repository throws an exception") {
            everySuspend { mockRepository.getAllTravelPlans() } calls { throw RuntimeException("Network error") }
            val viewModel = TravelSelectionViewModel(mockRepository)

            then("error should be set to exception message") {
                eventually(1.seconds) {
                    viewModel.uiState.value.error shouldBe "Network error"
                }
            }

            then("isLoading should be false after failure") {
                eventually(1.seconds) {
                    viewModel.uiState.value.isLoading shouldBe false
                }
            }

            then("travelList should remain empty") {
                eventually(1.seconds) {
                    viewModel.uiState.value.travelList shouldHaveSize 0
                }
            }
        }

        given("a TravelSelectionViewModel after initial load with two plans") {
            everySuspend { mockRepository.getAllTravelPlans() } returns listOf(
                samplePlan1,
                samplePlan2
            )
            val viewModel = TravelSelectionViewModel(mockRepository)

            `when`("loadTravelPlans() is called again with a single updated plan") {
                eventually(1.seconds) {
                    viewModel.uiState.value.travelList shouldHaveSize 2
                }

                everySuspend { mockRepository.getAllTravelPlans() } returns listOf(samplePlan3)
                viewModel.loadTravelPlans()

                then("travelList should be replaced, not appended") {
                    eventually(1.seconds) {
                        viewModel.uiState.value.travelList shouldHaveSize 1
                    }
                }

                then("travelList should contain only the new plan") {
                    eventually(1.seconds) {
                        viewModel.uiState.value.travelList.first().id shouldBe samplePlan3.id
                    }
                }
            }
        }
    }
}
