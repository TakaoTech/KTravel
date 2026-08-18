package com.takaotech.ktravel.presentation.planning.transport.options

import com.slack.circuit.test.presenterTestOf
import com.takaotech.ktravel.di.PlanningGraph
import com.takaotech.ktravel.di.PlanningGraphStore
import com.takaotech.ktravel.domain.routing.RouteFeature
import com.takaotech.ktravel.domain.routing.RouteSelection
import com.takaotech.ktravel.domain.routing.RoutingMode
import com.takaotech.ktravel.domain.routing.RoutingOptionsSpec
import com.takaotech.ktravel.domain.routing.RoutingProfileId
import com.takaotech.ktravel.presentation.planning.transport.RouteOptionsDraft
import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.matcher.any
import dev.mokkery.mock
import io.kotest.assertions.nondeterministic.eventually
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import kotlin.time.Duration.Companion.seconds

private const val TRAVEL_ID = "TRAVEL_ID"

private val ROAD_PROFILE = RoutingProfileId(provider = "here", profile = "routing")
private val TRANSIT_PROFILE = RoutingProfileId(provider = "here", profile = "transit")

private val CAR = RoutingMode("CAR")
private val SCOOTER = RoutingMode("SCOOTER")
private val PEDESTRIAN = RoutingMode("PEDESTRIAN")
private val SUBWAY = RoutingMode("SUBWAY")

private val ROAD_SPEC = RoutingOptionsSpec.RoadSingleMode(
    modes = listOf(CAR, SCOOTER, PEDESTRIAN),
    maxAlternatives = 6,
    modesSupportingShortest = setOf(CAR),
    supportsTolls = true,
)

private val TRANSIT_SPEC = RoutingOptionsSpec.TransitFilter(
    modes = listOf(SUBWAY, RoutingMode("BUS")),
    maxAlternatives = 5,
)

