package com.takaotech.ktravel.data.mapper

import com.takaotech.ktravel.data.entity.*
import com.takaotech.ktravel.domain.model.*
import com.takaotech.ktravel.domain.routing.model.Route
import com.takaotech.ktravel.domain.routing.model.RouteAction
import com.takaotech.ktravel.domain.routing.model.RouteSection
import com.takaotech.ktravel.domain.routing.model.RouteSummary
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.nacular.measured.units.Length
import io.nacular.measured.units.times
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

class TravelPlanMapperTest : BehaviorSpec({

    given("a TravelPlan domain model") {
        val place = PlaceDomain(
            id = "place-1",
            name = "Colosseo",
            lat = 41.89,
            lng = 12.49
        )
        val day = TravelDayDomain(
            id = "day-1",
            date = LocalDate(2024, 6, 1),
            places = listOf(place)
        )
        val travelPlan = TravelPlan(
            name = "Roma Trip",
            periodStart = LocalDate(2024, 6, 1),
            periodEnd = LocalDate(2024, 6, 30),
            days = listOf(day),
            places = emptyList()
        )

        `when`("toEntity is called") {
            val entity = with(TravelPlanMapper) { travelPlan.toEntity("plan-id") }

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
            lng = 12.49
        )
        val dayEntity = TravelDayEntity(
            id = "day-1",
            date = LocalDate(2024, 6, 1),
            steps = emptyList(),
            places = listOf(placeEntity)
        )
        val entity = TravelPlanEntity(
            id = "plan-id",
            name = "Roma Trip",
            periodStart = LocalDate(2024, 6, 1),
            periodEnd = LocalDate(2024, 6, 30),
            days = listOf(dayEntity),
            places = emptyList()
        )

        `when`("toDomain is called") {
            val domain = with(TravelPlanMapper) { entity.toDomain() }

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

    given("a PlaceDomain with a VisitSchedule") {
        val place = PlaceDomain(
            id = "place-2",
            name = "Pantheon",
            lat = 41.89,
            lng = 12.47,
            schedule = VisitScheduleDomain(
                date = LocalDate(2024, 6, 2),
                time = LocalTime(10, 30)
            )
        )

        `when`("toEntity is called") {
            val entity = with(TravelPlanMapper) { place.toEntity() }

            then("entity schedule should not be null") {
                entity.schedule shouldBe VisitScheduleEntity(
                    dateEpochDays = LocalDate(2024, 6, 2).toEpochDays().toInt(),
                    timeHour = 10,
                    timeMinute = 30
                )
            }
        }

        `when`("toEntity and then toDomain is called") {
            val roundTripped = with(TravelPlanMapper) { place.toEntity().toDomain() }

            then("round-tripped place name should match original") {
                roundTripped.name shouldBe place.name
            }
            then("round-tripped schedule time should match original") {
                roundTripped.schedule?.time shouldBe LocalTime(10, 30)
            }
            then("round-tripped schedule date should match original") {
                roundTripped.schedule?.date shouldBe LocalDate(2024, 6, 2)
            }
        }
    }

    given("a StepDomain.Place") {
        val step = StepDomain.Place(
            id = "step-1",
            location = "Colosseo",
            lat = 41.89,
            lng = 12.49
        )

        `when`("toEntity is called") {
            val entity = with(TravelPlanMapper) { step.toEntity() }

            then("entity should be StepEntity.Place") {
                entity shouldBe StepEntity.Place(
                    id = "step-1",
                    location = "Colosseo",
                    lat = 41.89,
                    lng = 12.49
                )
            }
        }

        `when`("toEntity and then toDomain is called") {
            val roundTripped = with(TravelPlanMapper) { step.toEntity().toDomain() }

            then("round-tripped step should match original") {
                roundTripped shouldBe step
            }
        }
    }

    given("a StepDomain.Transport") {
        val route = Route(
            sections = listOf(
                RouteSection(
                    summary = RouteSummary(
                        durationSeconds = 30.minutes,
                        distanceMeters = 5000
                    ),
                    actions = listOf(
                        RouteAction(
                            action = "depart",
                            durationSeconds = 5.minutes,
                            distanceMeters = 1000.0 * Length.meters,
                            instruction = "Head north"
                        )
                    )
                )
            )
        )
        val step = StepDomain.Transport(
            id = "step-2",
            type = TransportType.TRAIN,
            route = route
        )

        `when`("toEntity is called") {
            val entity = with(TravelPlanMapper) { step.toEntity() } as? StepEntity.Transport

            then("entity should be StepEntity.Transport") {
                (entity != null) shouldBe true
            }
            then("entity transportType should be TRAIN") {
                entity!!.transportType shouldBe "TRAIN"
            }
            then("entity route sections size should match") {
                entity!!.route.sections.size shouldBe 1
            }
        }

        `when`("toEntity and then toDomain is called") {
            val roundTripped = with(TravelPlanMapper) { step.toEntity().toDomain() } as? StepDomain.Transport

            then("round-tripped step type should be TRAIN") {
                roundTripped!!.type shouldBe TransportType.TRAIN
            }
            then("round-tripped route sections size should match original") {
                roundTripped!!.route.sections.size shouldBe 1
            }
            then("round-tripped route section duration should match original") {
                roundTripped!!.route.sections[0].summary.durationSeconds shouldBe 30.minutes
            }
        }
    }

    given("a RouteEntity") {
        val routeEntity = RouteEntity(
            sections = listOf(
                RouteSectionEntity(
                    durationSeconds = 1800L,
                    distanceMeters = 5000.0,
                    transportMode = "car",
                    actions = listOf(
                        RouteActionEntity(
                            action = "depart",
                            durationSeconds = 300L,
                            distanceMeters = 1000.0,
                            instruction = "Head north"
                        )
                    )
                )
            )
        )

        `when`("toDomain is called") {
            val domain = with(TravelPlanMapper) { routeEntity.toDomain() }

            then("domain sections size should match entity sections size") {
                domain.sections.size shouldBe 1
            }
            then("domain section duration should match entity durationSeconds") {
                domain.sections[0].summary.durationSeconds shouldBe 1800.seconds
            }
            then("domain section transport mode should match entity transportMode") {
                domain.sections[0].transport?.mode shouldBe "car"
            }
            then("domain section actions size should match entity actions size") {
                domain.sections[0].actions.size shouldBe 1
            }
        }
    }
})
