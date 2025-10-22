package com.example.bookbuddy

import java.util.*

// Simple in-memory database for BookBuddy
object BookBuddyDatabase {
    private val users = mutableMapOf<String, UserProfile>()
    private val books = mutableMapOf<String, BookWithCategory>()
    private val collections = mutableMapOf<String, BookCollection>()
    private var currentUser: UserProfile? = null
    
    // User Management
    fun registerUser(userProfile: UserProfile): Boolean {
        return if (users.containsKey(userProfile.email)) {
            false // User already exists
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
    
    // Book Management
    fun addBook(book: BookWithCategory): Boolean {
        return if (currentUser != null) {
            books[book.id] = book
            true
        } else {
            false
        }
    }
    
    fun deleteBook(bookId: String): Boolean {
        return if (currentUser != null && books.containsKey(bookId)) {
            books.remove(bookId)
            true
        } else {
            false
        }
    }
    
    fun getUserBooks(): List<BookWithCategory> {
        return if (currentUser != null) {
            books.values.filter { book ->
                // For now, all books belong to current user
                // In a real app, you'd have user-book relationships
                true
            }
        } else {
            emptyList()
        }
    }
    
    fun getAllBooks(): List<BookWithCategory> = books.values.toList()
    
    // Collection Management
    fun createCollection(collection: BookCollection): Boolean {
        return if (currentUser != null) {
            collections[collection.title] = collection
            true
        } else {
            false
        }
    }
    
    fun getUserCollections(): List<BookCollection> {
        return if (currentUser != null) {
            collections.values.toList()
        } else {
            emptyList()
        }
    }
    
    fun addBookToCollection(collectionTitle: String, book: BookWithCategory): Boolean {
        return if (currentUser != null && collections.containsKey(collectionTitle)) {
            val collection = collections[collectionTitle]!!
            val updatedBooks = collection.books + Book(book.title, book.author)
            collections[collectionTitle] = collection.copy(books = updatedBooks)
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
            true
        } else {
            false
        }
    }
    
    // Search and Filter
    fun searchBooks(query: String): List<BookWithCategory> {
        val lowerQuery = query.lowercase()
        return books.values.filter { book ->
            book.title.lowercase().contains(lowerQuery) ||
            book.author.lowercase().contains(lowerQuery) ||
            book.categories.any { it.lowercase().contains(lowerQuery) }
        }
    }
    
    fun getBooksByCategory(category: String): List<BookWithCategory> {
        return books.values.filter { book ->
            book.categories.any { it.equals(category, ignoreCase = true) }
        }
    }
    
    // Initialize with sample data
    fun initializeSampleData() {
        // Add sample user
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
        users["user@example.com"] = sampleUser
        currentUser = sampleUser
        
        // Add sample books
        val sampleBooks = listOf(
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
        
        sampleBooks.forEach { book ->
            books[book.id] = book
        }
        
        // Add sample collections
        val sampleCollections = listOf(
            BookCollection("Favorites", listOf(Book("Of Mice and Men", "John Steinbeck"))),
            BookCollection("To Read", listOf(Book("The Housemaid", "Freida McFadden"))),
            BookCollection("Classics", listOf(Book("To Kill a Mockingbird", "Harper Lee")))
        )
        
        sampleCollections.forEach { collection ->
            collections[collection.title] = collection
        }
    }
}
