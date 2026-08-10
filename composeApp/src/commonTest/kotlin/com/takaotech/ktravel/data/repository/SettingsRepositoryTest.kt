package com.takaotech.ktravel.data.repository

import com.takaotech.ktravel.data.datasource.AttachmentDataSource
import com.takaotech.ktravel.data.datasource.TravelPlanStorageDataSource
import com.takaotech.ktravel.data.entity.TravelPlanEntity
import com.takaotech.ktravel.data.entity.TravelSettingsEntity
import dev.mokkery.MockMode
import dev.mokkery.mock
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import kotlinx.datetime.LocalDate

private const val TEST_PLAN_ID = "test-plan-id"
private const val API_KEY = "here-api-key"

/** Records what the repository writes, which is how the persistence assertions are made. */
private class RecordingStorageDataSource(initial: TravelPlanEntity) : TravelPlanStorageDataSource {
    val writes = mutableListOf<TravelPlanEntity>()
    private var stored = initial

    override suspend fun saveTravelPlan(entity: TravelPlanEntity) {
        writes += entity
        stored = entity
    }

    override suspend fun insertTravelPlan(entity: TravelPlanEntity) {
        stored = entity
    }

    override fun getTravelPlan(id: String): TravelPlanEntity = stored

    override suspend fun getTravelPlanNameOrNull(id: String): String? = stored.name

    override suspend fun getAllTravelPlans(): List<TravelPlanEntity> = listOf(stored)

    override suspend fun deleteTravelPlan(id: String) = Unit
}

private fun plan(settings: TravelSettingsEntity = TravelSettingsEntity()) = TravelPlanEntity(
    id = TEST_PLAN_ID,
    name = "Tokyo",
    periodStart = LocalDate.fromEpochDays(0),
    periodEnd = LocalDate.fromEpochDays(0),
    days = emptyList(),
    places = emptyList(),
    settings = settings,
)

private fun repositoryOver(entity: TravelPlanEntity): Pair<SettingsRepositoryImpl, RecordingStorageDataSource> {
    val dataSource = RecordingStorageDataSource(entity)
    val planRepository = TravelPlanRepositoryImpl(
        TEST_PLAN_ID,
        dataSource,
        mock<AttachmentDataSource>(MockMode.autoUnit),
    )
    return SettingsRepositoryImpl(planRepository) to dataSource
}

class SettingsRepositoryTest :
    BehaviorSpec({

        given("a plan whose settings hold a HERE API key") {
            `when`("the settings are read") {
                then("the key is returned") {
                    val (repository, _) = repositoryOver(plan(TravelSettingsEntity(hereApiKey = API_KEY)))

                    repository.settings.hereApiKey shouldBe API_KEY
                }
            }
        }

        given("a plan with no settings configured") {
            `when`("the HERE API key is updated") {
                then("the settings expose the new key") {
                    val (repository, _) = repositoryOver(plan())

                    repository.updateHereApiKey(API_KEY)

                    repository.settings.hereApiKey shouldBe API_KEY
                }

                then("the plan document is persisted with it, so the write goes through the plan repository") {
                    val (repository, dataSource) = repositoryOver(plan())

                    repository.updateHereApiKey(API_KEY)

                    dataSource.writes.lastOrNull().shouldNotBeNull()
                        .settings.hereApiKey shouldBe API_KEY
                }

                then("nothing else in the plan is touched") {
                    val (repository, dataSource) = repositoryOver(plan())

                    repository.updateHereApiKey(API_KEY)

                    val persisted = dataSource.writes.last()
                    persisted.name shouldBe "Tokyo"
                    persisted.days.shouldBeEmpty()
                    persisted.places.shouldBeEmpty()
                }
            }
        }

        given("a plan with a key already configured") {
            `when`("the key is updated to an empty value") {
                then("the settings are cleared") {
                    val (repository, _) = repositoryOver(plan(TravelSettingsEntity(hereApiKey = API_KEY)))

                    repository.updateHereApiKey("")

                    repository.settings.hereApiKey shouldBe ""
                }
            }
        }
    })
