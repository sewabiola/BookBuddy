package com.example.bookbuddy

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryBrowsingScreen(
    navController: NavHostController,
    books: List<BookWithCategory>,
    onBookClick: (BookWithCategory) -> Unit
) {
    var selectedCategory by remember { mutableStateOf<String?>(null) }
    var selectedReadingStatus by remember { mutableStateOf<String?>(null) }

    // Extract all unique categories from books
    val categories = remember(books) {
        books.flatMap { it.categories }.distinct().sorted()
    }

    // Filter books based on selected filters
    val filteredBooks = remember(books, selectedCategory, selectedReadingStatus) {
        var result = books

        if (selectedCategory != null) {
            result = result.filter { it.categories.contains(selectedCategory) }
        }

        if (selectedReadingStatus != null) {
            result = result.filter { it.readingStatus == selectedReadingStatus }
        }

        result
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Browse Books") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    // Clear filters button
                    if (selectedCategory != null || selectedReadingStatus != null) {
                        TextButton(onClick = {
                            selectedCategory = null
                            selectedReadingStatus = null
                        }) {
                            Text("Clear Filters")
                        }
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Filter Chips Section
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                // Category Filter
                Text(
                    text = "Filter by Category",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                LazyColumn(
                    modifier = Modifier.heightIn(max = 150.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(categories.chunked(3)) { rowCategories ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            rowCategories.forEach { category ->
                                FilterChip(
                                    selected = selectedCategory == category,
                                    onClick = {
                                        selectedCategory = if (selectedCategory == category) null else category
                                    },
                                    label = { Text(category) },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            // Fill empty spaces in incomplete rows
                            repeat(3 - rowCategories.size) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Reading Status Filter
                Text(
                    text = "Filter by Reading Status",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("Not Started", "Reading", "Read").forEach { status ->
                        FilterChip(
                            selected = selectedReadingStatus == status,
                            onClick = {
                                selectedReadingStatus = if (selectedReadingStatus == status) null else status
                            },
                            label = { Text(status) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            Divider()

            // Results Section
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Results",
                        style = MaterialTheme.typography.titleLarge
                    )
                    Text(
                        text = "${filteredBooks.size} books",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Books Grid
            if (filteredBooks.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            Icons.Default.Search,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "No books found",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Try adjusting your filters",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    contentPadding = PaddingValues(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredBooks) { book ->
                        BookCard(
                            book = book,
                            onClick = { onBookClick(book) }
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookCard(
    book: BookWithCategory,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(280.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp)
        ) {
            // Book Cover or Placeholder
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    if (book.coverImageUrl.isNotEmpty()) {
                        coil.compose.AsyncImage(
                            model = book.coverImageUrl,
                            contentDescription = book.title,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = androidx.compose.ui.layout.ContentScale.Crop
                        )
                    } else {
                        Icon(
                            Icons.Default.Star,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Book Title
            Text(
                text = book.title,
                style = MaterialTheme.typography.titleSmall,
                maxLines = 2,
                minLines = 2
            )

            // Author
            Text(
                text = book.author,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1
            )

            // Reading Status Badge
            if (book.readingStatus.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                AssistChip(
                    onClick = { },
                    label = {
                        Text(
                            text = book.readingStatus,
                            style = MaterialTheme.typography.labelSmall
                        )
                    },
                    modifier = Modifier.height(24.dp)
                )
            }
        }
    }
}
