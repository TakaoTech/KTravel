package com.takaotech.ktravel.presentation.intro

import com.takaotech.ktravel.di.PlanningGraph
import com.takaotech.ktravel.di.PlanningGraphStore
import com.takaotech.ktravel.domain.archive.TravelArchiveImporter
import com.takaotech.ktravel.domain.model.TravelPlanSummary
import com.takaotech.ktravel.domain.repository.TravelManagerRepository
import dev.mokkery.answering.calls
import dev.mokkery.answering.returns
import dev.mokkery.everySuspend
import dev.mokkery.matcher.any
import dev.mokkery.mock
import dev.mokkery.verify.VerifyMode.Companion.exactly
import dev.mokkery.verifySuspend
import io.kotest.assertions.nondeterministic.eventually
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import kotlinx.datetime.LocalDate
import kotlin.time.Duration.Companion.seconds

class TravelSelectionViewModelTest : BehaviorSpec() {
    init {
        coroutineTestScope = true
        coroutineDebugProbes = true

        val mockRepository: TravelManagerRepository = mock()
        val mockImporter: TravelArchiveImporter = mock()

        // Only `release` is ever reached from these tests, and that never touches the factory:
        // creating a real graph here would drag in the whole dependency graph for nothing.
        val planningGraphStore = PlanningGraphStore(
            PlanningGraph.Factory { error("PlanningGraph must not be created in these tests") },
        )

        val samplePlan1 = TravelPlanSummary(
            id = "id-1",
            name = "Viaggio a Tokyo",
            periodStart = LocalDate(2024, 3, 1),
            periodEnd = LocalDate(2024, 3, 15),
        )
        val samplePlan2 = TravelPlanSummary(
            id = "id-2",
            name = "Weekend a Roma",
            periodStart = LocalDate(2024, 6, 10),
            periodEnd = LocalDate(2024, 6, 12),
        )
        val samplePlan3 = TravelPlanSummary(
            id = "id-3",
            name = "Vacanza al mare",
            periodStart = LocalDate(2024, 8, 1),
            periodEnd = LocalDate(2024, 8, 14),
        )

        given("a TravelSelectionViewModel when repository returns an empty list") {
            everySuspend { mockRepository.getAllTravelPlans() } returns emptyList()
            val viewModel = TravelSelectionViewModel(mockRepository, mockImporter, planningGraphStore)
            viewModel.loadTravelPlans()

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
            val viewModel = TravelSelectionViewModel(mockRepository, mockImporter, planningGraphStore)
            viewModel.loadTravelPlans()

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
                samplePlan3,
            )
            val viewModel = TravelSelectionViewModel(mockRepository, mockImporter, planningGraphStore)
            viewModel.loadTravelPlans()

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
            val viewModel = TravelSelectionViewModel(mockRepository, mockImporter, planningGraphStore)
            viewModel.loadTravelPlans()

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
                samplePlan2,
            )
            val viewModel = TravelSelectionViewModel(mockRepository, mockImporter, planningGraphStore)
            viewModel.loadTravelPlans()

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

        given("a TravelSelectionViewModel not in selection mode") {
            val repository: TravelManagerRepository = mock()
            everySuspend { repository.getAllTravelPlans() } returns listOf(samplePlan1, samplePlan2)
            val viewModel = TravelSelectionViewModel(repository, mockImporter, planningGraphStore)
            viewModel.loadTravelPlans()

            `when`("enterSelectionMode is called on an item") {
                viewModel.enterSelectionMode(samplePlan1.id)

                then("selection mode should be active with that item selected") {
                    viewModel.uiState.value.isSelectionMode shouldBe true
                    viewModel.uiState.value.selectedIds shouldContainExactlyInAnyOrder setOf(
                        samplePlan1.id,
                    )
                }
            }

            `when`("toggleSelection is called on another item") {
                viewModel.toggleSelection(samplePlan2.id)

                then("both items should be selected") {
                    viewModel.uiState.value.selectedIds shouldContainExactlyInAnyOrder setOf(
                        samplePlan1.id,
                        samplePlan2.id,
                    )
                }
            }

            `when`("toggleSelection is called again on an already selected item") {
                viewModel.toggleSelection(samplePlan2.id)

                then("that item should be deselected while selection mode stays active") {
                    viewModel.uiState.value.selectedIds shouldContainExactlyInAnyOrder setOf(
                        samplePlan1.id,
                    )
                    viewModel.uiState.value.isSelectionMode shouldBe true
                }
            }

            `when`("the last selected item is deselected") {
                viewModel.toggleSelection(samplePlan1.id)

                then("selection mode should be left automatically") {
                    viewModel.uiState.value.isSelectionMode shouldBe false
                    viewModel.uiState.value.selectedIds.shouldBeEmpty()
                }
            }
        }

