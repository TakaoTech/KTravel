package com.takaotech.ktravel.data.datasource

import com.takaotech.ktravel.core.annotation.OpenForMokkery
import com.takaotech.ktravel.data.entity.AppSettingsEntity

/** Reads and writes the single document holding this installation's preferences. */
@OpenForMokkery
interface AppSettingsStorageDataSource {

    /**
     * The stored preferences, or their defaults when nothing has been written yet.
     *
     * Not suspending, like [TravelPlanStorageDataSource.getTravelPlan]: it is one local document read
     * by primary key, and the repository above it needs a value to start from rather than a promise
     * of one.
     */
    fun getAppSettings(): AppSettingsEntity

    /** Replaces them, and waits for the write to land. */
    suspend fun saveAppSettings(entity: AppSettingsEntity)
}
