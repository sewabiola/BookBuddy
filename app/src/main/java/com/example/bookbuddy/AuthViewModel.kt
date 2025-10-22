package com.example.bookbuddy

import androidx.lifecycle.ViewModel
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue

/**
 * AuthViewModel
 * -------------------------------
 * Sprint 1-2-1 Tasks:
 * - (1-2-1-1) Implemented login form and validation
 * - (1-2-1-2) Implemented password reset flow
 * - (1-2-1-3) Implemented logout functionality
 *
 * This ViewModel handles the user authentication logic,
 * interacting with UserRepository for login, logout, and password reset operations.
 */

class AuthViewModel : ViewModel() {

    // Repository layer handling user authentication logic
    private val repository = UserRepository()

    // Mutable state properties observed by the UI (Compose)
    var email by mutableStateOf("")
    var password by mutableStateOf("")
    var isLoggedIn by mutableStateOf(false)
    var loginError by mutableStateOf<String?>(null)

    /**
     * (1-2-1-1) Login function with basic validation.
     * Calls repository.login() to verify credentials.
     * Updates UI states depending on success or failure.
     */
    fun login() {
        val success = repository.login(email, password)
        if (success) {
            isLoggedIn = true
            loginError = null
        } else {
            loginError = "Invalid email or password"
        }
    }

    // (1-2-1-3) Logout function.

    fun logout() {
        repository.logout()
        isLoggedIn = false
    }

    /**
     * (1-2-1-2) Reset password function.
     * Simulates sending a reset link to the user email.
     * Returns a boolean indicating success/failure.
     */
    fun resetPassword(email: String): Boolean {
        return repository.resetPassword(email)
    }
}
