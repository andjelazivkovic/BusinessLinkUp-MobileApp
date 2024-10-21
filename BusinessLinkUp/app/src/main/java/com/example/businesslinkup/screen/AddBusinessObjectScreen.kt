package com.example.businesslinkup.screen

import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.businesslinkup.entities.BusinessObject
import com.example.businesslinkup.entities.ObjectType
import com.example.businesslinkup.sevices.NominatimService
import com.example.businesslinkup.sevices.NominatimResult
import com.example.businesslinkup.viewModels.BusinessObjectViewModel
import com.example.businesslinkup.viewModels.UserViewModel
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.util.Date
import java.util.UUID
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.saveable.rememberSaveable


@Composable
fun AddObjectScreen(viewModel: BusinessObjectViewModel, userViewModel: UserViewModel) {
    var address by rememberSaveable { mutableStateOf("") }
    var desc by rememberSaveable { mutableStateOf("") }
    var tlt by rememberSaveable { mutableStateOf("") }
    var photoUri by remember { mutableStateOf<Uri?>(null) }
    var objectType by remember { mutableStateOf(ObjectType.OTHER) }
    var latitude by remember { mutableDoubleStateOf(0.0) }
    var longitude by remember { mutableDoubleStateOf(0.0) }
    var showToast by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var showImagePicker by remember { mutableStateOf(false) }
    var autocompleteSuggestions by remember { mutableStateOf<List<NominatimResult>>(emptyList()) }
    var selectedAddress by remember { mutableStateOf<String?>(null) }

    val nominatimService = remember { NominatimService.create() }
    val scrollState = rememberScrollState() // Scroll state for the Column

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        photoUri = uri
    }
    if (showImagePicker) {
        launcher.launch("image/*")
        showImagePicker = false
    }

    fun validateAndSubmit() {
        scope.launch {
            try {
                val userId = userViewModel.getIdCurrentUser() ?: throw Exception("User not logged in")

                val newObject = BusinessObject(
                    id = UUID.randomUUID().toString(),
                    desc = desc,
                    type = objectType,
                    latitude = latitude,
                    longitude = longitude,
                    address = selectedAddress ?: "",
                    photoUrl = "",
                    createDate = Date(),
                    title=tlt,
                    userId = userId
                )
                viewModel.addObject(newObject, photoUri)
                showToast = "Object added successfully"
            } catch (e: Exception) {
                showToast = "Error: ${e.message}"
            }
        }
    }

    fun fetchAddressSuggestions(query: String) {
        scope.launch {
            try {
                val response = nominatimService.search(query)
                autocompleteSuggestions = response
            } catch (e: HttpException) {
                Log.e("Nominatim", "Error fetching address suggestions", e)
            }
        }
    }

    fun selectAddress(result: NominatimResult) {
        latitude = result.lat.toDouble()
        longitude = result.lon.toDouble()
        address = result.display_name
        selectedAddress = address
    }

    LaunchedEffect(address) {
        if (address.isNotEmpty()) {
            fetchAddressSuggestions(address)
        }
    }

    val isFormValid = address.isNotEmpty() && desc.isNotEmpty() && tlt.isNotEmpty() && photoUri != null

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .verticalScroll(scrollState),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Address TextField
        TextField(
            value = address,
            onValueChange = {
                address = it
                if (it.isNotEmpty()) {
                    fetchAddressSuggestions(it)
                }
            },
            label = { Text("Address") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))

        autocompleteSuggestions.forEach { result ->
            Text(
                text = result.display_name,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { selectAddress(result) }
                    .padding(8.dp)
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        TextField(
            value = desc,
            onValueChange = { desc = it },
            label = { Text("Description") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        TextField(
            value = tlt,
            onValueChange = { tlt = it },
            label = { Text("Title") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text("Object Type", style = MaterialTheme.typography.titleMedium)
        ObjectType.values().forEach { type ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .selectable(
                        selected = (type == objectType),
                        onClick = { objectType = type }
                    )
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = (type == objectType),
                    onClick = { objectType = type }
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(type.name)
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = { showImagePicker = true }) {
            Text("Select Object Image")
        }
        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = { validateAndSubmit() },
            enabled = isFormValid
        ) {
            Text("Add Object")
        }

        showToast?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            showToast = null
        }
    }
}
