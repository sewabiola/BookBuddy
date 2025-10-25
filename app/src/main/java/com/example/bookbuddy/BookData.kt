package com.example.bookbuddy

data class Book(
    val title: String,
    val author: String,
    var status: String = "Not Started"
)

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

data class ReadingPreferences(
    val preferredGenres: List<String> = emptyList(),
    val readingGoal: ReadingGoal = ReadingGoal(),
    val notificationsEnabled: Boolean = true,
    val shareReadingActivity: Boolean = false
)

data class ReadingGoal(
    val targetBooksPerYear: Int = 12,
    val currentProgress: Int = 0,
    val goalType: GoalType = GoalType.YEARLY
)

data class BookCollection(
    val title: String,
    val books: List<Book> = emptyList()
)

data class Collection(
    val name: String,
    val books: MutableList<Book> = mutableListOf()
)

// Goal type enumeration
enum class GoalType {
    WEEKLY,
    MONTHLY,
    YEARLY
}