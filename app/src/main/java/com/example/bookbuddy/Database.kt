package com.example.bookbuddy

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlin.math.sqrt
import kotlin.random.Random
import java.util.UUID

// In-memory database refreshed by Google Books API
object BookBuddyDatabase {
    private val users = mutableMapOf<String, UserProfile>()
    private val books = mutableMapOf<String, BookWithCategory>()
    private val collections = mutableMapOf<String, BookCollection>()
    private val userRatings = mutableMapOf<String, MutableMap<String, Float>>()
    private var currentUser: UserProfile? = null
    private var remoteInitialized = false

    private val booksState = MutableStateFlow<List<BookWithCategory>>(emptyList())
    private val collectionState = MutableStateFlow<List<BookCollection>>(emptyList())
    private val reviewsState = MutableStateFlow<List<Review>>(emptyList())
    private val recommendationsState = MutableStateFlow<List<BookWithCategory>>(emptyList())
    private val isFetchingState = MutableStateFlow(false)

    // region Observers
    fun observeBooks(): StateFlow<List<BookWithCategory>> = booksState
    fun observeCollections(): StateFlow<List<BookCollection>> = collectionState
    fun observeReviews(): StateFlow<List<Review>> = reviewsState
    fun observeRecommendations(): StateFlow<List<BookWithCategory>> = recommendationsState
    fun observeIsFetchingBooks(): StateFlow<Boolean> = isFetchingState
    // endregion

    // region User Management
    fun registerUser(userProfile: UserProfile): Boolean {
        return if (users.containsKey(userProfile.email)) {
            false
        } else {
            users[userProfile.email] = userProfile
            currentUser = userProfile
            true
        }
    }

    fun loginUser(email: String): UserProfile? {
        val user = users[email]
        currentUser = user
        return user
    }

    fun getCurrentUser(): UserProfile? = currentUser

    fun updateUserProfile(updatedProfile: UserProfile): Boolean {
        return if (currentUser != null) {
            users[currentUser!!.email] = updatedProfile
            currentUser = updatedProfile
            true
        } else {
            false
        }
    }

    fun logout() {
        currentUser = null
    }
    // endregion

    // region Book Management
    fun addBook(book: BookWithCategory): Boolean {
        return if (currentUser != null) {
            books[book.id] = book
            publishBooks()
            true
        } else {
            false
        }
    }

    fun deleteBook(bookId: String): Boolean {
        return if (currentUser != null && books.containsKey(bookId)) {
            books.remove(bookId)
            publishBooks()
            removeRatingsForBook(bookId)
            true
        } else {
            false
        }
    }

    fun getUserBooks(): List<BookWithCategory> = booksState.value
    fun getAllBooks(): List<BookWithCategory> = booksState.value

    fun searchBooks(query: String): List<BookWithCategory> {
        val lowerQuery = query.lowercase()
        return booksState.value.filter { book ->
            book.title.lowercase().contains(lowerQuery) ||
                book.author.lowercase().contains(lowerQuery) ||
                book.categories.any { it.lowercase().contains(lowerQuery) }
        }
    }

    fun getBooksByCategory(category: String): List<BookWithCategory> {
        return booksState.value.filter { book ->
            book.categories.any { it.equals(category, ignoreCase = true) }
        }
    }
    // endregion

    // region Collection Management
    fun createCollection(collection: BookCollection): Boolean {
        return if (currentUser != null) {
            collections[collection.title] = collection
            publishCollections()
            true
        } else {
            false
        }
    }

    fun getUserCollections(): List<BookCollection> = collectionState.value

    fun addBookToCollection(collectionTitle: String, book: BookWithCategory): Boolean {
        return if (currentUser != null && collections.containsKey(collectionTitle)) {
            val collection = collections[collectionTitle]!!
            val updatedBooks = collection.books + Book(book.title, book.author)
            collections[collectionTitle] = collection.copy(books = updatedBooks)
            publishCollections()
            true
        } else {
            false
        }
    }

    fun removeBookFromCollection(collectionTitle: String, bookTitle: String): Boolean {
        return if (currentUser != null && collections.containsKey(collectionTitle)) {
            val collection = collections[collectionTitle]!!
            val updatedBooks = collection.books.filter { it.title != bookTitle }
            collections[collectionTitle] = collection.copy(books = updatedBooks)
            publishCollections()
            true
        } else {
            false
        }
    }
    // endregion

