package com.takaotech.ktravel.core

import org.koin.core.KoinApplication

expect fun KoinApplication.platformModules()

//fun startKTravelKoin(
//    config: KoinAppDeclaration? = null
//) {
//    startKoin {
//        config?.invoke(this)
//        modules(appModule())
//        platformModules()
//    }
//}