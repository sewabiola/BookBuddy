package com.example.bookbuddy

// Book and Collection classes are defined in BookData.kt

// Registration data model for Task 41
data class RegistrationData(
    val username: String = "",
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val agreeToTerms: Boolean = false
)

// Validation state for registration
data class ValidationState(
    val isValid: Boolean = false,
    val errorMessage: String = ""
)

// Book category for categorization system - Task 54
data class BookCategory(
    val id: String,
    val name: String,
    val description: String = "",
    val iconResource: String = "",
    val booksCount: Int = 0,
    val subcategories: List<BookCategory> = emptyList()
)

// GoalType enum is defined in BookData.kt
