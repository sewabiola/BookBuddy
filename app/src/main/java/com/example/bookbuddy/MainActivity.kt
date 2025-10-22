package com.example.bookbuddy

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface {
                    val navController = rememberNavController()
                    val authViewModel = AuthViewModel() // Adds the AuthViewModel to manage login/logout/reset

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

                        // collection screen (after login)
                        composable("collections") {
                            CollectionsScreen(
                                onEditProfile = { navController.navigate("profile") }
                            )
                        }

                        // profile screen
                        composable("profile") {
                            ProfileScreen(
                                onBack = { navController.popBackStack() },
                                navController = navController,
                                authViewModel = authViewModel
                            )
                        }
                    }
                }
            }
        }
    }
}

