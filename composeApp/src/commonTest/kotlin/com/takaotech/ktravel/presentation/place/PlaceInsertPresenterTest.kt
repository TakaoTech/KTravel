package com.takaotech.ktravel.presentation.place

import com.slack.circuit.test.CircuitReceiveTurbine
import com.slack.circuit.test.FakeNavigator
import com.slack.circuit.test.presenterTestOf
import com.takaotech.ktravel.di.PlanningGraph
import com.takaotech.ktravel.di.PlanningGraphStore
import com.takaotech.ktravel.domain.model.PlaceDomain
import com.takaotech.ktravel.domain.model.TravelDayDomain
import com.takaotech.ktravel.domain.model.TravelPlanDomain
import com.takaotech.ktravel.domain.repository.TravelPlanRepository
import com.takaotech.ktravel.domain.routing.ProfileAvailability
import com.takaotech.ktravel.domain.search.NearbyPlacesService
import com.takaotech.ktravel.domain.search.PlaceSearchCatalog
import com.takaotech.ktravel.domain.search.PlaceSearchFailure
import com.takaotech.ktravel.domain.search.PlaceSearchProvider
import com.takaotech.ktravel.domain.search.PlaceSearchProviderOption
import com.takaotech.ktravel.domain.search.PlaceSearchQuery
import com.takaotech.ktravel.domain.search.PlaceSearchService
import com.takaotech.ktravel.domain.search.model.GeoArea
import com.takaotech.ktravel.domain.search.model.GeoCoordinate
import com.takaotech.ktravel.domain.search.model.PlaceCandidate
import com.takaotech.ktravel.domain.search.model.PlaceCandidateSource
import com.takaotech.ktravel.domain.search.model.PlaceCategory
import com.takaotech.ktravel.domain.usecase.SavePlaceUseCase
import com.takaotech.ktravel.presentation.plan.day.AddPlaceScreen
import dev.mokkery.MockMode
import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.matcher.any
import dev.mokkery.mock
import dev.mokkery.verify.VerifyMode
import dev.mokkery.verifySuspend
import io.kotest.assertions.nondeterministic.eventually
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.datetime.LocalDate
import kotlin.time.Duration.Companion.seconds

private const val TRAVEL_ID = "TRAVEL_ID"
private const val DAY_ID = "DAY_ID"

private val HERE = PlaceSearchProvider(id = "here", name = "HERE")
private val AREA = GeoArea(west = 9.0, south = 45.0, east = 10.0, north = 46.0)

private fun candidate(index: Int) = PlaceCandidate(
    id = "SEARCH:$index",
    title = "Place $index",
    coordinate = GeoCoordinate(lat = 45.0 + index / 100.0, lng = 9.0),
    source = PlaceCandidateSource.SEARCH,
)

private class FakePlaceSearchService(
    private val catalog: PlaceSearchCatalog = PlaceSearchCatalog(
        listOf(PlaceSearchProviderOption(HERE, ProfileAvailability.Available)),
    ),
    private val answer: (PlaceSearchQuery) -> List<PlaceCandidate> = { emptyList() },
) : PlaceSearchService {
    val queries = mutableListOf<PlaceSearchQuery>()

    override suspend fun catalog(): PlaceSearchCatalog = catalog

    override suspend fun autocomplete(provider: PlaceSearchProvider, query: PlaceSearchQuery): List<PlaceCandidate> {
        queries += query
        return answer(query)
    }
}

private class FakeNearbyPlacesService : NearbyPlacesService {
    val requests = mutableListOf<Pair<GeoArea, PlaceCategory?>>()

    override suspend fun placesIn(area: GeoArea, category: PlaceCategory?): List<PlaceCandidate> {
        requests += area to category
        return emptyList()
    }
}

private class Setup(
    val search: FakePlaceSearchService = FakePlaceSearchService(),
    val nearby: FakeNearbyPlacesService = FakeNearbyPlacesService(),
    plan: TravelPlanDomain = TravelPlanDomain(),
) {
    val screen = AddPlaceScreen(TRAVEL_ID, DAY_ID)
    val navigator = FakeNavigator(screen)
    val savePlaceUseCase = mock<SavePlaceUseCase>(MockMode.autoUnit)

    private val repository = mock<TravelPlanRepository> {
        every { planningState } returns MutableStateFlow(plan) as StateFlow<TravelPlanDomain>
    }

    val store: PlanningGraphStore = run {
        val graph = mock<PlanningGraph> {
            every { travelPlanRepository } returns repository
            every { placeSearchService } returns search
            every { nearbyPlacesService } returns nearby
            every { savePlaceUseCase } returns this@Setup.savePlaceUseCase
        }
        val factory = mock<PlanningGraph.Factory> { every { create(any()) } returns graph }
        PlanningGraphStore(factory)
    }
}

