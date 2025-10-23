package com.example.bookbuddy

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.compose.runtime.getValue


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface {
                    val navController = rememberNavController()
                    val authViewModel = AuthViewModel() // Adds the AuthViewModel to manage login/logout/reset
                   // val bookRepository = BookRepository() // Adds the BookRepository instance

                    NavHost(
                        navController = navController,
                        startDestination = "login" // App starts at Login screen
                    ) {
                        // login screen
                        composable("login") {
                            LoginScreen(
                                viewModel = authViewModel,
                                onLoginSuccess = {
                                    navController.navigate("collections") { //Redirects to collections
                                        popUpTo("login") { inclusive = true }
                                    }
                                },
                                onForgotPassword = {
                                    navController.navigate("resetPassword")
                                }
                            )
                        }

                        // reset password screen
                        composable("resetPassword") {
                            ResetPasswordScreen(
                                viewModel = authViewModel,
                                onBackToLogin = {
                                    navController.popBackStack()
                                }
                            )
                        }

                        // book collection display screen
                        composable("collections") {
                            val repository = remember { BookRepository() }

                            var books by remember { mutableStateOf<List<BookWithCategory>>(emptyList()) }

                            LaunchedEffect(Unit) {
                                books = repository.searchBookOnline("popular novels")
                            }

                            EnhancedCollectionDisplay(
                                collections = emptyList(),
                                booksWithCategory = books,
                                onBookClick = { /* TODO: Navigate to Book Details */ },
                                onCollectionClick = { /* TODO: Future feature */ },
                                onAddBookClick = { navController.navigate("addBook") },
                                onProfileClick = { /* TODO: Navigate to profile screen */ }
                            )
                        }

                        // book addition screen
                        composable("addBook") {
                            BookAdditionScreen(
                                onBookAdded = { newBook ->

                                    // For now, simply go back to collections since the books come from Google API
                                    navController.popBackStack()
                                },
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }
                    }
                }
            }
        }
    }
}

