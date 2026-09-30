package com.takaotech.ktravel.ui.plan.place.component

import com.takaotech.ktravel.domain.search.model.OpeningPeriod
import com.takaotech.ktravel.domain.search.model.PlaceContact
import com.takaotech.ktravel.domain.search.model.PlaceContactKind
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import kotlinx.collections.immutable.persistentSetOf
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalTime
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes

class PlaceDetailSectionsTest :
    BehaviorSpec({

        given("a restaurant open for lunch and dinner on weekdays and late on Saturday") {
            val periods = listOf(
                OpeningPeriod(
                    days = persistentSetOf(DayOfWeek.MONDAY, DayOfWeek.TUESDAY),
                    opensAt = LocalTime(hour = 19, minute = 30),
                    duration = 3.hours + 30.minutes,
                ),
                OpeningPeriod(
                    days = persistentSetOf(DayOfWeek.MONDAY, DayOfWeek.TUESDAY),
                    opensAt = LocalTime(hour = 12, minute = 30),
                    duration = 150.minutes,
                ),
                OpeningPeriod(
                    days = persistentSetOf(DayOfWeek.SATURDAY),
                    opensAt = LocalTime(hour = 20, minute = 0),
                    duration = 6.hours,
                ),
            )

            `when`("its week is listed") {
                val week = openingHoursByDay(periods)

                then("every day appears once, from Monday") {
                    week.map { it.day } shouldBe DayOfWeek.entries
                }

                then("the periods of a day are in opening order") {
                    week.first { it.day == DayOfWeek.MONDAY }.spans shouldBe listOf("12:30–15:00", "19:30–23:00")
                }

                then("a period past midnight stays on the day it opens, closing on the next day's clock") {
                    week.first { it.day == DayOfWeek.SATURDAY }.spans shouldBe listOf("20:00–02:00")
                }

                then("a day no period covers has no spans, which the card shows as closed") {
                    week.first { it.day == DayOfWeek.SUNDAY }.spans shouldBe emptyList()
                }
            }
        }

        given("contacts of every kind") {
            `when`("they are tapped") {
                then("each opens what its kind calls for") {
                    PlaceContact(PlaceContactKind.PHONE, "+39 06 581 2260").toUri() shouldBe "tel:+39065812260"
                    PlaceContact(PlaceContactKind.MOBILE, "333 000 0000").toUri() shouldBe "tel:3330000000"
                    PlaceContact(PlaceContactKind.EMAIL, "info@example.it").toUri() shouldBe "mailto:info@example.it"
                    PlaceContact(PlaceContactKind.WEBSITE, "https://example.it").toUri() shouldBe "https://example.it"
                    PlaceContact(PlaceContactKind.WEBSITE, "www.example.it").toUri() shouldBe "https://www.example.it"
                }
            }
        }
    })
