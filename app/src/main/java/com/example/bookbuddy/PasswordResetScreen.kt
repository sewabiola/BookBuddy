
package com.example.bookbuddy

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PasswordResetScreen(
    onBack: () -> Unit,
    onResetComplete: () -> Unit
) {
    var step by remember { mutableStateOf(1) }
    var email by remember { mutableStateOf("") }
    var token by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirm by remember { mutableStateOf("") }
    var info by remember { mutableStateOf<String?>(null) }
    var error by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        step = 1
        email = ""
        token = ""
        newPassword = ""
        confirm = ""
        info = null
        error = null
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Password Reset") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Text("◀") }
                }
            )
        }
    ) { padding ->
        Column(
            Modifier
                .padding(padding)
                .padding(24.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.Top
        ) {
            when (step) {
                1 -> {
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it; error = null; info = null },
                        label = { Text("Email") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(16.dp))
                    Button(
                        onClick = {
                            val t = BookBuddyDatabase.requestPasswordReset(email)
                            if (t == null) {
                                error = "Email not found"
                                info = null
                            } else {
                                token = t
                                info = "Token (demo): $t"
                                error = null
                                step = 2
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("Generate token") }

                    if (info != null) {
                        Spacer(Modifier.height(8.dp))
                        Text(info!!, color = MaterialTheme.colorScheme.primary)
                    }
                    if (error != null) {
                        Spacer(Modifier.height(8.dp))
                        Text(error!!, color = MaterialTheme.colorScheme.error)
                    }
                }

                2 -> {
                    if (info != null) {
                        Text(info!!, color = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.height(8.dp))
                    }

                    OutlinedTextField(
                        value = token,
                        onValueChange = { token = it; error = null },
                        label = { Text("Token") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(12.dp))
                    OutlinedTextField(
                        value = newPassword,
                        onValueChange = { newPassword = it; error = null },
                        label = { Text("New password") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(12.dp))
                    OutlinedTextField(
                        value = confirm,
                        onValueChange = { confirm = it; error = null },
                        label = { Text("Confirm password") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(16.dp))
                    Button(
                        onClick = {
                            when {
                                newPassword.length < 6 -> error = "Password must be at least 6 characters"
                                newPassword != confirm -> error = "Passwords do not match"
                                else -> {
                                    val ok = BookBuddyDatabase.resetPassword(email, token, newPassword)
                                    if (ok) onResetComplete() else error = "Invalid token"
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("Reset password") }

                    if (error != null) {
                        Spacer(Modifier.height(8.dp))
                        Text(error!!, color = MaterialTheme.colorScheme.error)
                    }
                }
            }
        }
    }
}

