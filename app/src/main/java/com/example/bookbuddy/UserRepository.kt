package com.example.bookbuddy

/**
 * UserRepository. It simulates a local user database
 * and provides methods for authentication and basic user management.
 *
 * Tasks:
 * - (1-2-1-1) Implemented login verification logic
 * - (1-2-1-2) Implemented password reset simulation
 * - (1-2-1-3) Implemented logout functionality
 *
 */

class UserRepository {

    // Simulating a local database with a pre-registered user
    private val users = mutableListOf(
        RegistrationData(username = "Loren", email = "loren@test.com", password = "1234")
    )

    // Holds the current logged-in user information
    private var loggedInUser: UserProfile? = null

    // Simulates user login validation.

    fun login(email: String, password: String): Boolean {
        val user = users.find { it.email == email && it.password == password }
        return if (user != null) {
            loggedInUser = UserProfile(
                userId = user.email,
                username = user.username,
                email = user.email,
                displayName = user.username
            )
            true
        } else {
            false
        }
    }

    // Logs out the current user
    fun logout() {
        loggedInUser = null
    }

    //Simulates password reset by checking if the email exists.
    fun resetPassword(email: String): Boolean {
        return users.any { it.email == email }
    }

    // Returns the currently logged-in user information.
    fun getLoggedInUser(): UserProfile? = loggedInUser


    fun register(data: RegistrationData): Boolean {
        if (users.any { it.email == data.email }) return false
        users.add(data)
        return true
    }

}
