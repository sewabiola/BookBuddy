package com.example.bookbuddy

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.*
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.bookbuddy.ui.theme.BookBuddyTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            BookBuddyTheme {
                val navController = rememberNavController()
                NavHost(navController = navController, startDestination = "collections") {
                    composable("collections") {
                        CollectionsScreen(onEditProfile = { navController.navigate("profile") })
                    }
                    composable("profile") {
                        ProfileScreen(onBack = { navController.popBackStack() })
                    }
                }
            }
        }
    }
}
