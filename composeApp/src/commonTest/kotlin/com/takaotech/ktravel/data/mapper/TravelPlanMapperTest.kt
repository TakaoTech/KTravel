@file:OptIn(ExperimentalTime::class)

package com.takaotech.ktravel.data.mapper

import com.takaotech.ktravel.data.entity.PlaceEntity
import com.takaotech.ktravel.data.entity.RouteActionEntity
import com.takaotech.ktravel.data.entity.RouteEntity
import com.takaotech.ktravel.data.entity.RouteSectionEntity
import com.takaotech.ktravel.data.entity.StepEntity
import com.takaotech.ktravel.data.entity.TransportAnswerEntity
import com.takaotech.ktravel.data.entity.TransportRequestEntity
import com.takaotech.ktravel.data.entity.TravelDayEntity
import com.takaotech.ktravel.data.entity.TravelPlanEntity
import com.takaotech.ktravel.data.entity.VisitScheduleEntity
import com.takaotech.ktravel.domain.model.PlaceDomain
import com.takaotech.ktravel.domain.model.StepDomain
import com.takaotech.ktravel.domain.model.TransportType
import com.takaotech.ktravel.domain.model.TravelDayDomain
import com.takaotech.ktravel.domain.model.TravelPlanDomain
import com.takaotech.ktravel.domain.model.VisitScheduleDomain
import com.takaotech.ktravel.domain.routing.RouteFeature
import com.takaotech.ktravel.domain.routing.RouteSelection
import com.takaotech.ktravel.domain.routing.RoutingMode
import com.takaotech.ktravel.domain.routing.RoutingProfileId
import com.takaotech.ktravel.domain.routing.model.RouteAction
import com.takaotech.ktravel.domain.routing.model.RouteLocation
import com.takaotech.ktravel.domain.routing.model.RouteSummary
import com.takaotech.ktravel.domain.routing.model.TransitAgency
import com.takaotech.ktravel.domain.routing.model.TransitJourney
import com.takaotech.ktravel.domain.routing.model.TransitLine
import com.takaotech.ktravel.domain.routing.model.TransitStep
import com.takaotech.ktravel.domain.routing.model.TransitStop
import com.takaotech.ktravel.domain.routing.model.TransitTime
import com.takaotech.ktravel.domain.routing.model.TransportAnswer
import com.takaotech.ktravel.domain.routing.model.WheelchairAccess
import com.takaotech.ktravel.testutil.roadAnswer
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.nacular.measured.units.Length
import io.nacular.measured.units.times
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.datetime.UtcOffset
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