        given("a TravelSelectionViewModel in selection mode") {
            val repository: TravelManagerRepository = mock()
            everySuspend { repository.getAllTravelPlans() } returns listOf(samplePlan1, samplePlan2)
            val viewModel = TravelSelectionViewModel(repository, mockImporter, planningGraphStore)
            viewModel.loadTravelPlans()
            viewModel.enterSelectionMode(samplePlan1.id)

            `when`("exitSelectionMode is called") {
                viewModel.exitSelectionMode()

                then("selection mode should be inactive and the selection cleared") {
                    viewModel.uiState.value.isSelectionMode shouldBe false
                    viewModel.uiState.value.selectedIds.shouldBeEmpty()
                }
            }
        }

        given("a TravelSelectionViewModel with two loaded plans") {
            val repository: TravelManagerRepository = mock()
            everySuspend { repository.getAllTravelPlans() } returns listOf(samplePlan1, samplePlan2)
            everySuspend { repository.deleteTravelPlan(any()) } returns Unit
            val viewModel = TravelSelectionViewModel(repository, mockImporter, planningGraphStore)
            viewModel.loadTravelPlans()

            `when`("deleteTravels is called with a single id") {
                eventually(1.seconds) {
                    viewModel.uiState.value.travelList shouldHaveSize 2
                }

                everySuspend { repository.getAllTravelPlans() } returns listOf(samplePlan2)
                viewModel.deleteTravels(setOf(samplePlan1.id))

                then("the plan should be deleted from the repository") {
                    eventually(1.seconds) {
                        verifySuspend { repository.deleteTravelPlan(samplePlan1.id) }
                    }
                }

                then("the list should be reloaded without the deleted plan") {
                    eventually(1.seconds) {
                        viewModel.uiState.value.travelList shouldHaveSize 1
                        viewModel.uiState.value.travelList.first().id shouldBe samplePlan2.id
                    }
                }

                then("isLoading should be false and no error should be reported") {
                    eventually(1.seconds) {
                        viewModel.uiState.value.isLoading shouldBe false
                        viewModel.uiState.value.error shouldBe null
                    }
                }
            }
        }

        given("a TravelSelectionViewModel with three selected plans") {
            val repository: TravelManagerRepository = mock()
            everySuspend { repository.getAllTravelPlans() } returns listOf(
                samplePlan1,
                samplePlan2,
                samplePlan3,
            )
            everySuspend { repository.deleteTravelPlan(any()) } returns Unit
            val viewModel = TravelSelectionViewModel(repository, mockImporter, planningGraphStore)
            viewModel.loadTravelPlans()
            viewModel.enterSelectionMode(samplePlan1.id)
            viewModel.toggleSelection(samplePlan2.id)

            `when`("deleteTravels is called with the selected ids") {
                eventually(1.seconds) {
                    viewModel.uiState.value.travelList shouldHaveSize 3
                }

                everySuspend { repository.getAllTravelPlans() } returns listOf(samplePlan3)
                viewModel.deleteTravels(viewModel.uiState.value.selectedIds)

                then("every selected plan should be deleted") {
                    eventually(1.seconds) {
                        verifySuspend { repository.deleteTravelPlan(samplePlan1.id) }
                        verifySuspend { repository.deleteTravelPlan(samplePlan2.id) }
                    }
                }

                then("the list should be reloaded only once after the deletions") {
                    eventually(1.seconds) {
                        viewModel.uiState.value.travelList shouldHaveSize 1
                    }
                    // One call for the initial load, one for the refresh after the deletions.
                    verifySuspend(exactly(2)) { repository.getAllTravelPlans() }
                }

                then("selection mode should be left") {
                    eventually(1.seconds) {
                        viewModel.uiState.value.isSelectionMode shouldBe false
                        viewModel.uiState.value.selectedIds.shouldBeEmpty()
                    }
                }
            }
        }

        given("a TravelSelectionViewModel whose repository fails to delete") {
            val repository: TravelManagerRepository = mock()
            everySuspend { repository.getAllTravelPlans() } returns listOf(samplePlan1, samplePlan2)
            everySuspend { repository.deleteTravelPlan(any()) } calls { throw RuntimeException("Delete failed") }
            val viewModel = TravelSelectionViewModel(repository, mockImporter, planningGraphStore)
            viewModel.loadTravelPlans()

            `when`("deleteTravels is called") {
                eventually(1.seconds) {
                    viewModel.uiState.value.travelList shouldHaveSize 2
                }

                viewModel.deleteTravels(setOf(samplePlan1.id))

                then("error should be set to the exception message") {
                    eventually(1.seconds) {
                        viewModel.uiState.value.error shouldBe "Delete failed"
                    }
                }

                then("isLoading should be false after the failure") {
                    eventually(1.seconds) {
                        viewModel.uiState.value.isLoading shouldBe false
                    }
                }

                then("the list should stay unchanged") {
                    eventually(1.seconds) {
                        viewModel.uiState.value.travelList shouldHaveSize 2
                    }
                }
            }
        }
    }
}
