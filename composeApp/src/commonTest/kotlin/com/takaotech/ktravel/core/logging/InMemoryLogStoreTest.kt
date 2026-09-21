package com.takaotech.ktravel.core.logging

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import kotlin.time.Instant

/**
 * The buffer behind the diagnostics screen.
 *
 * What is worth checking is the bound: a session that logs all day writes far more than a screen can
 * show, and a buffer that kept all of it would trade a diagnostics feature for a leak.
 */
class InMemoryLogStoreTest :
    BehaviorSpec({
        given("a store holding one line short of its capacity") {
            `when`("two more lines arrive") {
                then("the oldest one is dropped and the order is kept") {
                    val store = InMemoryLogStore()

                    repeat(MAX_LOG_ENTRIES) { store.record(entry(it)) }
                    store.record(entry(MAX_LOG_ENTRIES))

                    val held = store.entries.value
                    held.size shouldBe MAX_LOG_ENTRIES
                    held.first().id shouldBe "1"
                    held.last().id shouldBe MAX_LOG_ENTRIES.toString()
                }
            }
        }

        given("a store with lines in it") {
            `when`("it is cleared") {
                then("nothing is held any more") {
                    val store = InMemoryLogStore()
                    store.record(entry(0))

                    store.clear()

                    store.entries.value.size shouldBe 0
                }
            }
        }
    })

private fun entry(index: Int) = LogEntry(
    id = index.toString(),
    timestamp = Instant.fromEpochMilliseconds(index.toLong()),
    level = LogLevel.Info,
    tag = "test",
    message = "line $index",
)
