package com.takaotech.ktravel.data.storage

import kotbase.Database
import kotbase.DatabaseConfiguration
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import org.koin.core.annotation.Single
import kotlin.coroutines.CoroutineContext

@Single
class DatabaseProvider(
    val databaseName: String = DB_NAME,
    val directory: String? = null,
    val readContext: CoroutineContext = CoroutineName("db-read") + Dispatchers.IO,
    val writeContext: CoroutineContext = CoroutineName("db-write") + Dispatchers.IO.limitedParallelism(1),
    val scope: CoroutineScope = CoroutineScope(writeContext)
) {

    val database by lazy {
        val config = DatabaseConfiguration()
        if (directory != null) config.directory = directory
        Database(databaseName, config)
    }

    companion object {
        const val DB_NAME = "ktravel-travel-db"
    }
}