package com.danifitdev.naughtymatch

import android.app.Application
import android.content.Intent
import com.facebook.FacebookSdk
import com.facebook.appevents.AppEventsLogger
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class MyApplication: Application(){
    override fun onCreate() {
        super.onCreate()

        // Inicializa el SDK de Facebook
        FacebookSdk.sdkInitialize(applicationContext)
        AppEventsLogger.activateApp(this)
    }

}