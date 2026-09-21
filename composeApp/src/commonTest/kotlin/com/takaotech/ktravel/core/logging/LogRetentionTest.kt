package com.takaotech.ktravel.core.logging

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import kotlinx.datetime.LocalDate

/**
 * How long the log files live.
 *
 * The boundary is the whole point: a retention of three days keeps today and the two days before it,
 * and one file more or less is the difference between a bug report that shows the crash and one that
 * does not.
 */
class LogRetentionTest :
    BehaviorSpec({
        val today = LocalDate(2026, 9, 6)
        val days = listOf(
            LocalDate(2026, 9, 2),
            LocalDate(2026, 9, 3),
            LocalDate(2026, 9, 4),
            LocalDate(2026, 9, 5),
            today,
        )

        given("three days of retention") {
            `when`("the kept days are worked out") {
                then("today and the two days before it survive") {
                    LogRetention.expired(days, today, retentionDays = 3) shouldBe listOf(
                        LocalDate(2026, 9, 2),
                        LocalDate(2026, 9, 3),
                    )
                }
            }
        }

        given("the retention lowered to one day") {
            `when`("the kept days are worked out again") {
                then("only today survives") {
                    LogRetention.expired(days, today, retentionDays = 1) shouldBe days.dropLast(1)
                }
            }
        }

        given("a day in the future, which means the clock moved backwards") {
            `when`("the kept days are worked out") {
                then("it is kept, because it holds the session that is running") {
                    val future = LocalDate(2026, 9, 20)

                    LogRetention.expired(listOf(future), today, retentionDays = 3) shouldBe emptyList()
                }
            }
        }

        given("a retention outside the allowed range") {
            `when`("it is coerced") {
                then("it comes back inside it") {
                    LogRetention.coerce(0) shouldBe MIN_LOG_RETENTION_DAYS
                    LogRetention.coerce(9_000) shouldBe MAX_LOG_RETENTION_DAYS
                    LogRetention.coerce(DEFAULT_LOG_RETENTION_DAYS) shouldBe DEFAULT_LOG_RETENTION_DAYS
                }
            }
        }
    })
