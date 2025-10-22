package com.example.bookbuddy

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp

// Task 41: 1-1-1-2 Create Registration Form
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegistrationScreen(
    onRegisterSuccess: () -> Unit,
    onNavigateToLogin: () -> Unit
) {
    var registrationData by remember { mutableStateOf(RegistrationData()) }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }
    var validationErrors by remember { mutableStateOf<Map<String, String>>(emptyMap()) }
    var isLoading by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Create Account") },
                navigationIcon = {
                    IconButton(onClick = onNavigateToLogin) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back to Login")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(32.dp))

            // Welcome Text
            Text(
                text = "Join BookBuddy",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "Share your love of books with fellow enthusiasts",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Username Field
            OutlinedTextField(
                value = registrationData.username,
                onValueChange = {
                    registrationData = registrationData.copy(username = it)
                    validationErrors = validationErrors - "username"
                },
                label = { Text("Username") },
                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                isError = validationErrors.containsKey("username"),
                supportingText = { validationErrors["username"]?.let { Text(it) } },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Email Field
            OutlinedTextField(
                value = registrationData.email,
                onValueChange = {
                    registrationData = registrationData.copy(email = it)
                    validationErrors = validationErrors - "email"
                },
                label = { Text("Email") },
                leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                isError = validationErrors.containsKey("email"),
                supportingText = { validationErrors["email"]?.let { Text(it) } },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Password Field
            OutlinedTextField(
                value = registrationData.password,
                onValueChange = {
                    registrationData = registrationData.copy(password = it)
                    validationErrors = validationErrors - "password"
                },
                label = { Text("Password") },
                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            if (passwordVisible) Icons.Default.Add else Icons.Default.Close,
                            contentDescription = if (passwordVisible) "Hide password" else "Show password"
                        )
                    }
                },
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                isError = validationErrors.containsKey("password"),
                supportingText = { validationErrors["password"]?.let { Text(it) } },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Confirm Password Field
            OutlinedTextField(
                value = registrationData.confirmPassword,
                onValueChange = {
                    registrationData = registrationData.copy(confirmPassword = it)
                    validationErrors = validationErrors - "confirmPassword"
                },
                label = { Text("Confirm Password") },
                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                trailingIcon = {
                    IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                        Icon(
                            if (confirmPasswordVisible) Icons.Default.Add else Icons.Default.Close,
                            contentDescription = if (confirmPasswordVisible) "Hide password" else "Show password"
                        )
                    }
                },
                visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                isError = validationErrors.containsKey("confirmPassword"),
                supportingText = { validationErrors["confirmPassword"]?.let { Text(it) } },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Terms and Conditions Checkbox
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = registrationData.agreeToTerms,
                    onCheckedChange = {
                        registrationData = registrationData.copy(agreeToTerms = it)
                        validationErrors = validationErrors - "terms"
                    }
                )
                Text(
                    text = "I agree to the Terms of Service and Privacy Policy",
                    style = MaterialTheme.typography.bodySmall
                )
            }
            if (validationErrors.containsKey("terms")) {
                Text(
                    text = validationErrors["terms"] ?: "",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(start = 48.dp)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Register Button
            Button(
                onClick = {
                    val errors = validateRegistrationData(registrationData)
                    if (errors.isEmpty()) {
                        isLoading = true
                        val repository = UserRepository()
                        val success = repository.register(registrationData)
                        isLoading = false
                        if (success) {
                            onRegisterSuccess()
                        } else {
                            validationErrors = mapOf("email" to "An account with this email already exists")
                        }
                    } else {
                        validationErrors = errors
                    }

                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Create Account")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Login Link
            TextButton(onClick = onNavigateToLogin) {
                Text("Already have an account? Sign in")
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

// Validation helper function
fun validateRegistrationData(data: RegistrationData): Map<String, String> {
    val errors = mutableMapOf<String, String>()

    // Username validation
    when {
        data.username.isBlank() -> errors["username"] = "Username is required"
        data.username.length < 3 -> errors["username"] = "Username must be at least 3 characters"
        data.username.length > 20 -> errors["username"] = "Username must be less than 20 characters"
        !data.username.matches(Regex("^[a-zA-Z0-9_]+$")) ->
            errors["username"] = "Username can only contain letters, numbers, and underscores"
    }

    // Email validation
    when {
        data.email.isBlank() -> errors["email"] = "Email is required"
        !android.util.Patterns.EMAIL_ADDRESS.matcher(data.email).matches() ->
            errors["email"] = "Please enter a valid email address"
    }

    // Password validation
    when {
        data.password.isBlank() -> errors["password"] = "Password is required"
        data.password.length < 8 -> errors["password"] = "Password must be at least 8 characters"
        !data.password.any { it.isUpperCase() } ->
            errors["password"] = "Password must contain at least one uppercase letter"
        !data.password.any { it.isLowerCase() } ->
            errors["password"] = "Password must contain at least one lowercase letter"
        !data.password.any { it.isDigit() } ->
            errors["password"] = "Password must contain at least one number"
    }

    // Confirm password validation
    when {
        data.confirmPassword.isBlank() -> errors["confirmPassword"] = "Please confirm your password"
        data.password != data.confirmPassword ->
            errors["confirmPassword"] = "Passwords do not match"
    }

    // Terms validation
    if (!data.agreeToTerms) {
        errors["terms"] = "You must agree to the terms to continue"
    }

    return errors
}
