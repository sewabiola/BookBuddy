package com.example.bookbuddy

import java.util.UUID


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
    val id: String = "",
    val title: String = "",
    val author: String = "",
    val categories: List<String> = emptyList(),
    val description: String = "",
    val publishedYear: Int = 0,
    val rating: Float = 0f,
    val pageCount: Int = 0,
    val coverImageUrl: String = "",
    val language: String = "",
    val isbn: String = "",
    val readingStatus: String = "Not Started"
)


data class BookCategoryStatics(
    val book: BookWithCategory,
    val readingStatus: ReadingStatus = ReadingStatus.NOT_STARTED
)

enum class ReadingStatus {
    NOT_STARTED,
    READING,
    FINISHED
}
data class BookCollection(
    val title: String,
    val books: List<Book>
)

data class ForumPost(
    val id: String = UUID.randomUUID().toString(),
    val authorId: String,
    val authorName: String,
    val title: String,
    val body: String,
    val createdAt: Long = System.currentTimeMillis(),
    val comments: MutableList<Comment> = mutableListOf(),
    var isDeleted: Boolean = false,
    var flags: Int = 0
)

data class Comment(
    val id: String = UUID.randomUUID().toString(),
    val authorId: String,
    val authorName: String,
    val body: String,
    val createdAt: Long = System.currentTimeMillis(),
    val replies: MutableList<Comment> = mutableListOf(),
    var isDeleted: Boolean = false,
    var flags: Int = 0
)

data class ModerationAction(
    val moderatorId: String,
    val targetId: String,
    val targetType: String, // "post" or "comment"
    val action: String, // "delete", "restore", "flag", "note"
    val note: String?,
    val timestamp: Long = System.currentTimeMillis()
)

// Club data models
data class Club(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val description: String,
    val ownerId: String,
    val coverImageUrl: String? = null,
    val genreTags: List<String> = emptyList(),
    val isPublic: Boolean = true,
    val rules: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val memberCount: Int = 1,
    val currentBookId: String? = null,
    val inviteCode: String? = null // Optional invite code for private clubs
)

data class ClubMember(
    val memberId: String = UUID.randomUUID().toString(),
    val clubId: String,
    val userId: String,
    val role: MemberRole = MemberRole.MEMBER,
    val joinedAt: Long = System.currentTimeMillis(),
    val status: MemberStatus = MemberStatus.ACTIVE
)

data class JoinRequest(
    val requestId: String = UUID.randomUUID().toString(),
    val clubId: String,
    val userId: String,
    val username: String,
    val requestedAt: Long = System.currentTimeMillis(),
    val message: String? = null
)

// Invite code for clubs
data class ClubInviteCode(
    val code: String,
    val clubId: String,
    val createdBy: String, // userId of creator
    val createdAt: Long = System.currentTimeMillis(),
    val expiresAt: Long? = null, // null = never expires
    val maxUses: Int? = null, // null = unlimited
    val currentUses: Int = 0
)

// Club-specific discussion post
data class ClubDiscussion(
    val id: String = UUID.randomUUID().toString(),
    val clubId: String,
    val authorId: String,
    val authorName: String,
    val title: String,
    val body: String,
    val createdAt: Long = System.currentTimeMillis(),
    val comments: MutableList<Comment> = mutableListOf(),
    var isDeleted: Boolean = false
)

// Club activity entry
data class ClubActivity(
    val id: String = UUID.randomUUID().toString(),
    val clubId: String,
    val type: ActivityType,
    val userId: String? = null,
    val username: String? = null,
    val bookId: String? = null,
    val bookTitle: String? = null,
    val discussionId: String? = null,
    val discussionTitle: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)

enum class ActivityType {
    MEMBER_JOINED,
    MEMBER_LEFT,
    BOOK_ADDED,
    BOOK_REMOVED,
    CURRENT_BOOK_CHANGED,
    DISCUSSION_CREATED,
    DISCUSSION_COMMENTED
}

enum class MemberRole {
    OWNER,
    ADMIN,
    MODERATOR,
    MEMBER
}

enum class MemberStatus {
    ACTIVE,
    PENDING,
    INVITED,
    BANNED
}

// Review & feedback model for tasks 63/64
data class Review(
    val id: String,
    val bookId: String,
    val userId: String,
    val username: String,
    val rating: Int,
    val content: String,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    val isEdited: Boolean get() = updatedAt - createdAt > 5_000
}
