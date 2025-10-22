package com.example.bookbuddy

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp

// Task 1-2-1-1: Implement login form and validation.

@Composable
fun LoginScreen(
    viewModel: AuthViewModel,
    onLoginSuccess: () -> Unit,
    onForgotPassword: () -> Unit
) {
    // Automatically navigate to next screen if login is successful
    if (viewModel.isLoggedIn) {
        onLoginSuccess()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("BookBuddy Login", style = MaterialTheme.typography.headlineMedium)

        Spacer(Modifier.height(20.dp))

        // Email input field
        OutlinedTextField(
            value = viewModel.email,
            onValueChange = { viewModel.email = it },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth()
        )

        // Password input field
        OutlinedTextField(
            value = viewModel.password,
            onValueChange = { viewModel.password = it },
            label = { Text("Password") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(16.dp))

        // Login button
        Button(
            onClick = {
                // Simple validation before login
                if (viewModel.email.isBlank() || viewModel.password.isBlank()) {
                    viewModel.loginError = "Please enter both email and password"
                } else {
                    viewModel.login()
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Login")
        }

        // Display error message if login fails
        if (viewModel.loginError != null) {
            Spacer(Modifier.height(8.dp))
            Text(viewModel.loginError!!, color = MaterialTheme.colorScheme.error)
        }

        // Navigate to reset password screen
        TextButton(onClick = onForgotPassword) {
            Text("Forgot Password?")
        }
    }
}
