package com.oqza.myzenflow

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class MyZenFlowApplication : Application() {
    override fun onCreate() {
        super.onCreate()
    }
}
