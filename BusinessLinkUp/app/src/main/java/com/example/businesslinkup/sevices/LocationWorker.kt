package com.example.businesslinkup.sevices

import android.Manifest
import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import androidx.core.app.NotificationCompat
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.content.ContextCompat
import com.example.businesslinkup.MainActivity
import com.example.businesslinkup.R
import com.example.businesslinkup.viewModels.SettingsManager
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.auth.FirebaseAuth
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.Task
import com.google.android.gms.maps.model.LatLng

class LocationWorker(context: Context, params: WorkerParameters) : Worker(context, params) {

    private val firestore = FirebaseFirestore.getInstance()
    private val fusedLocationClient: FusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context)

    override fun doWork(): Result {
        if (ContextCompat.checkSelfPermission(applicationContext, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return Result.failure()
        }

        val locationTask: Task<android.location.Location> = fusedLocationClient.lastLocation
        locationTask.addOnSuccessListener { location ->
            location?.let {
                val userId = getCurrentUserId() ?: return@addOnSuccessListener
                val userLocation = LatLng(location.latitude, location.longitude)
                updateLocationInFirestore(userId, userLocation)
                checkForNearbyObjects(userLocation)
            }
        }.addOnFailureListener {
            it.printStackTrace()
        }
        return Result.success()
    }

    private fun getCurrentUserId(): String? {
        val auth = FirebaseAuth.getInstance()
        val user = auth.currentUser
        return user?.uid
    }

    private fun updateLocationInFirestore(userId: String, location: LatLng) {
        firestore.collection("users").document(userId)
            .update("latitude", location.latitude)
            .addOnSuccessListener {
                Log.d("LocationWorker", "Latitude updated successfully")
            }
            .addOnFailureListener { e ->
                Log.e("LocationWorker", "Error updating latitude", e)
            }

        firestore.collection("users").document(userId)
            .update("longitude", location.longitude)
            .addOnSuccessListener {
                Log.d("LocationWorker", "Longitude updated successfully")
            }
            .addOnFailureListener { e ->
                Log.e("LocationWorker", "Error updating longitude", e)
            }
    }



    private fun checkForNearbyObjects(userLocation: LatLng) {
        val settingsManager = SettingsManager(applicationContext)
        if (!settingsManager.areNotificationsEnabled()) return

        val radius = settingsManager.getRadius()
        firestore.collection("business_objects").get().addOnSuccessListener { documents ->
            for (document in documents) {
                val latitude = document.get("latitude")
                val longitude = document.get("longitude")

                if (latitude is Number && longitude is Number) {
                    val objectLocation = LatLng(
                        (latitude as Number).toDouble(),
                        (longitude as Number).toDouble()
                    )

                    if (isWithinRadius(userLocation, objectLocation, radius)) {
                        showNotification()
                    }
                } else {
                    Log.w("LocationWorker", "Latitude or Longitude is not a number for document ${document.id}")
                }
            }
        }.addOnFailureListener { e ->
            Log.e("LocationWorker", "Error fetching documents", e)
        }
    }



    private fun isWithinRadius(userLocation: LatLng, objectLocation: LatLng, radius: Int): Boolean {
        val results = FloatArray(1)
        android.location.Location.distanceBetween(userLocation.latitude, userLocation.longitude,
            objectLocation.latitude, objectLocation.longitude, results)
        return results[0] <= radius
    }

    private fun showNotification() {
        val intent = Intent(applicationContext, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(applicationContext, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

        val notification = NotificationCompat.Builder(applicationContext, "CHANNEL_ID")
            .setContentTitle("New Nearby Object")
            .setContentText("Click to view details.")
            .setSmallIcon(R.drawable.ic_service_icon)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(1, notification)
        Log.d("LocationWorker", "Notification shown")
    }
}
