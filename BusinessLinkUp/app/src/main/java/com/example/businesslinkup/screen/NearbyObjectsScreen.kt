package com.example.businesslinkup.screen

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.businesslinkup.entities.BusinessObject
import com.example.businesslinkup.viewModels.BusinessObjectViewModel
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import com.example.businesslinkup.components.BusinessObjectCard

@Composable
fun NearbyObjectsScreen(
    navController: NavController,
    businessObjectViewModel: BusinessObjectViewModel = viewModel()
) {
    val context = LocalContext.current
    val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
    val coroutineScope = rememberCoroutineScope()

    val hasLocationPermission = ContextCompat.checkSelfPermission(
        context, Manifest.permission.ACCESS_FINE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            coroutineScope.launch {
                fetchLocationAndUpdate(fusedLocationClient, businessObjectViewModel)
            }
        } else {
            // Handle permission denial
        }
    }

    LaunchedEffect(hasLocationPermission) {
        if (hasLocationPermission) {
            coroutineScope.launch {
                fetchLocationAndUpdate(fusedLocationClient, businessObjectViewModel)
            }
        } else {
            permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    val businessObjects by businessObjectViewModel.businessObjects.collectAsState()

    Column {
        LazyColumn {
            items(businessObjects) { businessObject ->
                BusinessObjectCard(
                    businessObject = businessObject,
                    onClick = {
                        navController.navigate("business_object_detail/${businessObject.id.toString()}")
                    }
                )
            }
        }
    }
}

suspend fun fetchLocationAndUpdate(
    fusedLocationClient: FusedLocationProviderClient,
    businessObjectViewModel: BusinessObjectViewModel
) {
    try {
        val location: Location? = fusedLocationClient.lastLocation.await()
        location?.let {
            val userLat = it.latitude
            val userLon = it.longitude
            businessObjectViewModel.fetchBusinessObjectsInRealTimeDistance(userLat, userLon)
        }
    } catch (e: SecurityException) {
        // Handle exception
    }
}
