package com.example.bookbuddy

// Book data models
data class Book(val title: String, val author: String)
data class BookCollection(val title: String, val books: List<Book>)

// Profile data model - Task 43: 1-1-1-1 Implement profile data model
data class UserProfile(
    val userId: String = "",
    val username: String = "",
    val email: String = "",
    val displayName: String = "",
    val bio: String = "",
    val profilePictureUrl: String = "",
    val readingPreferences: ReadingPreferences = ReadingPreferences(),
    val favoriteGenres: List<String> = emptyList(),
    val joinedDate: Long = System.currentTimeMillis(),
    val booksRead: Int = 0,
    val reviewsWritten: Int = 0,
    val followersCount: Int = 0,
    val followingCount: Int = 0,
    val isPublicProfile: Boolean = true,
    val location: String = "",
    val website: String = ""
)

// Reading preferences for user profile
data class ReadingPreferences(
    val preferredGenres: List<String> = emptyList(),
    val readingGoal: ReadingGoal = ReadingGoal(),
    val notificationsEnabled: Boolean = true,
    val shareReadingActivity: Boolean = true
)

// Reading goal tracking
data class ReadingGoal(
    val targetBooksPerYear: Int = 0,
    val currentProgress: Int = 0,
    val goalType: GoalType = GoalType.YEARLY
)

// Goal type enumeration
enum class GoalType {
    WEEKLY,
    MONTHLY,
    YEARLY
}

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

// Enhanced Book model with categorization
data class BookWithCategory(
    val id: String,
    val title: String,
    val author: String,
    val categories: List<String> = emptyList(),
    val coverImageUrl: String = "",
    val isbn: String = "",
    val description: String = "",
    val publishedYear: Int = 0,
    val rating: Float = 0f,
    val pageCount: Int = 0,
    val language: String = "English"
)
