package com.takaotech.ktravel

import android.app.Application
import com.takaotech.ktravel.core.startKTravelKoin
import org.koin.android.ext.koin.androidContext

class KTravelApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        startKTravelKoin {
            androidContext(this@KTravelApplication)
        }
    }
}