class TravelPlanMapperTest :
    BehaviorSpec({

        given("a TravelPlanDomain domain model") {
            val place = PlaceDomain(
                id = "place-1",
                name = "Colosseo",
                lat = 41.89,
                lng = 12.49,
            )
            val day = TravelDayDomain(
                id = "day-1",
                date = LocalDate(2024, 6, 1),
                places = listOf(place),
            )
            val travelPlan = TravelPlanDomain(
                name = "Roma Trip",
                periodStart = LocalDate(2024, 6, 1),
                periodEnd = LocalDate(2024, 6, 30),
                days = listOf(day),
                places = emptyList(),
            )

            `when`("toEntity is called") {
                val entity = with(TravelPlanEntityMapper) { travelPlan.toEntity("plan-id") }

                then("entity id should match the provided id") {
                    entity.id shouldBe "plan-id"
                }
                then("entity name should match domain name") {
                    entity.name shouldBe "Roma Trip"
                }
                then("entity periodStart should match domain periodStart") {
                    entity.periodStart shouldBe LocalDate(2024, 6, 1)
                }
                then("entity days size should match domain days size") {
                    entity.days.size shouldBe 1
                }
                then("entity day id should match domain day id") {
                    entity.days[0].id shouldBe "day-1"
                }
                then("entity day dateEpochDays should match domain date") {
                    entity.days[0].date shouldBe LocalDate(2024, 6, 1)
                }
                then("entity place name should match domain place name") {
                    entity.days[0].places[0].name shouldBe "Colosseo"
                }
            }
        }

        given("a TravelPlanEntity") {
            val placeEntity = PlaceEntity(
                id = "place-1",
                name = "Colosseo",
                lat = 41.89,
                lng = 12.49,
            )
            val dayEntity = TravelDayEntity(
                id = "day-1",
                date = LocalDate(2024, 6, 1),
                steps = emptyList(),
                places = listOf(placeEntity),
            )
            val entity = TravelPlanEntity(
                id = "plan-id",
                name = "Roma Trip",
                periodStart = LocalDate(2024, 6, 1),
                periodEnd = LocalDate(2024, 6, 30),
                days = listOf(dayEntity),
                places = emptyList(),
            )

            `when`("toDomain is called") {
                val domain = with(TravelPlanEntityMapper) { entity.toDomain() }

                then("domain name should match entity name") {
                    domain.name shouldBe "Roma Trip"
                }
                then("domain periodStart should match entity periodStart") {
                    domain.periodStart shouldBe LocalDate(2024, 6, 1)
                }
                then("domain days size should match entity days size") {
                    domain.days.size shouldBe 1
                }
                then("domain day id should match entity day id") {
                    domain.days[0].id shouldBe "day-1"
                }
                then("domain day date should match entity dateEpochDays") {
                    domain.days[0].date shouldBe LocalDate(2024, 6, 1)
                }
                then("domain place name should match entity place name") {
                    domain.days[0].places[0].name shouldBe "Colosseo"
                }
            }
        }

        given("a StepDomain.Place with a VisitSchedule") {
            val step = StepDomain.Place(
                id = "step-2",
                name = "Pantheon",
                lat = 41.89,
                lng = 12.47,
                schedule = VisitScheduleDomain(
                    date = LocalDate(2024, 6, 2),
                    startTime = LocalTime(10, 30),
                    endTime = LocalTime(11, 15),
                ),
            )

            `when`("toEntity is called") {
                val entity = with(TravelPlanEntityMapper) { step.toEntity() } as StepEntity.Place

                then("entity schedule should not be null") {
                    entity.schedule shouldBe VisitScheduleEntity(
                        dateEpochDays = LocalDate(2024, 6, 2).toEpochDays().toInt(),
                        startTimeHour = 10,
                        startTimeMinute = 30,
                        endTimeHour = 11,
                        endTimeMinute = 15,
                    )
                }
            }

            `when`("toEntity and then toDomain is called") {
                val roundTripped =
                    with(TravelPlanEntityMapper) { step.toEntity().toDomain() } as StepDomain.Place

                then("round-tripped step name should match original") {
                    roundTripped.name shouldBe step.name
                }
                then("round-tripped schedule arrival time should match original") {
                    roundTripped.schedule?.startTime shouldBe LocalTime(10, 30)
                }
                then("round-tripped schedule departure time should match original") {
                    roundTripped.schedule?.endTime shouldBe LocalTime(11, 15)
                }
                then("round-tripped schedule date should match original") {
                    roundTripped.schedule?.date shouldBe LocalDate(2024, 6, 2)
                }
            }
        }

        given("a StepDomain.Place") {
            val step = StepDomain.Place(
                id = "step-1",
                name = "Colosseo",
                lat = 41.89,
                lng = 12.49,
            )

            `when`("toEntity is called") {
                val entity = with(TravelPlanEntityMapper) { step.toEntity() }

                then("entity should be StepEntity.Place") {
                    entity shouldBe StepEntity.Place(
                        id = "step-1",
                        name = "Colosseo",
                        lat = 41.89,
                        lng = 12.49,
                    )
                }
            }

            `when`("toEntity and then toDomain is called") {
                val roundTripped = with(TravelPlanEntityMapper) { step.toEntity().toDomain() }

                then("round-tripped step should match original") {
                    roundTripped shouldBe step
                }
            }
        }

        given("a StepDomain.Transport carrying a road route") {
            val answer = roadAnswer(
                duration = 30.minutes,
                metres = 5000.0,
                actions = listOf(
                    RouteAction(
                        action = "depart",
                        durationSeconds = 5.minutes,
                        distanceMeters = 1000.0 * Length.meters,
                        instruction = "Head north",
                        offset = 3,
                    ),
                ),
            )
            val step = StepDomain.Transport(id = "step-2", type = TransportType.TRAIN, answer = answer)

            `when`("toEntity is called") {
                val entity =
                    with(TravelPlanEntityMapper) { step.toEntity() } as? StepEntity.Transport

                then("entity should be StepEntity.Transport") {
                    (entity != null) shouldBe true
                }
                then("entity transportType should be TRAIN") {
                    entity!!.transportType shouldBe "TRAIN"
                }
                then("the answer should be filed as a road route") {
                    entity!!.answer.shouldBeInstanceOf<TransportAnswerEntity.Routing>()
                        .route.sections.size shouldBe 1
                }
                then("the flat legacy route should no longer be written") {
                    entity!!.route shouldBe null
                }
            }

            `when`("toEntity and then toDomain is called") {
                val roundTripped =
                    with(TravelPlanEntityMapper) {
                        step.toEntity().toDomain()
                    } as? StepDomain.Transport

                then("round-tripped step type should be TRAIN") {
                    roundTripped!!.type shouldBe TransportType.TRAIN
                }
                then("the answer should come back as the same road route") {
                    roundTripped!!.answer shouldBe answer
                }
                then("the manoeuvre offset should survive, so the map can still be pointed at it") {
                    val routing = roundTripped!!.answer.shouldBeInstanceOf<TransportAnswer.Routing>()
                    routing.route.sections[0].actions[0].offset shouldBe 3
                }
            }
        }

        given("a StepDomain.Transport carrying a journey on scheduled services") {
            val journey = TransitJourney(
                summary = RouteSummary(durationSeconds = 30.minutes, distance = 9000.0 * Length.meters),
                steps = listOf(
                    TransitStep.Walk(
                        summary = RouteSummary(8.minutes, 600.0 * Length.meters),
                        departure = TransitTime(Instant.parse("2026-05-30T09:12:00+02:00"), UtcOffset(hours = 2)),
                    ),
                    TransitStep.Ride(
                        summary = RouteSummary(22.minutes, 8400.0 * Length.meters),
                        line = TransitLine(
                            mode = "SUBWAY",
                            name = "M1",
                            shortName = "M1",
                            headsign = "Sesto",
                            color = "#D52B1E",
                            textColor = "#FFFFFF",
                            wheelchairAccessible = WheelchairAccess.YES,
                        ),
                        agency = TransitAgency(name = "ATM", id = "atm", website = "https://atm.it"),
                        boarding = TransitStop(
                            location = RouteLocation(45.46, 9.19),
                            name = "Duomo",
                            departure = TransitTime(
                                Instant.parse("2026-05-30T09:24:00+02:00"),
                                UtcOffset(hours = 2),
                            ),
                        ),
                        alighting = TransitStop(location = RouteLocation(45.53, 9.24), name = "Sesto FS"),
                        intermediateStops = listOf(
                            TransitStop(location = RouteLocation(45.48, 9.21), name = "Cadorna", offset = 12),
                        ),
                    ),
                ),
            )
            val step = StepDomain.Transport(
                id = "step-3",
                type = TransportType.TRAIN,
                answer = TransportAnswer.Transit(journey),
            )

            `when`("toEntity and then toDomain is called") {
                val roundTripped = with(TravelPlanEntityMapper) {
                    step.toEntity().toDomain()
                } as StepDomain.Transport

                then("the journey should come back whole") {
                    // Lines, operators, stop names and colours are the whole reason the two shapes
                    // are told apart: flattened, this assertion could never pass.
                    roundTripped.answer shouldBe TransportAnswer.Transit(journey)
                }
            }
        }

        given("a StepDomain.Transport carrying the request it was computed with") {
            val answer = roadAnswer()

            `when`("the request is a road one and it is round-tripped") {
                val request = RouteSelection.Routing(
                    profileId = RoutingProfileId(provider = "here", profile = "routing"),
                    mode = RoutingMode("CAR"),
                    alternatives = 3,
                    avoid = setOf(RouteFeature.TOLL_ROAD, RouteFeature.FERRY),
                    shortestDistance = true,
                )
                val step = StepDomain.Transport(
                    id = "step-3",
                    type = TransportType.CAR,
                    answer = answer,
                    request = request,
                )
                val roundTripped = with(TravelPlanEntityMapper) {
                    step.toEntity().toDomain()
                } as? StepDomain.Transport

                then("the request should come back unchanged") {
                    roundTripped!!.request shouldBe request
                }
            }

            `when`("the request is a transit one and it is round-tripped") {
                val request = RouteSelection.Transit(
                    profileId = RoutingProfileId(provider = "here", profile = "transit"),
                    modeFilter = setOf(RoutingMode("REGIONAL_TRAIN"), RoutingMode("BUS")),
                    alternatives = 2,
                    maxChanges = 1,
                    pedestrianSpeedMetersPerSecond = 0.8,
                    pedestrianMaxDistanceMeters = 1500,
                )
                val step = StepDomain.Transport(
                    id = "step-4",
                    type = TransportType.TRAIN,
                    answer = answer,
                    request = request,
                )
                val roundTripped = with(TravelPlanEntityMapper) {
                    step.toEntity().toDomain()
                } as? StepDomain.Transport

                then("the request should come back unchanged") {
                    roundTripped!!.request shouldBe request
                }
            }

            `when`("the step carries no request") {
                val step = StepDomain.Transport(id = "step-5", type = TransportType.BUS, answer = answer)
                val entity = with(TravelPlanEntityMapper) { step.toEntity() } as StepEntity.Transport

                then("the entity should carry none either") {
                    entity.request shouldBe null
                }
                then("the round-tripped step should carry none either") {
                    val roundTripped = with(TravelPlanEntityMapper) { entity.toDomain() } as StepDomain.Transport
                    roundTripped.request shouldBe null
                }
            }
        }

        given("a stored request naming a feature this build has never heard of") {
            val entity = TransportRequestEntity.Routing(
                provider = "here",
                profile = "routing",
                mode = "CAR",
                avoid = listOf("TOLL_ROAD", "HOVERCRAFT_LANE"),
            )

            `when`("toDomain is called") {
                val domain = with(TravelPlanEntityMapper) { entity.toDomain() } as RouteSelection.Routing

                then("the unknown feature should be dropped rather than failing the plan") {
                    domain.avoid shouldBe setOf(RouteFeature.TOLL_ROAD)
                }
            }
        }

        given("a transport saved by a build that flattened every answer into one shape") {
            fun legacy(vararg sections: RouteSectionEntity) = StepEntity.Transport(
                id = "step-legacy",
                transportType = "CAR",
                route = RouteEntity(sections = sections.toList()),
            )

            `when`("the flat route carries manoeuvres") {
                val entity = legacy(
                    RouteSectionEntity(
                        durationSeconds = 1800L,
                        distanceMeters = 5000.0,
                        transportMode = "car",
                        actions = listOf(
                            RouteActionEntity(
                                action = "depart",
                                durationSeconds = 300L,
                                distanceMeters = 1000.0,
                                instruction = "Head north",
                            ),
                        ),
                    ),
                )

                then("it should read back as a road route rather than failing") {
                    val step = with(TravelPlanEntityMapper) { entity.toDomain() } as StepDomain.Transport
                    val routing = step.answer.shouldBeInstanceOf<TransportAnswer.Routing>()

                    routing.route.sections.size shouldBe 1
                    routing.route.sections[0].mode shouldBe "car"
                    routing.route.sections[0].actions.size shouldBe 1
                    routing.route.summary.durationSeconds shouldBe 1800.seconds
                }
            }

            `when`("the flat route carries no manoeuvres") {
                val entity = legacy(
                    RouteSectionEntity(durationSeconds = 300L, distanceMeters = 200.0, transportMode = "PEDESTRIAN"),
                    RouteSectionEntity(durationSeconds = 1500L, distanceMeters = 4800.0, transportMode = "SUBWAY"),
                )

                then("it should read back as the poor journey that shape kept") {
                    val step = with(TravelPlanEntityMapper) { entity.toDomain() } as StepDomain.Transport
                    val transit = step.answer.shouldBeInstanceOf<TransportAnswer.Transit>()

                    transit.journey.steps[0].shouldBeInstanceOf<TransitStep.Walk>()
                    val ride = transit.journey.steps[1].shouldBeInstanceOf<TransitStep.Ride>()
                    ride.line.mode shouldBe "SUBWAY"
                    // Never written by those builds, so there is nothing to read back.
                    ride.line.name shouldBe null
                    ride.agency shouldBe null
                }
            }

            `when`("it is saved again") {
                val entity = legacy(RouteSectionEntity(durationSeconds = 300L, distanceMeters = 200.0))

                then("it should be re-filed under the new shape and drop the flat one") {
                    val step = with(TravelPlanEntityMapper) { entity.toDomain() } as StepDomain.Transport
                    val resaved = with(TravelPlanEntityMapper) { step.toEntity() } as StepEntity.Transport

                    resaved.answer shouldNotBe null
                    resaved.route shouldBe null
                }
            }
        }
    })