    // region Reviews
    fun createReview(bookId: String, rating: Int, content: String): Review? {
        val user = currentUser ?: return null
        if (content.isBlank()) return null
        val existing = reviewsState.value.find { it.bookId == bookId && it.userId == user.userId }
        if (existing != null) {
            updateReview(existing.id, rating, content)
            return existing
        }
        val review = Review(
            id = UUID.randomUUID().toString(),
            bookId = bookId,
            userId = user.userId,
            username = user.displayName.ifBlank { user.username },
            rating = rating.coerceIn(1, 5),
            content = content.trim()
        )
        reviewsState.value = reviewsState.value + review
        updateUserRating(user.userId, bookId, review.rating.toFloat())
        return review
    }

    fun updateReview(reviewId: String, rating: Int, content: String): Boolean {
        val user = currentUser ?: return false
        var updated = false
        val revised = reviewsState.value.map { review ->
            if (review.id == reviewId && review.userId == user.userId) {
                updated = true
                review.copy(
                    rating = rating.coerceIn(1, 5),
                    content = content.trim(),
                    updatedAt = System.currentTimeMillis()
                )
            } else {
                review
            }
        }
        if (updated) {
            reviewsState.value = revised
            val target = revised.find { it.id == reviewId }
            if (target != null) {
                updateUserRating(user.userId, target.bookId, target.rating.toFloat())
            }
        }
        return updated
    }

    fun deleteReview(reviewId: String): Boolean {
        val user = currentUser ?: return false
        val existing = reviewsState.value.find { it.id == reviewId && it.userId == user.userId } ?: return false
        reviewsState.value = reviewsState.value.filterNot { it.id == reviewId }
        updateUserRating(user.userId, existing.bookId, null)
        return true
    }
    // endregion

    // region Collaborative Filtering
    fun getCollaborativeRecommendations(): List<BookWithCategory> = recommendationsState.value

    private fun updateRecommendations() {
        recommendationsState.value = computeRecommendations()
    }

    private fun computeRecommendations(): List<BookWithCategory> {
        val user = currentUser ?: return emptyList()
        val currentRatings = userRatings[user.userId] ?: return emptyList()
        if (currentRatings.isEmpty()) return emptyList()

        val similarityScores = mutableMapOf<String, Double>()
        userRatings.forEach { (otherUserId, otherRatings) ->
            if (otherUserId == user.userId) return@forEach
            val similarity = cosineSimilarity(currentRatings, otherRatings)
            if (similarity > 0) {
                similarityScores[otherUserId] = similarity
            }
        }

        if (similarityScores.isEmpty()) return emptyList()

        val bookScores = mutableMapOf<String, Double>()
        similarityScores.forEach { (otherUserId, similarity) ->
            val ratings = userRatings[otherUserId] ?: return@forEach
            ratings.forEach { (bookId, rating) ->
                if (!currentRatings.containsKey(bookId)) {
                    bookScores[bookId] = bookScores.getOrDefault(bookId, 0.0) + similarity * rating
                }
            }
        }

        return bookScores.entries
            .sortedByDescending { it.value }
            .mapNotNull { books[it.key] }
            .take(10)
    }

    private fun cosineSimilarity(
        a: Map<String, Float>,
        b: Map<String, Float>
    ): Double {
        var numerator = 0.0
        var sumASq = 0.0
        var sumBSq = 0.0

        a.forEach { (bookId, ratingA) ->
            val ratingB = b[bookId]
            sumASq += ratingA * ratingA
            if (ratingB != null) {
                numerator += ratingA * ratingB
            }
        }

        b.forEach { (_, ratingB) ->
            sumBSq += ratingB * ratingB
        }

        val denominator = sqrt(sumASq) * sqrt(sumBSq)
        return if (denominator == 0.0) 0.0 else numerator / denominator
    }
    // endregion

