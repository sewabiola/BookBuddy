package com.example.bookbuddy

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable

fun CollectionsScreen(onEditProfile: () -> Unit) {

    val repository = remember { BookRepository() }
    var collections by remember { mutableStateOf<List<BookCollection>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var categories by remember { mutableStateOf<List<String>>(emptyList()) }

// Load book data from Google Books API when screen opens
    LaunchedEffect(Unit) {
        categories = repository.getCategories().filter { it != "All" }
        val tempCollections = mutableListOf<BookCollection>()

        for (category in categories) {
            val books = repository.searchBookOnline(category)
            tempCollections.add(BookCollection(category, books))
        }

        collections = tempCollections
        isLoading = false
    }


    // Task 2-2-3-1: Added state for search bar
    var searchQuery by remember { mutableStateOf("") }

    // Task 2-2-3-2: Added state for filter menu
    var selectedCategory by remember { mutableStateOf("All") }

    // Filters books first by category, then by title/author
    val filteredCollections = collections.filter { col ->
        (selectedCategory == "All" || col.title == selectedCategory) &&
                (searchQuery.isBlank() ||
                        col.books.any { book ->
                            book.title.contains(searchQuery, ignoreCase = true) ||
                                    book.author.contains(searchQuery, ignoreCase = true)
                        })
    }


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
        // Changed the code to place the search bar, filter menu, and book list vertically
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(16.dp)
        ) {

            // Task 2-2-3-1: Added Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text("Search books...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )


            Spacer(Modifier.height(16.dp))

            // Task 2-2-3-2: Added Dropdown filter for categories
            var expanded by remember { mutableStateOf(false) }

            Box {
                OutlinedButton(
                    onClick = { expanded = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Filter: $selectedCategory")
                }

                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    categories.forEach { category ->
                        DropdownMenuItem(
                            text = { Text(category) },
                            onClick = {
                                selectedCategory = category
                                expanded = false
                            }
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // Task 2-2-3: Updated list and empty state
            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else

                if (filteredCollections.isEmpty()) {
                Text(
                    text = when {
                        searchQuery.isNotBlank() ->
                            "No books found matching '$searchQuery'"

                        selectedCategory != "All" ->
                            "No books found in '$selectedCategory' category"

                        else -> "No books found"
                    },
                    style = MaterialTheme.typography.bodyMedium
                )
            } else {
                LazyColumn {
                    items(filteredCollections) { col ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(col.title, style = MaterialTheme.typography.titleMedium)
                                col.books.forEach { book ->
                                    Text(
                                        "• ${book.title} by ${book.author}",
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}