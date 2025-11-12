package com.example.bookbuddy

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import coil.compose.AsyncImage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookDetailScreen(
    book: BookWithCategory,
    onNavigateBack: () -> Unit,
    onDeleteBook: () -> Unit,
    navController: NavHostController
) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    var readingStatus by remember { mutableStateOf("Not Started") }

    // Load reviews for this book
    val reviews by remember { mutableStateOf(BookBuddyDatabase.getReviewsForBook(book.id)) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Book Details") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Delete Book",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    // Navigate to review screen
                    navController.navigate("book_review/${book.id}")
                }
            ) {
                Icon(Icons.Default.Edit, contentDescription = "Write Review")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            // --- Book Cover ---
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
                    .padding(16.dp)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    if (book.coverImageUrl.isNotEmpty()) {
                        AsyncImage(
                            model = book.coverImageUrl,
                            contentDescription = book.title,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Default.Star,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "No Cover Image",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // --- Book Information ---
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = book.title,
                    style = MaterialTheme.typography.headlineMedium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "by ${book.author}",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                if (book.rating > 0) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Star,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = String.format("%.1f", book.rating),
                            style = MaterialTheme.typography.titleLarge
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // Reading Status
                Text("Reading Status", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("Not Started", "Reading", "Finished").forEach { status ->
                        FilterChip(
                            selected = readingStatus == status,
                            onClick = { readingStatus = status },
                            label = { Text(status) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Categories
                if (book.categories.isNotEmpty()) {
                    Text("Categories", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        book.categories.forEach { category ->
                            FilterChip(
                                selected = false,
                                onClick = { },
                                label = { Text(category) }
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // Book Details
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Book Details", style = MaterialTheme.typography.titleMedium)
                        Spacer(modifier = Modifier.height(8.dp))
                        if (book.publishedYear > 0) DetailRow(
                            "Published",
                            book.publishedYear.toString()
                        )
                        if (book.pageCount > 0) DetailRow("Pages", book.pageCount.toString())
                        if (book.language.isNotEmpty()) DetailRow("Language", book.language)
                        if (book.isbn.isNotEmpty()) DetailRow("ISBN", book.isbn)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Description
                if (book.description.isNotEmpty()) {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Description", style = MaterialTheme.typography.titleMedium)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(book.description, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // --- Reviews Section ---
                Text(
                    text = "Reviews",
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(8.dp))

                // --- Review Section ---
                val allReviews = remember { mutableStateListOf<BookReview>() }
                var selectedSortOption by remember { mutableStateOf("Newest") }

                LaunchedEffect(book.id) {
                    val fetchedReviews = BookBuddyDatabase.getReviewsForBook(book.id)
                    allReviews.clear()
                    allReviews.addAll(fetchedReviews)
                }

                if (allReviews.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Reviews",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    // Sorting Dropdown
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Sort by:")
                        DropdownMenuBox(
                            selectedSortOption,
                            onOptionSelected = { selectedSortOption = it }
                        )
                    }

                    // Apply sorting logic
                    val sortedReviews = when (selectedSortOption) {
                        "Highest Rating" -> allReviews.sortedByDescending { it.rating }
                        "Lowest Rating" -> allReviews.sortedBy { it.rating }
                        "Oldest" -> allReviews // Assuming order added = oldest
                        else -> allReviews.reversed() // "Newest" default
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    sortedReviews.forEach { review ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Default.Star,
                                        contentDescription = "Rating",
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("${review.rating}/5")
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(review.comment)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    "- ${review.username}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                // Delete Confirmation Dialog
                if (showDeleteDialog) {
                    AlertDialog(
                        onDismissRequest = { showDeleteDialog = false },
                        title = { Text("Delete Book") },
                        text = { Text("Are you sure you want to delete \"${book.title}\"? This action cannot be undone.") },
                        confirmButton = {
                            TextButton(onClick = {
                                onDeleteBook()
                                showDeleteDialog = false
                            }) {
                                Text("Delete", color = MaterialTheme.colorScheme.error)
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { showDeleteDialog = false }) {
                                Text("Cancel")
                            }
                        }
                    )
                }
            }

            @Composable
            fun DetailRow(label: String, value: String) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = value,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}
@Composable
fun DropdownMenuBox(
    selectedOption: String,
    onOptionSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val options = listOf("Newest", "Oldest", "Highest Rating", "Lowest Rating")

    Box {
        Button(onClick = { expanded = true }) {
            Text(selectedOption)
            Icon(Icons.Default.ArrowDropDown, contentDescription = null)
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        onOptionSelected(option)
                        expanded = false
                    }
                )
            }
        }
    }
}
@Composable
fun DetailRow(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

