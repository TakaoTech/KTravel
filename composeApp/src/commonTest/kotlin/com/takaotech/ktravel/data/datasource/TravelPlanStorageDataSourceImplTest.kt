package com.takaotech.ktravel.data.datasource

import com.takaotech.ktravel.data.entity.TravelPlanEntity
import com.takaotech.ktravel.data.storage.DatabaseProvider
import com.takaotech.ktravel.testutil.tempdir
import io.github.vinceglb.filekit.createDirectories
import io.github.vinceglb.filekit.div
import io.github.vinceglb.filekit.path
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.datetime.LocalDate

private fun sampleEntity(id: String, name: String = "Test Plan") = TravelPlanEntity(
    id = id,
    name = name,
    periodStart = LocalDate(2025, 1, 1),
    periodEnd = LocalDate(2025, 1, 7),
    days = emptyList(),
    places = emptyList()
)

@OptIn(ExperimentalCoroutinesApi::class)
class TravelPlanStorageDataSourceImplTest : BehaviorSpec({
    val tempDir = tempdir("test-travel")

    lateinit var dataSource: TravelPlanStorageDataSourceImpl
    lateinit var provider: DatabaseProvider
    lateinit var testScope: TestScope

    beforeTest {
        val testDir = (tempDir / it.name.name.replace(Regex("\\W+"), "_"))
            .also { dir -> dir.createDirectories() }
        testScope = TestScope(UnconfinedTestDispatcher())
        provider = DatabaseProvider(
            databaseName = "test-travel",
            directory = testDir.path,
            scope = testScope
        )
        dataSource = TravelPlanStorageDataSourceImpl(provider)
    }

    afterTest {
        try {
            provider.database.close()
        } catch (e: Exception) { /* already closed */
        }
    }

    given("an empty database") {
        `when`("getAllTravelPlans is called") {
            then("should return an empty list") {
                dataSource.getAllTravelPlans().shouldBeEmpty()
            }
        }
    }

    given("a database with one saved travel plan") {
        `when`("getAllTravelPlans is called") {
            then("should return a list with one element") {
                dataSource.saveTravelPlan(sampleEntity("plan-1"))
                dataSource.getAllTravelPlans() shouldHaveSize 1
            }

            then("should return entities with the document id populated") {
                dataSource.saveTravelPlan(sampleEntity("plan-1"))
                dataSource.getAllTravelPlans().first().id shouldBe "plan-1"
            }
        }

        `when`("getTravelPlan is called with the saved id") {
            then("should return entity with matching fields") {
                val entity = sampleEntity("plan-1", "My Trip")
                dataSource.saveTravelPlan(entity)
                val result = dataSource.getTravelPlan("plan-1")
                result.name shouldBe entity.name
                result.periodStart shouldBe entity.periodStart
                result.periodEnd shouldBe entity.periodEnd
                result.days shouldBe entity.days
                result.places shouldBe entity.places
            }

            then("id field is empty because it is @Transient and not serialised") {
                dataSource.saveTravelPlan(sampleEntity("plan-1"))
                dataSource.getTravelPlan("plan-1").id shouldBe ""
            }
        }
    }

    given("a database with two saved travel plans") {
        `when`("getAllTravelPlans is called") {
            then("should return all saved entities") {
                dataSource.saveTravelPlan(sampleEntity("plan-1", "First Plan"))
                dataSource.saveTravelPlan(sampleEntity("plan-2", "Second Plan"))
                dataSource.getAllTravelPlans() shouldHaveSize 2
            }
        }
    }

    given("a saved travel plan that is then deleted") {
        `when`("deleteTravelPlan is called") {
            then("getAllTravelPlans should return an empty list") {
                dataSource.saveTravelPlan(sampleEntity("to-delete"))
                dataSource.deleteTravelPlan("to-delete")
                testScope.advanceUntilIdle()
                dataSource.getAllTravelPlans().shouldBeEmpty()
            }
        }
    }
})
