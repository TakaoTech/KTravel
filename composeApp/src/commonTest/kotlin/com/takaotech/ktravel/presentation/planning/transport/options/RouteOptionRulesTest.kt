package com.takaotech.ktravel.presentation.planning.transport.options

import com.takaotech.ktravel.domain.routing.RouteFeature
import com.takaotech.ktravel.domain.routing.RouteSelection
import com.takaotech.ktravel.domain.routing.RoutingMode
import com.takaotech.ktravel.domain.routing.RoutingOptionsSpec
import com.takaotech.ktravel.domain.routing.RoutingProfileId
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe

private val PROFILE = RoutingProfileId(provider = "here", profile = "routing")
private val TRANSIT_PROFILE = RoutingProfileId(provider = "here", profile = "transit")

private val CAR = RoutingMode("CAR")
private val SCOOTER = RoutingMode("SCOOTER")
private val PEDESTRIAN = RoutingMode("PEDESTRIAN")

private val ROUTING_SPEC = RoutingOptionsSpec.RoutingSingleMode(
    modes = listOf(CAR, SCOOTER, PEDESTRIAN),
    maxAlternatives = 6,
    modesSupportingShortest = setOf(CAR),
    supportsTolls = true,
)

private val TRANSIT_SPEC = RoutingOptionsSpec.TransitFilter(
    modes = listOf(RoutingMode("SUBWAY"), RoutingMode("BUS")),
    maxAlternatives = 5,
)