private suspend fun CircuitReceiveTurbine<PlaceInsertUiState>.awaitUntil(
    predicate: (PlaceInsertUiState) -> Boolean,
): PlaceInsertUiState {
    var state = awaitItem()
    while (!predicate(state)) state = awaitItem()
    return state
}

private suspend fun CircuitReceiveTurbine<PlaceInsertUiState>.awaitReady(): PlaceInsertUiState =
    awaitUntil { state -> state.selectedProvider != null }

class PlaceInsertPresenterTest : BehaviorSpec() {
    init {
        given("a catalog where HERE is available") {
            `when`("the presenter starts") {
                then("HERE is the selected provider and the framed area is on show") {
                    val setup = Setup()
                    presenterTestOf({ PlaceInsertPresenter(setup.screen, setup.navigator, setup.store) }) {
                        val state = awaitReady()

                        state.selectedProvider shouldBe HERE
                        state.providers shouldHaveSize 1
                        state.listContent shouldBe PlaceListContent.NEARBY
                        state.canConfirm shouldBe false
                    }
                }
            }

            `when`("the traveller types three characters in a row") {
                then("only the last text is searched, once typing pauses") {
                    val search = FakePlaceSearchService(
                        answer = { List(8) { index -> candidate(index) } },
                    )
                    val setup = Setup(search = search)
                    presenterTestOf({ PlaceInsertPresenter(setup.screen, setup.navigator, setup.store) }) {
                        val ready = awaitReady()
                        ready.eventSink(PlaceInsertEvent.QueryChanged("t"))
                        ready.eventSink(PlaceInsertEvent.QueryChanged("to"))
                        ready.eventSink(PlaceInsertEvent.QueryChanged("tor"))

                        val state = awaitUntil { it.search.places.isNotEmpty() }

                        setup.search.queries.map { query -> query.text } shouldBe listOf("tor")
                        state.listContent shouldBe PlaceListContent.SEARCH_RESULTS
                        state.search.places shouldHaveSize 8
                        state.suggestions shouldHaveSize SUGGESTION_COUNT
                        cancelAndIgnoreRemainingEvents()
                    }
                }
            }

            `when`("the query is cleared after a search") {
                then("the framed area comes back in the list") {
                    val setup = Setup(search = FakePlaceSearchService(answer = { listOf(candidate(1)) }))
                    presenterTestOf({ PlaceInsertPresenter(setup.screen, setup.navigator, setup.store) }) {
                        awaitReady().eventSink(PlaceInsertEvent.QueryChanged("duomo"))
                        val searched = awaitUntil { it.search.places.isNotEmpty() }

                        searched.eventSink(PlaceInsertEvent.QueryCleared)

                        val state = awaitUntil { it.query.isEmpty() && it.search.places.isEmpty() }
                        state.listContent shouldBe PlaceListContent.NEARBY
                        cancelAndIgnoreRemainingEvents()
                    }
                }
            }

            `when`("the traveller types coordinates and picks them") {
                then("no search is sent, the point is selected, the query is emptied and the map moves") {
                    val setup = Setup()
                    presenterTestOf({ PlaceInsertPresenter(setup.screen, setup.navigator, setup.store) }) {
                        awaitReady().eventSink(PlaceInsertEvent.QueryChanged("45.1, 9.2"))
                        val typed = awaitUntil { it.coordinateCandidate != null }
                        val point = typed.coordinateCandidate!!

                        typed.eventSink(PlaceInsertEvent.SuggestionChosen(point))

                        val state = awaitUntil { it.selected.isNotEmpty() && it.query.isEmpty() }
                        state.selected shouldBe listOf(point)
                        state.cameraRequest?.target shouldBe GeoCoordinate(45.1, 9.2)
                        setup.search.queries.shouldBeEmpty()
                        cancelAndIgnoreRemainingEvents()
                    }
                }
            }

            `when`("a place is toggled twice and the inventory is opened") {
                then("it is added, then removed, and the inventory takes the list area") {
                    val setup = Setup()
                    val place = candidate(1)
                    presenterTestOf({ PlaceInsertPresenter(setup.screen, setup.navigator, setup.store) }) {
                        awaitReady().eventSink(PlaceInsertEvent.SelectionToggled(place))

                        val added = awaitUntil { it.selected.isNotEmpty() }
                        added.isSelected(place.id) shouldBe true
                        added.canConfirm shouldBe true

                        added.eventSink(PlaceInsertEvent.SelectionToggled(place))
                        awaitUntil { it.selected.isEmpty() }.eventSink(PlaceInsertEvent.InventoryToggled)

                        awaitUntil { it.isInventoryOpen }.listContent shouldBe PlaceListContent.INVENTORY
                        cancelAndIgnoreRemainingEvents()
                    }
                }
            }

            `when`("two places are selected and the traveller confirms") {
                then("both are saved to the day's backlog in order and the screen closes") {
                    val setup = Setup()
                    val first = candidate(1)
                    val second = candidate(2)
                    presenterTestOf({ PlaceInsertPresenter(setup.screen, setup.navigator, setup.store) }) {
                        awaitReady().eventSink(PlaceInsertEvent.SelectionToggled(first))
                        awaitUntil { it.selected.size == 1 }.eventSink(PlaceInsertEvent.SelectionToggled(second))
                        awaitUntil { it.selected.size == 2 }.eventSink(PlaceInsertEvent.Confirm)

                        setup.navigator.awaitPop()
                        verifySuspend(VerifyMode.exactly(1)) {
                            setup.savePlaceUseCase.invoke(listOf(first, second), DAY_ID)
                        }
                        cancelAndIgnoreRemainingEvents()
                    }
                }
            }

            `when`("the traveller confirms with nothing selected") {
                then("nothing is saved") {
                    val setup = Setup()
                    presenterTestOf({ PlaceInsertPresenter(setup.screen, setup.navigator, setup.store) }) {
                        awaitReady().eventSink(PlaceInsertEvent.Confirm)

                        verifySuspend(VerifyMode.not) { setup.savePlaceUseCase.invoke(any(), any()) }
                        cancelAndIgnoreRemainingEvents()
                    }
                }
            }

            `when`("the traveller exits") {
                then("the screen closes") {
                    val setup = Setup()
                    presenterTestOf({ PlaceInsertPresenter(setup.screen, setup.navigator, setup.store) }) {
                        awaitReady().eventSink(PlaceInsertEvent.Exit)

                        setup.navigator.awaitPop()
                        cancelAndIgnoreRemainingEvents()
                    }
                }
            }

            `when`("the provider refuses the key") {
                then("the results say the API key is missing") {
                    val setup = Setup(
                        search = FakePlaceSearchService(
                            answer = { throw PlaceSearchFailure.ProviderCredentials("no") },
                        ),
                    )
                    presenterTestOf({ PlaceInsertPresenter(setup.screen, setup.navigator, setup.store) }) {
                        awaitReady().eventSink(PlaceInsertEvent.QueryChanged("duomo"))

                        val state = awaitUntil { it.search.problem != null }
                        state.search.problem shouldBe PlaceSearchProblem.MISSING_API_KEY
                        cancelAndIgnoreRemainingEvents()
                    }
                }
            }
        }

        given("a catalog where HERE needs a key the trip does not have") {
            `when`("the traveller types a query") {
                then("no search is sent and the results say the key is missing") {
                    val search = FakePlaceSearchService(
                        catalog = PlaceSearchCatalog(
                            listOf(PlaceSearchProviderOption(HERE, ProfileAvailability.MissingApiKey)),
                        ),
                    )
                    val setup = Setup(search = search)
                    presenterTestOf({ PlaceInsertPresenter(setup.screen, setup.navigator, setup.store) }) {
                        awaitReady().eventSink(PlaceInsertEvent.QueryChanged("duomo"))

                        val state = awaitUntil { it.search.problem != null }
                        state.search.problem shouldBe PlaceSearchProblem.MISSING_API_KEY
                        search.queries.shouldBeEmpty()
                        cancelAndIgnoreRemainingEvents()
                    }
                }
            }
        }

        given("the map settling on an area") {
            `when`("a category is then picked") {
                then("the framed area is asked for with the area and the category") {
                    val setup = Setup()
                    presenterTestOf({ PlaceInsertPresenter(setup.screen, setup.navigator, setup.store) }) {
                        val ready = awaitReady()
                        ready.eventSink(PlaceInsertEvent.ViewportChanged(AREA, AREA.center))
                        ready.eventSink(PlaceInsertEvent.CategorySelected(PlaceCategory.NATURE))

                        val state = awaitUntil { it.category == PlaceCategory.NATURE }
                        eventually(2.seconds) {
                            setup.nearby.requests shouldBe listOf(AREA to PlaceCategory.NATURE)
                        }
                        state.nearby.places.shouldBeEmpty()
                        cancelAndIgnoreRemainingEvents()
                    }
                }
            }
        }

        given("a trip whose day already has a place") {
            `when`("the presenter starts") {
                then("the map opens on that place") {
                    val plan = TravelPlanDomain(
                        days = listOf(
                            TravelDayDomain(
                                id = DAY_ID,
                                date = LocalDate(2026, 5, 18),
                                places = listOf(PlaceDomain(name = "Binasco", lat = 45.33, lng = 9.10)),
                            ),
                        ),
                    )
                    val setup = Setup(plan = plan)
                    presenterTestOf({ PlaceInsertPresenter(setup.screen, setup.navigator, setup.store) }) {
                        val state = awaitItem()

                        state.initialCamera.target shouldBe GeoCoordinate(45.33, 9.10)
                        state.initialCamera shouldNotBe MapCamera.DEFAULT
                        cancelAndIgnoreRemainingEvents()
                    }
                }
            }
        }
    }
}
