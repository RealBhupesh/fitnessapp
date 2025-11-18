package com.fitforge.app

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class FitForgeApplication : Application() {
    override fun onCreate() {
        super.onCreate()
    }
}
