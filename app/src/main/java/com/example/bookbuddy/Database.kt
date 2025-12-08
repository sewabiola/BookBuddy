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
    private val passwords = mutableMapOf<String, String>()  // Loren's password storage
    private val resetTokens = mutableMapOf<String, String>()  // Loren's password reset tokens
    private val reviewVotes: MutableMap<String, MutableMap<String, Int>> = mutableMapOf()  // Loren's review voting
    private var currentUser: UserProfile? = null
    private var remoteInitialized = false

    // Club storage
    private val clubs = mutableMapOf<String, Club>()
    private val clubMembers = mutableMapOf<String, MutableList<ClubMember>>()
    private val joinRequests = mutableMapOf<String, MutableList<JoinRequest>>()
    private val clubBooks = mutableMapOf<String, MutableList<String>>()  // clubId -> list of bookIds
    private val bannedMembers = mutableMapOf<String, MutableSet<String>>()  // clubId -> set of userIds
    private val inviteCodes = mutableMapOf<String, ClubInviteCode>()  // code -> ClubInviteCode
    private val clubDiscussions = mutableMapOf<String, MutableList<ClubDiscussion>>()  // clubId -> discussions
    private val clubActivities = mutableMapOf<String, MutableList<ClubActivity>>()  // clubId -> activities

    private val booksState = MutableStateFlow<List<BookWithCategory>>(emptyList())
    private val collectionState = MutableStateFlow<List<BookCollection>>(emptyList())
    private val reviewsState = MutableStateFlow<List<Review>>(emptyList())
    private val recommendationsState = MutableStateFlow<List<BookWithCategory>>(emptyList())
    private val isFetchingState = MutableStateFlow(false)
    private val clubsState = MutableStateFlow<List<Club>>(emptyList())
    private val userClubsState = MutableStateFlow<List<Club>>(emptyList())

    // region Observers
    fun observeBooks(): StateFlow<List<BookWithCategory>> = booksState
    fun observeCollections(): StateFlow<List<BookCollection>> = collectionState
    fun observeReviews(): StateFlow<List<Review>> = reviewsState
    fun observeRecommendations(): StateFlow<List<BookWithCategory>> = recommendationsState
    fun observeIsFetchingBooks(): StateFlow<Boolean> = isFetchingState
    fun observeClubs(): StateFlow<List<Club>> = clubsState
    fun observeUserClubs(): StateFlow<List<Club>> = userClubsState
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

    // Loren's password registration
    fun registerUser(userProfile: UserProfile, password: String): Boolean {
        val ok = registerUser(userProfile)
        if (ok) passwords[userProfile.email] = password
        return ok
    }

    // Loren's login with password validation
    fun validateLogin(email: String, password: String): UserProfile? {
        val user = users[email]
        if (user != null && passwords[email] == password) {
            currentUser = user
            return user
        }
        return null
    }

    // Loren's password reset functionality (demo)
    fun requestPasswordReset(email: String): String? {
        if (!users.containsKey(email)) return null
        val token = (100000..999999).random().toString()
        resetTokens[email] = token
        return token
    }

    fun resetPassword(email: String, token: String, newPassword: String): Boolean {
        val expected = resetTokens[email] ?: return false
        if (expected != token) return false
        passwords[email] = newPassword
        resetTokens.remove(email)
        return true
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

    fun getBookById(bookId: String): BookWithCategory? {
        return getAllBooks().find { it.id == bookId }
    }

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

    fun updateCollection(oldTitle: String, updatedCollection: BookCollection): Boolean {
        if (!collections.containsKey(oldTitle)) return false

        if (oldTitle != updatedCollection.title) {
            collections.remove(oldTitle)
        }

        collections[updatedCollection.title] = updatedCollection
        publishCollections()
        return true
    }

    fun deleteCollection(collection: BookCollection): Boolean {
        return if (collections.containsKey(collection.title)) {
            collections.remove(collection.title)
            publishCollections()
            true
        } else {
            false
        }
    }

    fun addBookToCollection(collectionTitle: String, book: BookWithCategory): Boolean {
        return if (currentUser != null && collections.containsKey(collectionTitle)) {
            val collection = collections[collectionTitle]!!
            val updatedBooks = collection.books + Book(
                id = book.id,
                title = book.title,
                author = book.author,
                description = book.description,
                category = book.categories.firstOrNull() ?: "",
                coverImageUrl = book.coverImageUrl,
                readingStatus = book.readingStatus
            )
            collections[collectionTitle] = collection.copy(books = updatedBooks)
            publishCollections()
            true
        } else {
            false
        }
    }

    fun removeBookFromCollection(collectionTitle: String, bookId: String): Boolean {
        return if (currentUser != null && collections.containsKey(collectionTitle)) {
            val collection = collections[collectionTitle]!!
            val updatedBooks = collection.books.filter { it.id != bookId }
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

    // Loren's review voting functionality
    fun voteReview(reviewId: String, userId: String, upvote: Boolean) {
        if (userId.isBlank()) return
        val votesByUser = reviewVotes.getOrPut(reviewId) { mutableMapOf() }
        val newVote = if (upvote) 1 else -1

        if (votesByUser[userId] == newVote) {
            votesByUser.remove(userId)
        } else {
            votesByUser[userId] = newVote
        }
    }

    fun getReviewVotes(reviewId: String): Pair<Int, Int> {
        val votes = reviewVotes[reviewId]?.values ?: emptyList()
        val up = votes.count { it == 1 }
        val down = votes.count { it == -1 }
        return up to down
    }

    fun getUserVoteForReview(reviewId: String, userId: String): Int {
        return reviewVotes[reviewId]?.get(userId) ?: 0
    }

    // LocYenDan's BookReview functions
    private val bookReviews = mutableListOf<BookReview>()

    fun addReview(review: BookReview) {
        val exists = bookReviews.any {
            it.bookId == review.bookId &&
                it.comment == review.comment &&
                it.rating == review.rating &&
                it.username == review.username
        }
        if (!exists) {
            bookReviews.add(review)
        }
    }

    fun getReviewsForBook(bookId: String): List<BookReview> {
        return bookReviews.filter { it.bookId == bookId }
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
        passwords[sampleUser.email] = "password123"  // Default password for demo

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
        // Sync reading status from map to book objects before publishing
        val updatedBooks = books.values.map { book ->
            val status = readingStatusMap[book.id]
            if (status != null && book.readingStatus != status) {
                book.copy(readingStatus = status)
            } else {
                book
            }
        }
        booksState.value = updatedBooks
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

    // LocYenDan's Reading Progress Tracking
    private val readingStatusMap = mutableMapOf<String, String>()

    fun setReadingStatus(bookId: String, status: String) {
        readingStatusMap[bookId] = status

        // Also update the book object itself to keep it in sync
        val book = books[bookId]
        if (book != null) {
            books[bookId] = book.copy(readingStatus = status)
            publishBooks()
        }
    }

    fun getReadingStatus(bookId: String): String {
        return readingStatusMap[bookId] ?: books[bookId]?.readingStatus ?: "Not Started"
    }

    fun getReadingStatistics(): ReadingStatistics {
        val totalBooks = getAllBooks().size
        val notStarted = getAllBooks().count { getReadingStatus(it.id) == "Not Started" }
        val reading = getAllBooks().count { getReadingStatus(it.id) == "Reading" }
        val read = getAllBooks().count { getReadingStatus(it.id) == "Read" }

        return ReadingStatistics(
            totalBooks = totalBooks,
            notStarted = notStarted,
            reading = reading,
            read = read
        )
    }

    // region Club Management
    fun createClub(club: Club): String? {
        val user = currentUser ?: return null
        val newClub = club.copy(ownerId = user.userId)
        clubs[newClub.id] = newClub

        // Add owner as first member
        val ownerMember = ClubMember(
            clubId = newClub.id,
            userId = user.userId,
            role = MemberRole.OWNER,
            status = MemberStatus.ACTIVE
        )
        clubMembers.getOrPut(newClub.id) { mutableListOf() }.add(ownerMember)
        clubBooks[newClub.id] = mutableListOf()

        publishClubs()
        return newClub.id
    }

    fun updateClub(club: Club): Boolean {
        val user = currentUser ?: return false
        val existingClub = clubs[club.id] ?: return false

        // Only owner or admin can update
        val member = getClubMember(club.id, user.userId)
        if (member == null || (member.role != MemberRole.OWNER && member.role != MemberRole.ADMIN)) {
            return false
        }

        clubs[club.id] = club
        publishClubs()
        return true
    }

    fun deleteClub(clubId: String): Boolean {
        val user = currentUser ?: return false
        val club = clubs[clubId] ?: return false

        // Only owner can delete
        if (club.ownerId != user.userId) return false

        clubs.remove(clubId)
        clubMembers.remove(clubId)
        joinRequests.remove(clubId)
        clubBooks.remove(clubId)
        bannedMembers.remove(clubId)

        publishClubs()
        return true
    }

    fun getClub(clubId: String): Club? = clubs[clubId]

    fun getAllPublicClubs(): List<Club> = clubs.values.filter { it.isPublic }

    fun searchClubs(query: String): List<Club> {
        val lowerQuery = query.lowercase()
        return clubs.values.filter { club ->
            club.isPublic && (
                club.name.lowercase().contains(lowerQuery) ||
                club.description.lowercase().contains(lowerQuery) ||
                club.genreTags.any { it.lowercase().contains(lowerQuery) }
            )
        }
    }

    fun getClubsByGenre(genre: String): List<Club> {
        return clubs.values.filter { club ->
            club.isPublic && club.genreTags.any { it.equals(genre, ignoreCase = true) }
        }
    }

    fun getRecommendedClubs(): List<Club> {
        val user = currentUser ?: return getAllPublicClubs().take(5)
        val userGenres = user.favoriteGenres

        return clubs.values.filter { club ->
            club.isPublic &&
            !isUserMember(club.id, user.userId) &&
            club.genreTags.any { tag -> userGenres.any { it.equals(tag, ignoreCase = true) } }
        }.sortedByDescending { it.memberCount }.take(10)
    }

    private fun publishClubs() {
        clubsState.value = clubs.values.toList()
        updateUserClubs()
    }

    private fun updateUserClubs() {
        val user = currentUser ?: return
        userClubsState.value = clubs.values.filter { club ->
            isUserMember(club.id, user.userId)
        }
    }
    // endregion

    // region Club Membership
    fun joinClub(clubId: String): Boolean {
        val user = currentUser ?: return false
        val club = clubs[clubId] ?: return false

        // Check if banned
        if (bannedMembers[clubId]?.contains(user.userId) == true) return false

        // Check if already a member
        if (isUserMember(clubId, user.userId)) return false

        // For private clubs, create a join request instead
        if (!club.isPublic) {
            return createJoinRequest(clubId, null)
        }

        // Add as member for public clubs
        val member = ClubMember(
            clubId = clubId,
            userId = user.userId,
            role = MemberRole.MEMBER,
            status = MemberStatus.ACTIVE
        )
        clubMembers.getOrPut(clubId) { mutableListOf() }.add(member)

        // Update member count
        clubs[clubId] = club.copy(memberCount = club.memberCount + 1)

        // Add activity
        addClubActivity(clubId, ActivityType.MEMBER_JOINED, user.userId, user.displayName.ifBlank { user.username })

        publishClubs()
        return true
    }

    fun leaveClub(clubId: String): Boolean {
        val user = currentUser ?: return false
        val club = clubs[clubId] ?: return false

        // Owner cannot leave (must transfer ownership first or delete club)
        if (club.ownerId == user.userId) return false

        val members = clubMembers[clubId] ?: return false
        val removed = members.removeIf { it.userId == user.userId }

        if (removed) {
            clubs[clubId] = club.copy(memberCount = (club.memberCount - 1).coerceAtLeast(1))
            
            // Add activity
            addClubActivity(clubId, ActivityType.MEMBER_LEFT, user.userId, user.displayName.ifBlank { user.username })
            
            publishClubs()
        }

        return removed
    }

    fun createJoinRequest(clubId: String, message: String?): Boolean {
        val user = currentUser ?: return false
        val club = clubs[clubId] ?: return false

        // Check if already a member or has pending request
        if (isUserMember(clubId, user.userId)) return false
        if (hasPendingRequest(clubId, user.userId)) return false
        if (bannedMembers[clubId]?.contains(user.userId) == true) return false

        val request = JoinRequest(
            clubId = clubId,
            userId = user.userId,
            username = user.displayName.ifBlank { user.username },
            message = message
        )
        joinRequests.getOrPut(clubId) { mutableListOf() }.add(request)
        return true
    }

    fun approveJoinRequest(clubId: String, userId: String): Boolean {
        val user = currentUser ?: return false
        val club = clubs[clubId] ?: return false

        // Only owner or admin can approve
        val approver = getClubMember(clubId, user.userId)
        if (approver == null || (approver.role != MemberRole.OWNER && approver.role != MemberRole.ADMIN)) {
            return false
        }

        val requests = joinRequests[clubId] ?: return false
        val request = requests.find { it.userId == userId } ?: return false

        // Remove request and add as member
        requests.remove(request)

        val member = ClubMember(
            clubId = clubId,
            userId = userId,
            role = MemberRole.MEMBER,
            status = MemberStatus.ACTIVE
        )
        clubMembers.getOrPut(clubId) { mutableListOf() }.add(member)

        clubs[clubId] = club.copy(memberCount = club.memberCount + 1)
        
        // Add activity
        val approvedUser = getUserById(userId)
        addClubActivity(clubId, ActivityType.MEMBER_JOINED, userId, approvedUser?.displayName?.ifBlank { approvedUser.username } ?: "Unknown")
        
        publishClubs()
        return true
    }

    fun rejectJoinRequest(clubId: String, userId: String): Boolean {
        val user = currentUser ?: return false

        // Only owner or admin can reject
        val approver = getClubMember(clubId, user.userId)
        if (approver == null || (approver.role != MemberRole.OWNER && approver.role != MemberRole.ADMIN)) {
            return false
        }

        val requests = joinRequests[clubId] ?: return false
        return requests.removeIf { it.userId == userId }
    }

    fun getClubMembers(clubId: String): List<ClubMember> {
        return clubMembers[clubId]?.filter { it.status == MemberStatus.ACTIVE } ?: emptyList()
    }

    fun getClubMember(clubId: String, userId: String): ClubMember? {
        return clubMembers[clubId]?.find { it.userId == userId && it.status == MemberStatus.ACTIVE }
    }

    fun getUserClubs(): List<Club> {
        val user = currentUser ?: return emptyList()
        return clubs.values.filter { isUserMember(it.id, user.userId) }
    }

    fun isUserMember(clubId: String, userId: String): Boolean {
        return clubMembers[clubId]?.any { it.userId == userId && it.status == MemberStatus.ACTIVE } == true
    }

    fun hasPendingRequest(clubId: String, userId: String): Boolean {
        return joinRequests[clubId]?.any { it.userId == userId } == true
    }

    fun getPendingRequests(clubId: String): List<JoinRequest> {
        val user = currentUser ?: return emptyList()

        // Only owner or admin can view requests
        val member = getClubMember(clubId, user.userId)
        if (member == null || (member.role != MemberRole.OWNER && member.role != MemberRole.ADMIN)) {
            return emptyList()
        }

        return joinRequests[clubId] ?: emptyList()
    }

    fun updateMemberRole(clubId: String, userId: String, newRole: MemberRole): Boolean {
        val user = currentUser ?: return false
        val club = clubs[clubId] ?: return false

        // Only owner can change roles (and cannot demote themselves)
        if (club.ownerId != user.userId) return false
        if (userId == user.userId) return false
        if (newRole == MemberRole.OWNER) return false  // Use transferOwnership instead

        val members = clubMembers[clubId] ?: return false
        val memberIndex = members.indexOfFirst { it.userId == userId }
        if (memberIndex == -1) return false

        members[memberIndex] = members[memberIndex].copy(role = newRole)
        return true
    }

    fun removeMember(clubId: String, userId: String): Boolean {
        val user = currentUser ?: return false
        val club = clubs[clubId] ?: return false

        // Only owner or admin can remove (but not the owner)
        val remover = getClubMember(clubId, user.userId)
        if (remover == null || (remover.role != MemberRole.OWNER && remover.role != MemberRole.ADMIN)) {
            return false
        }
        if (userId == club.ownerId) return false

        val members = clubMembers[clubId] ?: return false
        val removed = members.removeIf { it.userId == userId }

        if (removed) {
            clubs[clubId] = club.copy(memberCount = (club.memberCount - 1).coerceAtLeast(1))
            publishClubs()
        }

        return removed
    }

    fun banMember(clubId: String, userId: String): Boolean {
        val user = currentUser ?: return false
        val club = clubs[clubId] ?: return false

        // Only owner can ban
        if (club.ownerId != user.userId) return false
        if (userId == user.userId) return false

        // Remove from members first
        removeMember(clubId, userId)

        // Add to banned list
        bannedMembers.getOrPut(clubId) { mutableSetOf() }.add(userId)
        return true
    }

    fun transferOwnership(clubId: String, newOwnerId: String): Boolean {
        val user = currentUser ?: return false
        val club = clubs[clubId] ?: return false

        // Only current owner can transfer
        if (club.ownerId != user.userId) return false

        // New owner must be a member
        val newOwnerMember = getClubMember(clubId, newOwnerId) ?: return false

        // Update roles
        val members = clubMembers[clubId] ?: return false
        val currentOwnerIndex = members.indexOfFirst { it.userId == user.userId }
        val newOwnerIndex = members.indexOfFirst { it.userId == newOwnerId }

        if (currentOwnerIndex != -1) {
            members[currentOwnerIndex] = members[currentOwnerIndex].copy(role = MemberRole.ADMIN)
        }
        if (newOwnerIndex != -1) {
            members[newOwnerIndex] = members[newOwnerIndex].copy(role = MemberRole.OWNER)
        }

        clubs[clubId] = club.copy(ownerId = newOwnerId)
        publishClubs()
        return true
    }

    fun getUserById(userId: String): UserProfile? {
        return users.values.find { it.userId == userId }
    }
    // endregion

    // region Club Content
    fun addBookToClub(clubId: String, bookId: String): Boolean {
        val user = currentUser ?: return false

        // Only owner, admin, or moderator can add books
        val member = getClubMember(clubId, user.userId)
        if (member == null || member.role == MemberRole.MEMBER) {
            return false
        }

        val books = clubBooks.getOrPut(clubId) { mutableListOf() }
        if (books.contains(bookId)) return false

        books.add(bookId)
        
        // Add activity
        val book = this.books[bookId]
        addClubActivity(clubId, ActivityType.BOOK_ADDED, user.userId, user.displayName.ifBlank { user.username }, bookId = bookId, bookTitle = book?.title)
        
        return true
    }

    fun removeBookFromClub(clubId: String, bookId: String): Boolean {
        val user = currentUser ?: return false

        // Only owner or admin can remove books
        val member = getClubMember(clubId, user.userId)
        if (member == null || (member.role != MemberRole.OWNER && member.role != MemberRole.ADMIN)) {
            return false
        }

        val removed = clubBooks[clubId]?.remove(bookId) == true
        
        if (removed) {
            // Add activity
            val book = this.books[bookId]
            addClubActivity(clubId, ActivityType.BOOK_REMOVED, user.userId, user.displayName.ifBlank { user.username }, bookId = bookId, bookTitle = book?.title)
        }
        
        return removed
    }

    fun setCurrentClubBook(clubId: String, bookId: String?): Boolean {
        val user = currentUser ?: return false
        val club = clubs[clubId] ?: return false

        // Only owner or admin can set current book
        val member = getClubMember(clubId, user.userId)
        if (member == null || (member.role != MemberRole.OWNER && member.role != MemberRole.ADMIN)) {
            return false
        }

        clubs[clubId] = club.copy(currentBookId = bookId)
        
        // Add activity
        val book = this.books[bookId]
        addClubActivity(clubId, ActivityType.CURRENT_BOOK_CHANGED, user.userId, user.displayName.ifBlank { user.username }, bookId = bookId, bookTitle = book?.title)
        
        publishClubs()
        return true
    }

    fun getClubBooks(clubId: String): List<BookWithCategory> {
        val bookIds = clubBooks[clubId] ?: return emptyList()
        return bookIds.mapNotNull { books[it] }
    }

    fun getCurrentClubBook(clubId: String): BookWithCategory? {
        val club = clubs[clubId] ?: return null
        return club.currentBookId?.let { books[it] }
    }
    // endregion

    // region Invite Codes
    fun generateInviteCode(clubId: String): String? {
        val user = currentUser ?: return null
        val club = clubs[clubId] ?: return null

        // Only owner or admin can generate invite codes
        val member = getClubMember(clubId, user.userId)
        if (member == null || (member.role != MemberRole.OWNER && member.role != MemberRole.ADMIN)) {
            return null
        }

        // Generate a random 8-character code
        val code = (10000000..99999999).random().toString()
        val inviteCode = ClubInviteCode(
            code = code,
            clubId = clubId,
            createdBy = user.userId
        )
        inviteCodes[code] = inviteCode

        // Update club with invite code
        clubs[clubId] = club.copy(inviteCode = code)
        publishClubs()

        return code
    }

    fun joinClubViaInviteCode(code: String): Boolean {
        val user = currentUser ?: return false
        val invite = inviteCodes[code] ?: return false

        // Check if code is expired
        if (invite.expiresAt != null && invite.expiresAt < System.currentTimeMillis()) {
            return false
        }

        // Check if code has reached max uses
        if (invite.maxUses != null && invite.currentUses >= invite.maxUses) {
            return false
        }

        val club = clubs[invite.clubId] ?: return false

        // Check if already a member
        if (isUserMember(club.id, user.userId)) return false

        // Check if banned
        if (bannedMembers[club.id]?.contains(user.userId) == true) return false

        // Add as member
        val member = ClubMember(
            clubId = club.id,
            userId = user.userId,
            role = MemberRole.MEMBER,
            status = MemberStatus.ACTIVE
        )
        clubMembers.getOrPut(club.id) { mutableListOf() }.add(member)

        // Update invite code usage
        inviteCodes[code] = invite.copy(currentUses = invite.currentUses + 1)

        // Update member count
        clubs[club.id] = club.copy(memberCount = club.memberCount + 1)

        // Add activity
        addClubActivity(club.id, ActivityType.MEMBER_JOINED, user.userId, user.displayName.ifBlank { user.username })

        publishClubs()
        return true
    }

    fun getInviteCode(clubId: String): String? {
        return clubs[clubId]?.inviteCode
    }
    // endregion

    // region Club Discussions
    fun createClubDiscussion(clubId: String, title: String, body: String): ClubDiscussion? {
        val user = currentUser ?: return null
        val club = clubs[clubId] ?: return null

        // Only members can create discussions
        if (!isUserMember(clubId, user.userId)) return null

        val discussion = ClubDiscussion(
            clubId = clubId,
            authorId = user.userId,
            authorName = user.displayName.ifBlank { user.username },
            title = title,
            body = body
        )

        clubDiscussions.getOrPut(clubId) { mutableListOf() }.add(discussion)

        // Add activity
        addClubActivity(clubId, ActivityType.DISCUSSION_CREATED, user.userId, user.displayName.ifBlank { user.username }, discussionId = discussion.id, discussionTitle = title)

        return discussion
    }

    fun getClubDiscussions(clubId: String): List<ClubDiscussion> {
        return clubDiscussions[clubId]?.filter { !it.isDeleted }?.sortedByDescending { it.createdAt } ?: emptyList()
    }

    fun replyToClubDiscussion(clubId: String, discussionId: String, commentBody: String): Comment? {
        val user = currentUser ?: return null
        val discussions = clubDiscussions[clubId] ?: return null
        val discussion = discussions.find { it.id == discussionId } ?: return null

        if (!isUserMember(clubId, user.userId)) return null

        val comment = Comment(
            authorId = user.userId,
            authorName = user.displayName.ifBlank { user.username },
            body = commentBody
        )
        discussion.comments.add(comment)

        // Add activity
        addClubActivity(clubId, ActivityType.DISCUSSION_COMMENTED, user.userId, user.displayName.ifBlank { user.username }, discussionId = discussionId, discussionTitle = discussion.title)

        return comment
    }

    fun deleteClubDiscussion(clubId: String, discussionId: String): Boolean {
        val user = currentUser ?: return false
        val discussions = clubDiscussions[clubId] ?: return false
        val discussion = discussions.find { it.id == discussionId } ?: return false

        // Only author, owner, or admin can delete
        val member = getClubMember(clubId, user.userId)
        if (member == null || (discussion.authorId != user.userId && member.role != MemberRole.OWNER && member.role != MemberRole.ADMIN)) {
            return false
        }

        discussion.isDeleted = true
        return true
    }
    // endregion

    // region Club Activities
    private fun addClubActivity(
        clubId: String,
        type: ActivityType,
        userId: String? = null,
        username: String? = null,
        bookId: String? = null,
        bookTitle: String? = null,
        discussionId: String? = null,
        discussionTitle: String? = null
    ) {
        val activity = ClubActivity(
            clubId = clubId,
            type = type,
            userId = userId,
            username = username,
            bookId = bookId,
            bookTitle = bookTitle,
            discussionId = discussionId,
            discussionTitle = discussionTitle
        )
        clubActivities.getOrPut(clubId) { mutableListOf() }.add(activity)

        // Keep only last 50 activities per club
        val activities = clubActivities[clubId]!!
        if (activities.size > 50) {
            activities.removeAll(activities.sortedBy { it.timestamp }.take(activities.size - 50))
        }
    }

    fun getClubActivities(clubId: String, limit: Int = 20): List<ClubActivity> {
        return clubActivities[clubId]?.sortedByDescending { it.timestamp }?.take(limit) ?: emptyList()
    }
    // endregion

    // region Sample Club Data
    fun initializeSampleClubs() {
        if (clubs.isNotEmpty()) return

        val sampleClubs = listOf(
            Club(
                id = "club1",
                name = "Mystery Lovers",
                description = "A club for fans of mystery and detective novels. We discuss classic whodunits and modern thrillers.",
                ownerId = "user2",
                genreTags = listOf("Mystery", "Thriller"),
                isPublic = true,
                memberCount = 15
            ),
            Club(
                id = "club2",
                name = "Sci-Fi Explorers",
                description = "Exploring the vast universe of science fiction, from classic Asimov to modern space operas.",
                ownerId = "user3",
                genreTags = listOf("Science Fiction", "Fantasy"),
                isPublic = true,
                memberCount = 23
            ),
            Club(
                id = "club3",
                name = "Classic Literature Society",
                description = "Dedicated to reading and discussing timeless literary masterpieces.",
                ownerId = "user2",
                genreTags = listOf("Classics", "Fiction"),
                isPublic = true,
                memberCount = 42
            ),
            Club(
                id = "club4",
                name = "Romance Readers",
                description = "For those who love a good love story. Contemporary, historical, and everything in between!",
                ownerId = "user3",
                genreTags = listOf("Romance", "Fiction"),
                isPublic = true,
                memberCount = 31
            ),
            Club(
                id = "club5",
                name = "Private Book Club",
                description = "An exclusive book club for serious readers. Join by invitation only.",
                ownerId = "user2",
                genreTags = listOf("Fiction", "Non-Fiction"),
                isPublic = false,
                memberCount = 8
            )
        )

        sampleClubs.forEach { club ->
            clubs[club.id] = club
            // Add owner as member
            val ownerMember = ClubMember(
                clubId = club.id,
                userId = club.ownerId,
                role = MemberRole.OWNER,
                status = MemberStatus.ACTIVE
            )
            clubMembers.getOrPut(club.id) { mutableListOf() }.add(ownerMember)
            clubBooks[club.id] = mutableListOf()
        }

        publishClubs()
    }
    // endregion
}
