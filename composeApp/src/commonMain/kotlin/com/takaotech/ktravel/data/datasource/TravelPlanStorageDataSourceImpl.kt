package com.takaotech.ktravel.data.datasource

import com.takaotech.ktravel.data.entity.TravelPlanEntity
import com.takaotech.ktravel.data.storage.DatabaseProvider
import kotbase.DataSource
import kotbase.Expression
import kotbase.Meta
import kotbase.MutableDocument
import kotbase.QueryBuilder
import kotbase.SelectResult
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import org.koin.core.annotation.Single

@Single
class TravelPlanStorageDataSourceImpl(
    private val storageRepository: DatabaseProvider
) : TravelPlanStorageDataSource {

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    private val travelCollection = storageRepository.database.createCollection("travel_plans")

    override suspend fun saveTravelPlan(entity: TravelPlanEntity) {
        val jsonString = json.encodeToString(TravelPlanEntity.serializer(), entity)
        val existingDoc = travelCollection.getDocument(entity.id)
        val docToSave = existingDoc?.toMutable()?.also { it.setJSON(jsonString) }
            ?: MutableDocument(entity.id, jsonString)
        storageRepository.scope.launch {
            travelCollection.save(docToSave)
        }
    }

    //TODO Convert to suspend
    override fun getTravelPlan(id: String): TravelPlanEntity {
        val map = travelCollection.getDocument(id)!!
        return json.decodeFromString<TravelPlanEntity>(map.toJSON())
    }

    override suspend fun getAllTravelPlans(): List<TravelPlanEntity> {
        return withContext(storageRepository.readContext) {
            QueryBuilder
                .select(SelectResult.all(), SelectResult.expression(Meta.id).`as`("_id"))
                .from(DataSource.collection(travelCollection))
                .where(
                    Expression.property("type")
                        .equalTo(Expression.string(TravelPlanEntity.DOCUMENT_TYPE))
                )
                .execute()
                .allResults()
                .mapNotNull { result ->
                    result.getDictionary(travelCollection.name)?.toJSON()?.let { jsonString ->
                        json.decodeFromString<TravelPlanEntity>(jsonString)
                            .copy(id = result.getString("_id") ?: "")
                    }
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
