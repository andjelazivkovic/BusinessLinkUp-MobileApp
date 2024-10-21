package com.example.businesslinkup.screen

import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.navigation.NavController
import com.example.businesslinkup.viewModels.UserViewModel
import coil.compose.rememberImagePainter
import com.example.businesslinkup.R
import kotlinx.coroutines.launch
import com.example.businesslinkup.components.AppNavigation
import com.example.businesslinkup.components.DrawerContent
import com.example.businesslinkup.viewModels.SettingsManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlin.Result


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(userViewModel: UserViewModel, navController: NavController) {
    LaunchedEffect(Unit) {
        userViewModel.refreshCurrentUser()
    }

    val userState by userViewModel.current.collectAsState()
    val changePasswordError by userViewModel.changePasswordError.collectAsState()
    var editingName by rememberSaveable { mutableStateOf(false) }
    var editingPassword by rememberSaveable { mutableStateOf(false) }
    var firstName by rememberSaveable { mutableStateOf(userState?.firstName ?: "") }
    var lastName by rememberSaveable { mutableStateOf(userState?.lastName ?: "") }
    var currentPassword by rememberSaveable { mutableStateOf("") }
    var newPassword by rememberSaveable { mutableStateOf("") }
    var confirmPassword by rememberSaveable { mutableStateOf("") }
    var profileImageUri by remember { mutableStateOf<Uri?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    val signOutError by userViewModel.signOutError.collectAsState()
    val drawerState = rememberDrawerState(DrawerValue.Closed)

    val settingsManager = SettingsManager(context = LocalContext.current)
    var radius by remember { mutableStateOf(settingsManager.getRadius()) }
    var notificationsEnabled by remember { mutableStateOf(settingsManager.areNotificationsEnabled()) }

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let { selectedUri ->
            profileImageUri = selectedUri
            userViewModel.changeProfilePicture(selectedUri)
            coroutineScope.launch {
                snackbarHostState.showSnackbar("Profile picture updated")
            }
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            DrawerContent(navController, coroutineScope, drawerState, userViewModel)
        }
    ) {
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = { Text("Profile") },
                    navigationIcon = {
                        IconButton(onClick = { coroutineScope.launch { drawerState.open() } }) {
                            Icon(imageVector = Icons.Filled.Menu, contentDescription = "Menu")
                        }
                    }
                )
            },
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 16.dp)
                        .padding(start = 56.dp, end = 56.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    userState?.let { user ->
                        val painter = rememberImagePainter(
                            data = profileImageUri ?: user.profilePictureUrl,
                            builder = {
                                placeholder(R.drawable.icon)
                                error(R.drawable.icon)
                            }
                        )
                        Image(
                            painter = painter,
                            contentDescription = "Profile Picture",
                            modifier = Modifier
                                .size(100.dp)
                                .clip(CircleShape)
                                .align(Alignment.CenterHorizontally)
                        )

                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { launcher.launch("image/*") },
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        ) {
                            Text("Change Profile Picture")
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        if (editingName) {
                            UserInputField(label = "First Name", value = firstName, onValueChange = { firstName = it })
                            Spacer(modifier = Modifier.height(12.dp))
                            UserInputField(label = "Last Name", value = lastName, onValueChange = { lastName = it })
                            Spacer(modifier = Modifier.height(24.dp))

                            Button(
                                onClick = {
                                    val updatedFirstName = if (firstName.isBlank()) user.firstName else firstName
                                    val updatedLastName = if (lastName.isBlank()) user.lastName else lastName
                                    userViewModel.updateUser(updatedFirstName, updatedLastName)
                                    coroutineScope.launch {
                                        snackbarHostState.showSnackbar("Profile updated successfully")
                                    }
                                    editingName = false
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Save Name")
                            }
                        } else {
                            Text(text = "Name: ${user.firstName} ${user.lastName}")
                            Button(
                                onClick = {
                                    editingName = true
                                    firstName = ""
                                    lastName = ""
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Edit Name")
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        if (editingPassword) {
                            UserInputField(label = "Current Password", value = currentPassword, onValueChange = { currentPassword = it }, isPassword = true)
                            Spacer(modifier = Modifier.height(12.dp))
                            UserInputField(label = "New Password", value = newPassword, onValueChange = { newPassword = it }, isPassword = true)
                            Spacer(modifier = Modifier.height(12.dp))
                            UserInputField(label = "Confirm Password", value = confirmPassword, onValueChange = { confirmPassword = it }, isPassword = true)
                            Spacer(modifier = Modifier.height(24.dp))

                            Button(
                                onClick = {
                                    if (newPassword == confirmPassword) {
                                        userViewModel.changePassword(currentPassword, newPassword) { result ->
                                            coroutineScope.launch {
                                                if (result.isSuccess) {
                                                    snackbarHostState.showSnackbar("Password changed successfully")
                                                    editingPassword = false
                                                } else {
                                                    val errorMessage = userViewModel.changePasswordError.value ?: "Wrong current password!"
                                                    snackbarHostState.showSnackbar(errorMessage)
                                                }
                                            }
                                        }
                                    } else {
                                        coroutineScope.launch {
                                            snackbarHostState.showSnackbar("New passwords do not match")
                                        }
                                    }
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Save Password")
                            }

                            userViewModel.changePasswordError.collectAsState().value?.let {
                                Spacer(modifier = Modifier.height(24.dp))
                                Text(
                                    text = it,
                                    color = MaterialTheme.colorScheme.error,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        } else {
                            Button(
                                onClick = { editingPassword = true },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Change Password")
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        Text(text = "Username: ${user.username}")
                        Text(text = "Email: ${user.email}")
                        Text(text = "Points: ${user.points}")

                        Spacer(modifier = Modifier.height(24.dp))

                        Text(text = "Notification Radius (meters)")
                        TextField(
                            value = radius.toString(),
                            onValueChange = {
                                it.toIntOrNull()?.let { newRadius ->
                                    radius = newRadius
                                    settingsManager.saveRadius(newRadius)
                                }
                            },
                            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(text = "Enable Notifications")
                        Switch(
                            checked = notificationsEnabled,
                            onCheckedChange = {
                                notificationsEnabled = it
                                settingsManager.setNotificationsEnabled(it)
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun UserInputField(label: String, value: String, onValueChange: (String) -> Unit, isPassword: Boolean = false) {
    Column {
        Text(label)
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            visualTransformation = if (isPassword) PasswordVisualTransformation() else VisualTransformation.None,
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .border(1.dp, MaterialTheme.colorScheme.primary, CircleShape)
                .padding(16.dp)
        )
    }
}






