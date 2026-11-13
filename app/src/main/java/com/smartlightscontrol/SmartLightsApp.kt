package com.smartlightscontrol

import android.app.Application
import com.smartlightscontrol.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class SmartLightsApp : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@SmartLightsApp)
            modules(appModule)
        }
    }
}
