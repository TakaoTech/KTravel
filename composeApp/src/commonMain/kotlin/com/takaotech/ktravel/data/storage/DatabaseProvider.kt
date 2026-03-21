package com.takaotech.ktravel.data.storage

import kotbase.Database
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import org.koin.core.annotation.Single
import kotlin.coroutines.CoroutineContext

@Single
class DatabaseProvider(
    val readContext: CoroutineContext = CoroutineName("db-read") + Dispatchers.IO,
    val writeContext: CoroutineContext = CoroutineName("db-write") + Dispatchers.IO.limitedParallelism(1),
    val scope: CoroutineScope = CoroutineScope(writeContext)
) {

    val database by lazy { Database(DB_NAME) }

    companion object {
        private const val DB_NAME = "kotbase-notes"
    }
}