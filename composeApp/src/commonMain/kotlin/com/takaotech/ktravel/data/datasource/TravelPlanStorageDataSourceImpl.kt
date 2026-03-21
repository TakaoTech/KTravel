package com.takaotech.ktravel.data.datasource

import com.takaotech.ktravel.data.entity.TravelPlanEntity
import com.takaotech.ktravel.data.storage.DatabaseProvider
import kotbase.*
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import org.koin.core.annotation.Single

@Single
class TravelPlanStorageDataSourceImpl(
    private val storageRepository: DatabaseProvider
) : TravelPlanStorageDataSource {

    private val json = Json { ignoreUnknownKeys = true }

    private val travelCollection = storageRepository.database.createCollection("travel_plans")

    companion object {
        const val TRAVEL_PLAN_ID = "current_travel_plan"
    }

    override fun saveTravelPlan(entity: TravelPlanEntity) {
        val jsonString = json.encodeToString(TravelPlanEntity.serializer(), entity)
        travelCollection.save(
            MutableDocument(entity.id, jsonString)
        )

    }

    override fun getTravelPlan(id: String): TravelPlanEntity {
        val map = travelCollection.getDocument(id)!!
        return json.decodeFromString<TravelPlanEntity>(map.toJSON())
    }

    override fun getAllTravelPlans(): List<TravelPlanEntity> {
        return QueryBuilder
            .select(SelectResult.all())
            .from(DataSource.collection(travelCollection))
            .where(
                Expression.property("type")
                    .equalTo(Expression.string(TravelPlanEntity.DOCUMENT_TYPE))
            )
            .execute()
            .allResults()
            .mapNotNull { result ->
                result.getDictionary(0)?.toJSON()?.let { jsonString ->
                    json.decodeFromString<TravelPlanEntity>(jsonString)
                }
            }
    }

    override fun deleteTravelPlan(id: String) {
        storageRepository.scope.launch {
            travelCollection.getDocument(id)?.let { document ->
                travelCollection.delete(document)
            }
        }
    }
}
