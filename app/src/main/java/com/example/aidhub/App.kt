package com.example.aidhub

import android.app.Application
import com.example.aidhub.managers.LocationManager
import com.example.aidhub.managers.NotificationsManager
import com.example.aidhub.managers.SettingsManager
import com.example.aidhub.utilities.ToastHelper
import com.example.data.DataModule
import com.google.firebase.FirebaseApp

class App : Application(){
    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)
        SettingsManager.init(this)
        LocationManager.init(this)
        NotificationsManager.init(this)
        DataModule.initAll()
        ToastHelper.init(this)

    }
}