class RouteOptionsPresenterTest :
    BehaviorSpec({

        fun storeFor(draft: RouteOptionsDraft): PlanningGraphStore {
            val graph = mock<PlanningGraph>()
            every { graph.routeOptionsDraft } returns draft
            val factory = mock<PlanningGraph.Factory>()
            every { factory.create(any()) } returns graph
            return PlanningGraphStore(factory)
        }

        given("a road profile the traveller has not configured yet") {
            val screen = RoadRouteOptionsScreen.of(TRAVEL_ID, ROAD_PROFILE, ROAD_SPEC)

            `when`("the options block is composed") {
                then("it seeds the draft with the first vehicle, so Calculate lights up untouched") {
                    val draft = RouteOptionsDraft()

                    presenterTestOf({ RoadRouteOptionsPresenter(screen, storeFor(draft)) }) {
                        val state = awaitItem()

                        state.selectedMode shouldBe CAR
                        state.alternatives shouldBe 1
                        state.maxAlternatives shouldBe 6

                        eventually(2.seconds) {
                            val seeded = draft.selection.value.shouldBeInstanceOf<RouteSelection.Road>()
                            seeded.profileId shouldBe ROAD_PROFILE
                            seeded.mode shouldBe CAR
                        }

                        cancelAndIgnoreRemainingEvents()
                    }
                }

                then("the chosen vehicle brings its own extras block") {
                    val draft = RouteOptionsDraft()

                    presenterTestOf({ RoadRouteOptionsPresenter(screen, storeFor(draft)) }) {
                        awaitItem().modeExtrasScreen.shouldNotBeNull()

                        cancelAndIgnoreRemainingEvents()
                    }
                }
            }

            `when`("a vehicle with nothing to configure is chosen") {
                then("the extras block disappears instead of showing controls that do not apply") {
                    val draft = RouteOptionsDraft()

                    presenterTestOf({ RoadRouteOptionsPresenter(screen, storeFor(draft)) }) {
                        awaitItem().eventSink(RoadRouteOptionsEvent.SelectMode(PEDESTRIAN))

                        eventually(2.seconds) {
                            draft.selection.value.shouldBeInstanceOf<RouteSelection.Road>().mode shouldBe PEDESTRIAN
                        }

                        var state = awaitItem()
                        while (state.selectedMode != PEDESTRIAN) state = awaitItem()
                        state.modeExtrasScreen.shouldBeNull()

                        cancelAndIgnoreRemainingEvents()
                    }
                }
            }
        }

        given("a road profile configured on a vehicle that has extras") {
            val extrasScreen = RoadModeExtrasScreen.of(
                travelId = TRAVEL_ID,
                profileId = ROAD_PROFILE,
                mode = CAR,
                avoidable = setOf(RouteFeature.TOLL_ROAD, RouteFeature.FERRY),
                tollsUnsupported = false,
                supportsShortest = true,
            )

            `when`("a feature is ticked in the extras block") {
                then("it reaches the request the screen will send") {
                    val draft = RouteOptionsDraft().apply { update(RouteSelection.Road(ROAD_PROFILE, CAR)) }

                    presenterTestOf({ RoadModeExtrasPresenter(extrasScreen, storeFor(draft)) }) {
                        awaitItem().eventSink(RoadModeExtrasEvent.ToggleAvoid(RouteFeature.FERRY))

                        eventually(2.seconds) {
                            draft.selection.value
                                .shouldBeInstanceOf<RouteSelection.Road>()
                                .avoid shouldBe setOf(RouteFeature.FERRY)
                        }

                        cancelAndIgnoreRemainingEvents()
                    }
                }
            }

            `when`("the draft holds a request for another profile") {
                then("nothing is written, so a stale block cannot corrupt the current one") {
                    val draft = RouteOptionsDraft().apply { update(RouteSelection.Transit(TRANSIT_PROFILE)) }

                    presenterTestOf({ RoadModeExtrasPresenter(extrasScreen, storeFor(draft)) }) {
                        val state = awaitItem()
                        state.avoided shouldBe emptySet()

                        state.eventSink(RoadModeExtrasEvent.ToggleAvoid(RouteFeature.FERRY))

                        draft.selection.value.shouldBeInstanceOf<RouteSelection.Transit>()

                        cancelAndIgnoreRemainingEvents()
                    }
                }
            }
        }

        given("a transit profile the traveller has not configured yet") {
            val screen = TransitRouteOptionsScreen.of(TRAVEL_ID, TRANSIT_PROFILE, TRANSIT_SPEC)

            `when`("the options block is composed") {
                then("the filter starts empty and no journey limit is imposed") {
                    val draft = RouteOptionsDraft()

                    presenterTestOf({ TransitRouteOptionsPresenter(screen, storeFor(draft)) }) {
                        val state = awaitItem()

                        state.modeFilter shouldBe emptySet()
                        state.maxChanges.shouldBeNull()
                        state.maxWalkingDistanceMeters.shouldBeNull()
                        state.walkingPace shouldBe WalkingPace.NORMAL

                        eventually(2.seconds) {
                            draft.selection.value.shouldBeInstanceOf<RouteSelection.Transit>()
                        }

                        cancelAndIgnoreRemainingEvents()
                    }
                }
            }

            `when`("a vehicle is ticked in the filter") {
                then("it reaches the request the screen will send") {
                    val draft = RouteOptionsDraft()

                    presenterTestOf({ TransitRouteOptionsPresenter(screen, storeFor(draft)) }) {
                        awaitItem().eventSink(TransitRouteOptionsEvent.ToggleMode(SUBWAY))

                        eventually(2.seconds) {
                            draft.selection.value
                                .shouldBeInstanceOf<RouteSelection.Transit>()
                                .modeFilter shouldBe setOf(SUBWAY)
                        }

                        cancelAndIgnoreRemainingEvents()
                    }
                }
            }
        }
    })
