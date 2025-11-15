package com.example.bookbuddy

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Initialize sample data
        BookBuddyDatabase.initializeSampleData()
        
        setContent {
            MaterialTheme {
                Surface {
                    val navController = rememberNavController()
                    var isLoggedIn by remember { mutableStateOf(BookBuddyDatabase.getCurrentUser() != null) }
                    val books by BookBuddyDatabase.observeBooks().collectAsState()
                    val collections by BookBuddyDatabase.observeCollections().collectAsState()
                    val recommended by BookBuddyDatabase.observeRecommendations().collectAsState()
                    val allReviews by BookBuddyDatabase.observeReviews().collectAsState()
                    val isFetchingBooks by BookBuddyDatabase.observeIsFetchingBooks().collectAsState()
                    
                    // Check login status and navigate accordingly
                    LaunchedEffect(isLoggedIn) {
                        if (!isLoggedIn) {
                            navController.navigate("login") {
                                popUpTo(0) { inclusive = true }
                            }
                        }
                    }
                    
                    LaunchedEffect(Unit) {
                        BookBuddyDatabase.ensureRemoteBooksLoaded()
                    }
                    
                    NavHost(navController = navController, startDestination = if (isLoggedIn) "collections" else "login") {
                        // Authentication Screens
                        composable("login") {
                            LoginScreen(
                                onLoginSuccess = { 
                                    isLoggedIn = true
                                    navController.navigate("collections") {
                                        popUpTo(0) { inclusive = true }
                                    }
                                },
                                onNavigateToRegister = { navController.navigate("register") }
                            )
                        }
                        
                        composable("register") {
                            RegistrationScreen(
                                onRegisterSuccess = { 
                                    isLoggedIn = true
                                    navController.navigate("profile_setup") {
                                        popUpTo(0) { inclusive = true }
                                    }
                                },
                                onNavigateToLogin = { navController.popBackStack() }
                            )
                        }
                        
                        // Profile Setup Wizard
                        composable("profile_setup") {
                            ProfileSetupWizard(
                                onComplete = { userProfile ->
                                    BookBuddyDatabase.updateUserProfile(userProfile)
                                    navController.navigate("collections") {
                                        popUpTo(0) { inclusive = true }
                                    }
                                },
                                onSkip = { navController.navigate("collections") {
                                    popUpTo(0) { inclusive = true }
                                }}
                            )
                        }
                        
                        // Main App Screens
                        composable("collections") {
                            EnhancedCollectionDisplay(
                                collections = collections,
                                booksWithCategory = books,
                                recommendedBooks = recommended,
                                isLoadingBooks = isFetchingBooks,
                                onBookClick = { book -> 
                                    // Navigate to book details
                                    navController.navigate("book_details/${book.id}")
                                },
                                onCollectionClick = { collection ->
                                    // Navigate to collection details
                                    navController.navigate("collection_details/${collection.title}")
                                },
                                onAddBookClick = { navController.navigate("add_book") },
                                onProfileClick = { navController.navigate("profile") },
                                onBookDelete = { book ->
                                    BookBuddyDatabase.deleteBook(book.id)
                                }
                            )
                        }
                        
                        composable("add_book") {
                            BookAdditionScreen(
                                onBookAdded = { book ->
                                    BookBuddyDatabase.addBook(book)
                                    navController.popBackStack()
                                },
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }
                        
                        composable("profile") {
                            ProfileScreen(
                                onBack = { navController.popBackStack() },
                                onLogout = {
                                    BookBuddyDatabase.logout()
                                    isLoggedIn = false
                                }
                            )
                        }
                        
                        composable("categories") {
                            CategoryBrowserScreen(
                                onCategorySelected = { category ->
                                    navController.navigate("category_details/${category.id}")
                                },
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }
                        
                        composable("category_details/{categoryId}") { backStackEntry ->
                            val categoryId = backStackEntry.arguments?.getString("categoryId") ?: ""
                            val category = BookCategorization.getCategoryById(categoryId)
                            if (category != null) {
                                CategoryDetailScreen(
                                    category = category,
                                    books = BookBuddyDatabase.getBooksByCategory(category.name),
                                    onNavigateBack = { navController.popBackStack() },
                                    onBookClick = { book ->
                                        navController.navigate("book_details/${book.id}")
                                    }
                                )
                            }
                        }
                        
                        composable("book_details/{bookId}") { backStackEntry ->
                            val bookId = backStackEntry.arguments?.getString("bookId") ?: ""
                            val book = books.find { it.id == bookId }
                            if (book != null) {
                                BookDetailScreen(
                                    book = book,
                                    reviews = allReviews.filter { it.bookId == book.id },
                                    currentUser = BookBuddyDatabase.getCurrentUser(),
                                    onNavigateBack = { navController.popBackStack() },
                                    onDeleteBook = {
                                        BookBuddyDatabase.deleteBook(book.id)
                                        navController.popBackStack()
                                    },
                                    onSubmitReview = { reviewId, rating, content ->
                                        if (reviewId == null) {
                                            BookBuddyDatabase.createReview(book.id, rating, content)
                                        } else {
                                            BookBuddyDatabase.updateReview(reviewId, rating, content)
                                        }
                                    },
                                    onDeleteReview = { review ->
                                        BookBuddyDatabase.deleteReview(review.id)
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
