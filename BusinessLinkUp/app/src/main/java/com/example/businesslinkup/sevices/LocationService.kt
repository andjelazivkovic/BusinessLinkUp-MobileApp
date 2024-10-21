package com.example.businesslinkup.sevices

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.Context
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.work.OneTimeWorkRequest
import com.example.businesslinkup.R
import com.example.businesslinkup.MainActivity
import androidx.work.WorkManager
import androidx.work.PeriodicWorkRequest
import com.example.businesslinkup.sevices.LocationWorker
import java.util.concurrent.TimeUnit

class LocationService : Service() {

    override fun onCreate() {
        super.onCreate()
        startForegroundService()
        scheduleLocationWork()
    }

    private fun startForegroundService() {
        createNotificationChannel()
        Log.d("LocationService", "Notification channel created")

        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            notificationIntent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, "CHANNEL_ID")
            .setContentTitle("BusinessLinkUp Service")
            .setContentText("Monitoring your location...")
            .setSmallIcon(R.drawable.ic_notification_icon)
            .setContentIntent(pendingIntent)
            .build()

        startForeground(1, notification)
        Log.d("LocationService", "Foreground service started")
    }

    private fun createNotificationChannel() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "CHANNEL_ID",
                "BusinessLinkUp Notifications",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Channel for BusinessLinkUp notifications"
            }
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
            Log.d("LocationService", "Notification channel created")
        }
    }

    private fun scheduleLocationWork() {
        val locationWorkRequest = OneTimeWorkRequest.Builder(LocationWorker::class.java)
            .setInitialDelay(1, TimeUnit.MINUTES)
            .build()

        WorkManager.getInstance(applicationContext).enqueue(locationWorkRequest)
    }


    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onDestroy() {
        super.onDestroy()
    }
}
