package com.example.businesslinkup.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DrawerState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.businesslinkup.viewModels.UserViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Composable
fun DrawerContent(
    navController: NavController,
    coroutineScope: CoroutineScope,
    drawerState: DrawerState,
    userViewModel: UserViewModel
) {
    Column(
        modifier = Modifier
            .background(MaterialTheme.colorScheme.background)
            .fillMaxSize()
    ) {
        Text(
            text = "Map",
            modifier = Modifier
                .clickable {
                    coroutineScope.launch {
                        drawerState.close()
                        navController.navigate("map")
                    }
                }
                .padding(16.dp)
        )
        Text(
            text = "Add Business Object",
            modifier = Modifier
                .clickable {
                    coroutineScope.launch {
                        drawerState.close()
                        navController.navigate("add_object")
                    }
                }
                .padding(16.dp)
        )
        Text(
            text = "User List",
            modifier = Modifier
                .clickable {
                    coroutineScope.launch {
                        drawerState.close()
                        navController.navigate("user_list")
                    }
                }
                .padding(16.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Log Out",
            modifier = Modifier
                .clickable {
                    coroutineScope.launch {
                        userViewModel.signOut { result ->
                            if (result.isSuccess) {
                                coroutineScope.launch {
                                    drawerState.close()
                                    println("Navigating to login screen")
                                    navController.navigate("login") {
                                        popUpTo(navController.graph.startDestinationId) { inclusive = true }
                                        launchSingleTop = true
                                    }
                                    println("Navigated to login screen")
                                }
                            } else {
                                val errorMessage = result.exceptionOrNull()?.localizedMessage ?: "Failed to sign out"
                                println(errorMessage)
                            }
                        }

                    }
                }
                .padding(16.dp),
            color = MaterialTheme.colorScheme.error
        )
    }
}
