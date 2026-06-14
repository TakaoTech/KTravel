package com.takaotech.ktravel

import android.app.Application
import com.takaotech.ktravel.di.AppGraph
import com.takaotech.ktravel.di.createAppGraph

class KTravelApplication : Application() {
    val appGraph: AppGraph by lazy { createAppGraph() }

    override fun onCreate() {
        super.onCreate()
    }
}