package com.example.bookbuddy

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CollectionsScreen(onEditProfile: () -> Unit) {
    // This is now handled by EnhancedCollectionDisplay in MainActivity
    // Keeping this for backward compatibility
    val collections = BookBuddyDatabase.getUserCollections()
    val books = BookBuddyDatabase.getUserBooks()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("BookBuddy", style = MaterialTheme.typography.titleLarge) },
                actions = {
                    IconButton(onClick = onEditProfile) {
                        Icon(Icons.Default.Person, contentDescription = "Profile")
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(modifier = Modifier.padding(paddingValues).padding(16.dp)) {
            items(collections) { col ->
                Card(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(col.title, style = MaterialTheme.typography.titleMedium)
                        col.books.forEach { book ->
                            Text("• ${book.title} by ${book.author}", style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }
        }
    }
}