class RouteOptionRulesTest :
    BehaviorSpec({

        given("a road profile whose vehicles do not all take the same options") {

            `when`("the extras of a motorised vehicle are resolved") {
                then("it is offered everything a road vehicle can avoid, and distance optimization") {
                    val screen = routingModeExtrasScreen("t", PROFILE, CAR, ROUTING_SPEC)

                    screen.shouldNotBeNull()
                    screen as RoutingModeExtrasScreen
                    screen.avoidable shouldBe RouteFeature.entries.toSet()
                    screen.supportsShortest shouldBe true
                    screen.tollsUnsupported shouldBe false
                }
            }

            `when`("the extras of a scooter are resolved") {
                then("tolls and motorways are absent, because a scooter is never asked about them") {
                    val screen = routingModeExtrasScreen("t", PROFILE, SCOOTER, ROUTING_SPEC) as RoutingModeExtrasScreen

                    screen.avoidable shouldBe setOf(RouteFeature.FERRY, RouteFeature.TUNNEL, RouteFeature.DIRT_ROAD)
                    // Refused by upstream on this vehicle, so the row is shown disabled rather than hidden.
                    screen.supportsShortest shouldBe false
                }
            }

            `when`("the extras of a pedestrian are resolved") {
                then("there is no extras block at all") {
                    routingModeExtrasScreen("t", PROFILE, PEDESTRIAN, ROUTING_SPEC).shouldBeNull()
                }
            }

            `when`("the profile declares it cannot handle tolls") {
                then("the toll chip is dropped and the reason is flagged so the screen can say it") {
                    val spec = ROUTING_SPEC.copy(supportsTolls = false)

                    val screen = routingModeExtrasScreen("t", PROFILE, CAR, spec) as RoutingModeExtrasScreen

                    screen.avoidable.contains(RouteFeature.TOLL_ROAD) shouldBe false
                    screen.tollsUnsupported shouldBe true
                }
            }
        }

        given("a road request with options that belong to the vehicle chosen") {

            `when`("the vehicle changes to one that cannot be optimized for distance") {
                then("the distance option is switched off rather than sent and refused") {
                    val selection = RouteSelection.Routing(PROFILE, CAR, shortestDistance = true)

                    val updated = selection.reduce(RoutingRouteOptionsEvent.SelectMode(SCOOTER), ROUTING_SPEC)

                    updated.mode shouldBe SCOOTER
                    updated.shortestDistance shouldBe false
                }
            }

            `when`("the vehicle changes to one that is never asked about what was avoided") {
                then("only the features the new vehicle understands are kept") {
                    val selection = RouteSelection.Routing(
                        profileId = PROFILE,
                        mode = CAR,
                        avoid = setOf(RouteFeature.TOLL_ROAD, RouteFeature.FERRY),
                    )

                    val updated = selection.reduce(RoutingRouteOptionsEvent.SelectMode(SCOOTER), ROUTING_SPEC)

                    updated.avoid shouldBe setOf(RouteFeature.FERRY)
                }
            }

            `when`("more alternatives are asked for than the profile accepts") {
                then("the count is clamped to what the catalog declares") {
                    val selection = RouteSelection.Routing(PROFILE, CAR)

                    selection.reduce(RoutingRouteOptionsEvent.SetAlternatives(99), ROUTING_SPEC).alternatives shouldBe 6
                    selection.reduce(RoutingRouteOptionsEvent.SetAlternatives(0), ROUTING_SPEC).alternatives shouldBe 1
                }
            }

            `when`("a feature already avoided is toggled") {
                then("it is removed, so the same control both adds and removes") {
                    val selection = RouteSelection.Routing(PROFILE, CAR, avoid = setOf(RouteFeature.FERRY))

                    val updated = selection.reduce(RoutingModeExtrasEvent.ToggleAvoid(RouteFeature.FERRY))

                    updated.avoid shouldBe emptySet()
                }
            }
        }

        given("a transit request") {

            `when`("the last accepted vehicle is unticked") {
                then("the filter is left empty, which means no restriction rather than nothing allowed") {
                    val subway = RoutingMode("SUBWAY")
                    val selection = RouteSelection.Transit(TRANSIT_PROFILE, modeFilter = setOf(subway))

                    val updated = selection.reduce(TransitRouteOptionsEvent.ToggleMode(subway), TRANSIT_SPEC)

                    updated.modeFilter shouldBe emptySet()
                }
            }

            `when`("transfers and walking distance are set outside the range the API accepts") {
                then("they are clamped to it") {
                    val selection = RouteSelection.Transit(TRANSIT_PROFILE)

                    selection.reduce(TransitRouteOptionsEvent.SetMaxChanges(99), TRANSIT_SPEC)
                        .maxChanges shouldBe MAX_CHANGES_LIMIT
                    selection.reduce(TransitRouteOptionsEvent.SetMaxWalkingDistance(99_999), TRANSIT_SPEC)
                        .pedestrianMaxDistanceMeters shouldBe WALK_DISTANCE_MAX
                }
            }

            `when`("a limit is lifted") {
                then("nothing is sent, so the provider's own default applies") {
                    val selection = RouteSelection.Transit(TRANSIT_PROFILE, maxChanges = 2)

                    selection.reduce(TransitRouteOptionsEvent.SetMaxChanges(null), TRANSIT_SPEC)
                        .maxChanges.shouldBeNull()
                }
            }

            `when`("the normal walking pace is chosen") {
                then("no speed is sent, and the pace reads back as normal") {
                    val selection = RouteSelection.Transit(TRANSIT_PROFILE, pedestrianSpeedMetersPerSecond = 1.5)

                    val updated = selection.reduce(
                        TransitRouteOptionsEvent.SetWalkingPace(WalkingPace.NORMAL),
                        TRANSIT_SPEC,
                    )

                    updated.pedestrianSpeedMetersPerSecond.shouldBeNull()
                    WalkingPace.of(updated.pedestrianSpeedMetersPerSecond) shouldBe WalkingPace.NORMAL
                }
            }
        }

        given("the registry that decides which options block a profile gets") {

            `when`("each family is asked for its screen") {
                then("the routing family and the transit family get their own, and a modeless profile none") {
                    val routing = RoutingProfileInfoFixtures.routing(ROUTING_SPEC).routeOptionsScreen("t")
                    val transit = RoutingProfileInfoFixtures.transit(TRANSIT_SPEC).routeOptionsScreen("t")
                    val none = RoutingProfileInfoFixtures.none().routeOptionsScreen("t")

                    listOf(routing is RoutingRouteOptionsScreen, transit is TransitRouteOptionsScreen, none == null)
                        .shouldContainExactly(true, true, true)
                }
            }

            `when`("a profile arrives from the catalog with no modes at all") {
                then("no options block is drawn, rather than one with nothing to choose from") {
                    val empty = RoutingOptionsSpec.RoutingSingleMode(modes = emptyList(), maxAlternatives = 1)

                    RoutingProfileInfoFixtures.routing(empty).routeOptionsScreen("t").shouldBeNull()
                }
            }
        }
    })
