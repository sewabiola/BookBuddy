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
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay

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

                    NavHost(
                        navController = navController,
                        startDestination = if (isLoggedIn) "collections" else "login"
                    ) {
                        // Authentication Screens
                        composable("login") {
                            LoginScreen(
                                onLoginSuccess = {
                                    isLoggedIn = true
                                    navController.navigate("collections") {
                                        popUpTo(0) { inclusive = true }
                                    }
                                },
                                onNavigateToRegister = { navController.navigate("register") },
                                onNavigateToReset = { navController.navigate("password_reset") }
                            )
                        }

                        composable("password_reset") {
                            PasswordResetScreen(
                                onBack = { navController.popBackStack() },
                                onResetComplete = { navController.popBackStack() }
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
                                onSkip = {
                                    navController.navigate("collections") {
                                        popUpTo(0) { inclusive = true }
                                    }
                                }
                            )
                        }

                        // Stats Screen (Loren's feature)
                        composable("stats") {
                            ReadingStatsScreen(navController = navController)
                        }

                        // Main App Screens - Using your EnhancedCollectionDisplay with state management
                        composable("collections") {
                            EnhancedCollectionDisplay(
                                collections = collections,
                                booksWithCategory = books,
                                recommendedBooks = recommended,
                                isLoadingBooks = isFetchingBooks,
                                onBookClick = { book ->
                                    navController.navigate("book_details/${book.id}")
                                },
                                onCollectionClick = { collection ->
                                    navController.navigate("collection_details/${collection.title}")
                                },
                                onAddBookClick = { navController.navigate("add_book") },
                                onProfileClick = { navController.navigate("profile") },
                                onBookDelete = { book ->
                                    BookBuddyDatabase.deleteBook(book.id)
                                },
                                onBrowseCategories = { navController.navigate("browse_categories") },
                                onViewReadingStatus = { navController.navigate("reading_status") },
                                onViewRecommendations = { navController.navigate("recommendations") },
                                onViewClubs = { navController.navigate("clubs_discovery") },
                                modifier = androidx.compose.ui.Modifier
                            )
                        }

                        composable("all_collections") {
                            AllCollectionsScreen(
                                navController = navController,
                                onBack = { navController.popBackStack() }
                            )
                        }

                        composable("add_book") {
                            val scope = rememberCoroutineScope()
                            BookAdditionScreen(
                                onBookAdded = { book ->
                                    BookBuddyDatabase.addBook(book)
                                    // Delay navigation to allow snackbar to be visible
                                    scope.launch {
                                        kotlinx.coroutines.delay(1000)
                                        navController.popBackStack()
                                    }
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

                        // LocYenDan's forum and recommendation routes
                        composable("recommend") {
                            RecommendationScreen(
                                navController = navController,
                                onBookClick = { book ->
                                    navController.navigate("book_details/${book.id}")
                                }
                            )
                        }

                        composable("forum") { ForumScreen(navController) }

                        composable("forum_thread/{postId}") { backStackEntry ->
                            val postId = backStackEntry.arguments?.getString("postId") ?: ""
                            ForumThreadScreen(postId = postId, navController = navController)
                        }

                        // Loren's separate book review screen
                        composable("book_review/{bookId}") { backStackEntry ->
                            val bookId = backStackEntry.arguments?.getString("bookId") ?: ""
                            val book = BookBuddyDatabase.getBookById(bookId)
                            if (book != null) {
                                BookReviewScreen(
                                    book = book,
                                    onReviewAdded = {
                                        BookBuddyDatabase.addReview(it)
                                    },
                                    onNavigateBack = { navController.popBackStack() }
                                )
                            }
                        }

                        // Book details with your enhanced review functionality
                        composable("book_details/{bookId}") { backStackEntry ->
                            val bookId = backStackEntry.arguments?.getString("bookId") ?: ""
                            val book = BookBuddyDatabase.getAllBooks().find { it.id == bookId }
                            if (book != null) {
                                BookDetailScreen(
                                    book = book,
                                    onNavigateBack = { navController.popBackStack() },
                                    onDeleteBook = {
                                        BookBuddyDatabase.deleteBook(book.id)
                                        navController.popBackStack()
                                    },
                                    navController = navController
                                )
                            }
                        }

                        // Category browsing screen
                        composable("browse_categories") {
                            CategoryBrowsingScreen(
                                navController = navController,
                                books = books,
                                onBookClick = { book ->
                                    navController.navigate("book_details/${book.id}")
                                }
                            )
                        }

                        // Collection details screen
                        composable("collection_details/{collectionTitle}") { backStackEntry ->
                            val collectionTitle = backStackEntry.arguments?.getString("collectionTitle") ?: ""
                            CollectionDetailsScreen(
                                navController = navController,
                                collectionTitle = collectionTitle
                            )
                        }

                        // Recommendations screen
                        composable("recommendations") {
                            RecommendationsScreen(navController = navController)
                        }

                        // Reading status screen
                        composable("reading_status") {
                            ReadingStatusScreen(navController = navController)
                        }

                        // Club Screens
                        composable("clubs_discovery") {
                            ClubDiscoveryScreen(navController = navController)
                        }

                        composable("my_clubs") {
                            MyClubsScreen(navController = navController)
                        }

                        composable("create_club") {
                            ClubCreationScreen(navController = navController)
                        }

                        composable("club_detail/{clubId}") { backStackEntry ->
                            val clubId = backStackEntry.arguments?.getString("clubId") ?: ""
                            ClubDetailScreen(
                                clubId = clubId,
                                navController = navController
                            )
                        }

                        composable("club_members/{clubId}") { backStackEntry ->
                            val clubId = backStackEntry.arguments?.getString("clubId") ?: ""
                            ClubMembersScreen(
                                clubId = clubId,
                                navController = navController
                            )
                        }

                        composable("club_settings/{clubId}") { backStackEntry ->
                            val clubId = backStackEntry.arguments?.getString("clubId") ?: ""
                            ClubSettingsScreen(
                                clubId = clubId,
                                navController = navController
                            )
                        }
                    }
                }
            }
        }
    }
}
