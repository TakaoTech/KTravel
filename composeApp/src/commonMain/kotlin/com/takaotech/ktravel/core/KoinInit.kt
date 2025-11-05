package com.takaotech.ktravel.core

import org.koin.core.KoinApplication
import org.koin.core.context.startKoin
import org.koin.dsl.KoinAppDeclaration

expect fun KoinApplication.platformModules()

fun startKTravelKoin(
    config: KoinAppDeclaration? = null
) {
    startKoin {
        config?.invoke(this)
        platformModules()
    }
}