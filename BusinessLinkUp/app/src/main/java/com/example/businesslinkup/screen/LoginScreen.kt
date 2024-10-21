package com.example.businesslinkup.screen

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.navigation.NavController
import com.example.businesslinkup.viewModels.UserViewModel
import com.example.businesslinkup.entities.User

@Composable
fun LoginScreen(
    navController: NavController,
    viewModel: UserViewModel,
    onRegisterClick: () -> Unit
) {
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    val loginResult by viewModel.userState.collectAsState()

    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        TextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        TextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = {
            viewModel.loginUser(email, password)
        }) {
            Text("Login")
        }

        LaunchedEffect(loginResult) {
            loginResult?.let {
                if (it.isSuccess) {
                    navController.navigate("profile") {
                        popUpTo("login") { inclusive = true }
                    }
                } else {
                    Toast.makeText(context, "Login failed: ${it.exceptionOrNull()?.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
        TextButton(onClick = onRegisterClick) {
            Text("Don't have an account? Register")
        }
    }
}

