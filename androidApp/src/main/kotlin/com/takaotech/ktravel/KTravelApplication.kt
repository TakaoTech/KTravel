package com.takaotech.ktravel

import android.app.Application
import com.takaotech.ktravel.di.startKTravelKoin
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger

class KTravelApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        startKTravelKoin {
            androidContext(this@KTravelApplication)
            androidLogger()
        }
    }
}