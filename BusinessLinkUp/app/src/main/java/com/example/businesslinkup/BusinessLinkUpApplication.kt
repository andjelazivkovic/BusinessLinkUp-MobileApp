package com.example.businesslinkup

import android.app.Application
import androidx.work.Configuration
import androidx.work.WorkManager
import com.google.firebase.FirebaseApp

class BusinessLinkUpApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)

    }
}