    // region Initialization & Remote Sync
    fun initializeSampleData() {
        if (users.isNotEmpty()) return
        val sampleUser = UserProfile(
            userId = "user1",
            username = "booklover",
            email = "user@example.com",
            displayName = "Book Lover",
            bio = "I love reading books!",
            favoriteGenres = listOf("Fiction", "Mystery", "Romance"),
            readingPreferences = ReadingPreferences(
                preferredGenres = listOf("Fiction", "Mystery"),
                readingGoal = ReadingGoal(targetBooksPerYear = 24, currentProgress = 5),
                notificationsEnabled = true,
                shareReadingActivity = true
            )
        )
        users[sampleUser.email] = sampleUser
        currentUser = sampleUser

        val sampleCollections = listOf(
            BookCollection("Favorites", emptyList()),
            BookCollection("To Read", emptyList()),
            BookCollection("Classics", emptyList())
        )
        sampleCollections.forEach { collections[it.title] = it }
        publishCollections()
    }

    suspend fun ensureRemoteBooksLoaded(force: Boolean = false) {
        if (remoteInitialized && !force) return
        isFetchingState.value = true
        val remoteBooks = try {
            GoogleBooksService.fetchBooks()
        } catch (ex: Exception) {
            emptyList()
        }

        if (remoteBooks.isNotEmpty()) {
            setBooks(remoteBooks)
            seedCollaborativeData(remoteBooks)
        } else if (books.isEmpty()) {
            setBooks(defaultFallbackBooks())
            seedCollaborativeData(booksState.value)
        }

        remoteInitialized = true
        isFetchingState.value = false
    }
    // endregion

    // region Helpers
    private fun publishBooks() {
        booksState.value = books.values.toList()
        updateRecommendations()
    }

    private fun publishCollections() {
        collectionState.value = collections.values.toList()
    }

    private fun setBooks(remoteBooks: List<BookWithCategory>) {
        books.clear()
        remoteBooks.forEach { book ->
            books[book.id] = book
        }
        publishBooks()
    }

    private fun updateUserRating(userId: String, bookId: String, rating: Float?) {
        val ratings = userRatings.getOrPut(userId) { mutableMapOf() }
        if (rating == null) {
            ratings.remove(bookId)
        } else {
            ratings[bookId] = rating
        }
        updateRecommendations()
    }

    private fun removeRatingsForBook(bookId: String) {
        userRatings.values.forEach { it.remove(bookId) }
        updateRecommendations()
    }

    private fun seedCollaborativeData(sourceBooks: List<BookWithCategory>) {
        if (sourceBooks.isEmpty()) return
        val sampleMembers = listOf(
            UserProfile(userId = "user2", username = "mysteryfan", email = "mystery@example.com", displayName = "Mystery Fan"),
            UserProfile(userId = "user3", username = "romancelover", email = "romance@example.com", displayName = "Romance Lover")
        )
        sampleMembers.forEach { member ->
            users.putIfAbsent(member.email, member)
            val picks = sourceBooks.shuffled().take(5)
            val ratings = userRatings.getOrPut(member.userId) { mutableMapOf() }
            picks.forEach { book ->
                val score = Random.nextInt(3, 5).toFloat()
                ratings[book.id] = score
                if (reviewsState.value.none { it.userId == member.userId && it.bookId == book.id }) {
                    val review = Review(
                        id = UUID.randomUUID().toString(),
                        bookId = book.id,
                        userId = member.userId,
                        username = member.displayName.ifBlank { member.username },
                        rating = score.toInt(),
                        content = "I really enjoyed \"${book.title}\" – worth the read!"
                    )
                    reviewsState.value = reviewsState.value + review
                }
            }
        }
        updateRecommendations()
    }

    private fun defaultFallbackBooks(): List<BookWithCategory> = listOf(
        BookWithCategory(
            id = "book1",
            title = "Of Mice and Men",
            author = "John Steinbeck",
            categories = listOf("Fiction", "Classics"),
            description = "A classic American novel about friendship and dreams.",
            publishedYear = 1937,
            rating = 4.5f,
            pageCount = 107
        ),
        BookWithCategory(
            id = "book2",
            title = "The Housemaid",
            author = "Freida McFadden",
            categories = listOf("Thriller", "Mystery"),
            description = "A psychological thriller about a housemaid's dark secrets.",
            publishedYear = 2022,
            rating = 4.2f,
            pageCount = 320
        ),
        BookWithCategory(
            id = "book3",
            title = "To Kill a Mockingbird",
            author = "Harper Lee",
            categories = listOf("Fiction", "Classics"),
            description = "A powerful story of racial injustice and childhood innocence.",
            publishedYear = 1960,
            rating = 4.8f,
            pageCount = 281
        )
    )
    // endregion
}
