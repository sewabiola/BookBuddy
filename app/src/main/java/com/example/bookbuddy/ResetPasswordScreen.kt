package com.example.bookbuddy

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

// Task 2-1-2: Implement password reset flow.


@Composable
fun ResetPasswordScreen(viewModel: AuthViewModel, onBackToLogin: () -> Unit) {
    var email by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Reset Password", style = MaterialTheme.typography.headlineMedium)

        Spacer(Modifier.height(20.dp))

        // Email input field
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(16.dp))

        // It simulates sending a reset link button
        Button(
            onClick = {
                val success = viewModel.resetPassword(email)
                message = if (success)
                    "Password reset link sent!"
                else
                    "Email not found."
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Send Reset Link")
        }

        Spacer(Modifier.height(8.dp))

        // Back navigation to login
        TextButton(onClick = onBackToLogin) {
            Text("Back to Login")
        }

        // Display result message
        if (message.isNotEmpty()) {
            Text(message)
        }
    }
}
