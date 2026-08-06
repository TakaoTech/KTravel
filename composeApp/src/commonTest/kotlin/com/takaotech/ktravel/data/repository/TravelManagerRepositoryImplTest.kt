package com.takaotech.ktravel.data.repository

import com.takaotech.ktravel.data.datasource.TravelPlanStorageDataSource
import com.takaotech.ktravel.data.entity.TravelPlanEntity
import io.kotest.assertions.throwables.shouldNotThrowAny
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.shouldBe
import kotlinx.datetime.LocalDate

/**
 * Mimics the two write paths of the real data source: [saveTravelPlan] is fire-and-forget, so the
 * document only becomes readable once the pending write is drained, while [insertTravelPlan] awaits
 * it.
 */
private class FakeTravelPlanStorageDataSource : TravelPlanStorageDataSource {
    private val stored = mutableMapOf<String, TravelPlanEntity>()
    val pendingWrites = mutableListOf<TravelPlanEntity>()

    override suspend fun saveTravelPlan(entity: TravelPlanEntity) {
        pendingWrites += entity
    }

    override suspend fun insertTravelPlan(entity: TravelPlanEntity) {
        stored[entity.id] = entity
    }

    override fun getTravelPlan(id: String): TravelPlanEntity = stored[id] ?: error("No travel plan stored with id $id")

    override suspend fun getTravelPlanNameOrNull(id: String): String? = stored[id]?.name

    override suspend fun getAllTravelPlans(): List<TravelPlanEntity> = stored.values.toList()

    override suspend fun deleteTravelPlan(id: String) {
        stored.remove(id)
    }
}

class TravelManagerRepositoryImplTest :
    BehaviorSpec({
        val periodStart = LocalDate(2025, 1, 1)
        val periodEnd = LocalDate(2025, 1, 7)

        given("a repository backed by the travel plan storage") {
            `when`("createTravelPlan returns the new id") {
                then("the plan is readable straight away, without draining a pending write") {
                    val dataSource = FakeTravelPlanStorageDataSource()
                    val repository = TravelManagerRepositoryImpl(dataSource)

                    val id = repository.createTravelPlan("My Trip", periodStart, periodEnd)

                    shouldNotThrowAny { dataSource.getTravelPlan(id) }
                    dataSource.pendingWrites.shouldBeEmpty()
                }

                then("the stored plan carries the requested name and period") {
                    val dataSource = FakeTravelPlanStorageDataSource()
                    val repository = TravelManagerRepositoryImpl(dataSource)

                    val id = repository.createTravelPlan("My Trip", periodStart, periodEnd)

                    val stored = dataSource.getTravelPlan(id)
                    stored.name shouldBe "My Trip"
                    stored.periodStart shouldBe periodStart
                    stored.periodEnd shouldBe periodEnd
                }

                then("each call produces a distinct id") {
                    val dataSource = FakeTravelPlanStorageDataSource()
                    val repository = TravelManagerRepositoryImpl(dataSource)

                    val first = repository.createTravelPlan("First", periodStart, periodEnd)
                    val second = repository.createTravelPlan("Second", periodStart, periodEnd)

                    (first == second) shouldBe false
                    dataSource.getAllTravelPlans().size shouldBe 2
                }
            }
        }

        given("a repository with one created travel plan") {
            `when`("deleteTravelPlan is called with its id") {
                then("the plan is no longer stored") {
                    val dataSource = FakeTravelPlanStorageDataSource()
                    val repository = TravelManagerRepositoryImpl(dataSource)
                    val id = repository.createTravelPlan("My Trip", periodStart, periodEnd)

                    repository.deleteTravelPlan(id)

                    dataSource.getAllTravelPlans().shouldBeEmpty()
                }
            }

            `when`("getAllTravelPlans is called") {
                then("the summary of the created plan is returned") {
                    val dataSource = FakeTravelPlanStorageDataSource()
                    val repository = TravelManagerRepositoryImpl(dataSource)
                    repository.createTravelPlan("My Trip", periodStart, periodEnd)

                    val summaries = repository.getAllTravelPlans()

                    summaries.size shouldBe 1
                    summaries.first().name shouldBe "My Trip"
                }
            }
        }
    })
