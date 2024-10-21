package com.example.businesslinkup.screen

import com.example.businesslinkup.components.CustomMapMarker
import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Looper
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.businesslinkup.entities.ObjectType
import com.example.businesslinkup.viewModels.BusinessObjectViewModel
import com.google.android.gms.location.*
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*

@Composable
fun MapScreen(navController: NavController, viewModel: BusinessObjectViewModel = viewModel()) {
    val context = LocalContext.current
    val businessObjects by viewModel.businessObjects.collectAsState(initial = emptyList())
    val cameraPositionState = rememberCameraPositionState()

    var selectedTypes by remember { mutableStateOf<Set<String>>(emptySet()) }
    var searchTitle by rememberSaveable { mutableStateOf("") }
    var maxDistanceText by rememberSaveable { mutableStateOf("") }
    var initialLocation by remember { mutableStateOf<LatLng?>(null) }

    val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            fetchUserLocationAndSetMap(context, fusedLocationClient, cameraPositionState, viewModel) { location ->
                initialLocation = location
                viewModel.searchBusinessObjects(
                    title = searchTitle,
                    types = selectedTypes,
                    maxDistance = Double.MAX_VALUE,
                    userLat = location.latitude,
                    userLon = location.longitude
                )
            }
        }
    }

    LaunchedEffect(Unit) {
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            fetchUserLocationAndSetMap(context, fusedLocationClient, cameraPositionState, viewModel) { location ->
                initialLocation = location
                viewModel.searchBusinessObjects(
                    title = searchTitle,
                    types = selectedTypes,
                    maxDistance = Double.MAX_VALUE,
                    userLat = location.latitude,
                    userLon = location.longitude
                )
            }
        } else {
            permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .padding(8.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            TextField(
                value = searchTitle,
                onValueChange = { searchTitle = it },
                label = { Text("Search Title") },
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(8.dp))
        }

        LazyVerticalGrid(
            columns = GridCells.Fixed(1),
            modifier = Modifier.padding(8.dp)
        ) {
            items(ObjectType.values().toList()) { type ->
                Row(modifier = Modifier.padding(4.dp)) {
                    Checkbox(
                        checked = selectedTypes.contains(type.name),
                        onCheckedChange = {
                            selectedTypes = if (it) {
                                selectedTypes + type.name
                            } else {
                                selectedTypes - type.name
                            }
                        }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = type.name)
                }
            }
        }

        Row(modifier = Modifier.padding(8.dp)) {
            Text("Max Distance (km):")
            Spacer(modifier = Modifier.width(8.dp))
            TextField(
                value = maxDistanceText,
                onValueChange = {
                    maxDistanceText = it.filter { char -> char.isDigit() }
                },
                label = { Text("Distance") },
                modifier = Modifier.width(100.dp)
            )
        }

        Button(onClick = {
            val userLat = initialLocation?.latitude ?: cameraPositionState.position.target.latitude
            val userLon = initialLocation?.longitude ?: cameraPositionState.position.target.longitude
            val maxDistance = maxDistanceText.toDoubleOrNull() ?: Double.MAX_VALUE
            viewModel.searchBusinessObjects(
                title = searchTitle,
                types = selectedTypes,
                maxDistance = maxDistance,
                userLat = userLat,
                userLon = userLon
            )
        }) {
            Text("Search")
        }

        GoogleMap(
            modifier = Modifier.weight(1f),
            cameraPositionState = cameraPositionState,
            properties = MapProperties(isMyLocationEnabled = true)
        ) {
            businessObjects.forEach { businessObject ->
                val location = LatLng(businessObject.latitude, businessObject.longitude)
                CustomMapMarker(
                    location = location,
                    title = businessObject.title,
                    type = businessObject.type.name,
                    onClick = {
                        navController.navigate("business_object_detail/${businessObject.id.toString()}")
                    }
                )
            }
        }
    }
}



private fun fetchUserLocationAndSetMap(
    context: Context,
    fusedLocationClient: FusedLocationProviderClient,
    cameraPositionState: CameraPositionState,
    viewModel: BusinessObjectViewModel,
    onLocationFound: (LatLng) -> Unit
) {
    val locationRequest = LocationRequest.create().apply {
        interval = 10000L
        fastestInterval = 5000L
        priority = Priority.PRIORITY_HIGH_ACCURACY
    }

    var initialLocationSet = false

    val locationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            val location = locationResult.lastLocation
            val userLocation = location?.let {
                LatLng(it.latitude, it.longitude)
            } ?: LatLng(44.7866, 20.4489)

            if (!initialLocationSet) {
                cameraPositionState.position = CameraPosition(
                    userLocation,
                    12f,
                    0f,
                    0f
                )
                viewModel.setUserLocation(userLocation.latitude, userLocation.longitude)
                initialLocationSet = true
                onLocationFound(userLocation)
            }
        }
    }

    if (ActivityCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) != PackageManager.PERMISSION_GRANTED
    ) {
        return
    }
    fusedLocationClient.requestLocationUpdates(
        locationRequest,
        locationCallback,
        Looper.getMainLooper()
    )
}

