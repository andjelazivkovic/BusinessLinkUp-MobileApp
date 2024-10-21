package com.example.businesslinkup.components

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.businesslinkup.screen.*
import com.example.businesslinkup.viewModels.BusinessObjectViewModel
import com.example.businesslinkup.viewModels.UserViewModel

@Composable
fun AppNavigation(
    navController: NavHostController,
    userViewModel: UserViewModel,
    businessObjectViewModel: BusinessObjectViewModel
) {
    val isUserLoggedIn = userViewModel.isUserLoggedIn()

    NavHost(
        navController = navController,
        startDestination = if (isUserLoggedIn) "profile" else "login"
    ) {
        composable("login") {
            LoginScreen(
                navController = navController,
                viewModel = userViewModel,
                onRegisterClick = { navController.navigate("register") }
            )
        }
        composable("register") {
            RegisterScreen(
                navController = navController,
                viewModel = userViewModel
            )
        }
        composable("profile") {
            ProfileScreen(
                userViewModel = userViewModel,
                navController = navController
            )
        }
        composable("add_object") {
            AddObjectScreen(
                viewModel = businessObjectViewModel,
                userViewModel = userViewModel
            )
        }
        composable("user_list") {
            UserListScreen(
                viewModel = userViewModel
            )
        }
        composable("business_object_detail/{objectId}") { backStackEntry ->
            val objectId = backStackEntry.arguments?.getString("objectId") ?: return@composable
            BusinessObjectDetailScreen(
                navController = navController,
                businessViewModel = businessObjectViewModel,
                userViewModel = userViewModel,
                objectId = objectId
            )
        }
        composable("map") {
            MapScreen(
                navController = navController,
                viewModel = businessObjectViewModel
            )
        }
    }
}

