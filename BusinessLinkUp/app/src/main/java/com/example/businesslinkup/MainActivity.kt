package com.example.businesslinkup

import android.Manifest
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Build
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.example.businesslinkup.components.AppNavigation
import com.example.businesslinkup.sevices.UserRepository
import com.example.businesslinkup.sevices.BusinessObjectRepository
import com.example.businesslinkup.ui.theme.BusinessLinkUpTheme
import com.example.businesslinkup.viewModels.UserViewModel
import com.example.businesslinkup.viewModels.BusinessObjectViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.businesslinkup.sevices.LocationService

class MainActivity : ComponentActivity() {

    companion object {
        private const val REQUEST_CODE_NOTIFICATION_PERMISSION = 100
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    REQUEST_CODE_NOTIFICATION_PERMISSION
                )
            } else {
                if (areNotificationsEnabled(this)) {
                    startLocationService()
                } else {
                    Toast.makeText(
                        this,
                        "Please enable notifications in the app settings.",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        } else {
            startLocationService()
        }

        setContent {
            BusinessLinkUpTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    App()
                }
            }
        }
    }

    fun areNotificationsEnabled(context: Context): Boolean {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationManager.areNotificationsEnabled()
        } else {
            true
        }
    }

    @Deprecated("This method has been deprecated in favor of using the Activity Result API\n      which brings increased type safety via an {@link ActivityResultContract} and the prebuilt\n      contracts for common intents available in\n      {@link androidx.activity.result.contract.ActivityResultContracts}, provides hooks for\n      testing, and allow receiving results in separate, testable classes independent from your\n      activity. Use\n      {@link #registerForActivityResult(ActivityResultContract, ActivityResultCallback)} passing\n      in a {@link RequestMultiplePermissions} object for the {@link ActivityResultContract} and\n      handling the result in the {@link ActivityResultCallback#onActivityResult(Object) callback}.")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            REQUEST_CODE_NOTIFICATION_PERMISSION -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    startLocationService()
                } else {
                    Toast.makeText(
                        this,
                        "Dozvola za obaveštenja je potrebna za punu funkcionalnost aplikacije.",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    private fun startLocationService() {
        val serviceIntent = Intent(this, LocationService::class.java)
        ContextCompat.startForegroundService(this, serviceIntent)
    }
}

@Composable
fun App() {
    val navController = rememberNavController()
    val auth = remember { FirebaseAuth.getInstance() }
    val firestore = remember { FirebaseFirestore.getInstance() }
    val storage = remember { FirebaseStorage.getInstance() }

    val userRepository = remember { UserRepository(auth, firestore, storage) }
    val userViewModel = remember { UserViewModel(userRepository) }

    val businessObjectRepository = remember { BusinessObjectRepository(firestore, storage) }
    val businessObjectViewModel = remember { BusinessObjectViewModel(businessObjectRepository, userRepository) }

    AppNavigation(
        navController = navController,
        userViewModel = userViewModel,
        businessObjectViewModel = businessObjectViewModel
    )
}
