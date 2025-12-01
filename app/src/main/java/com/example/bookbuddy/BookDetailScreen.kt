package com.example.bookbuddy

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
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
    var readingStatus by remember {
        mutableStateOf(BookBuddyDatabase.getReadingStatus(book.id))
    }
    
    // Force recomposition when returning to this screen
    var refreshKey by remember { mutableStateOf(0) }
    LaunchedEffect(Unit) {
        refreshKey++
    }

    // Reviews are loaded via observeReviews() in the reviews section below

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

                // --- Reading Status Section ---
                Text("Reading Status", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))

                val statuses = listOf("Not Started", "Reading", "Read")

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    statuses.forEach { status ->
                        FilterChip(
                            selected = readingStatus == status,
                            onClick = {
                                readingStatus = status
                                // Save automatically to database
                                BookBuddyDatabase.setReadingStatus(book.id, status)
                            },
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

                    val allReviews by BookBuddyDatabase.observeReviews().collectAsState()
                    val reviewsForBook = remember(allReviews, book.id) {
                        allReviews.filter { it.bookId == book.id }
                    }
                    var selectedSortOption by remember { mutableStateOf("Newest") }
                    val currentUser = BookBuddyDatabase.getCurrentUser()
                    var editingReviewId by remember { mutableStateOf<String?>(null) }
                    var editingReviewText by remember { mutableStateOf("") }
                    var editingReviewRating by remember { mutableStateOf(0) }
                    var showDeleteReviewDialog by remember { mutableStateOf<String?>(null) }

                    if (reviewsForBook.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Reviews (${reviewsForBook.size})",
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
                            "Highest Rating" -> reviewsForBook.sortedByDescending { it.rating }
                            "Lowest Rating" -> reviewsForBook.sortedBy { it.rating }
                            "Oldest" -> reviewsForBook.sortedBy { it.createdAt }
                            else -> reviewsForBook.sortedByDescending { it.createdAt } // "Newest" default
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        sortedReviews.forEach { review ->
                            val isOwnReview = currentUser?.userId == review.userId
                            val votes = BookBuddyDatabase.getReviewVotes(review.id)
                            val userVote = currentUser?.let { BookBuddyDatabase.getUserVoteForReview(review.id, it.userId) } ?: 0

                            if (editingReviewId == review.id) {
                                // Edit Review Card
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                                ) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        Text("Edit Review", style = MaterialTheme.typography.titleMedium)
                                        Spacer(modifier = Modifier.height(8.dp))
                                        
                                        // Rating Selection
                                        Text("Rating:", style = MaterialTheme.typography.bodyMedium)
                                        Row {
                                            (1..5).forEach { rating ->
                                                IconButton(onClick = { editingReviewRating = rating }) {
                                                    Icon(
                                                        Icons.Default.Star,
                                                        contentDescription = "Rating $rating",
                                                        tint = if (rating <= editingReviewRating) 
                                                            MaterialTheme.colorScheme.primary 
                                                        else 
                                                            MaterialTheme.colorScheme.onSurfaceVariant
                                                    )
                                                }
                                            }
                                        }
                                        
                                        Spacer(modifier = Modifier.height(8.dp))
                                        
                                        // Review Text
                                        OutlinedTextField(
                                            value = editingReviewText,
                                            onValueChange = { editingReviewText = it },
                                            label = { Text("Review") },
                                            modifier = Modifier.fillMaxWidth(),
                                            minLines = 3,
                                            maxLines = 5
                                        )
                                        
                                        Spacer(modifier = Modifier.height(8.dp))
                                        
                                        // Action Buttons
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.End
                                        ) {
                                            TextButton(onClick = {
                                                editingReviewId = null
                                                editingReviewText = ""
                                                editingReviewRating = 0
                                            }) {
                                                Text("Cancel")
                                            }
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Button(onClick = {
                                                if (editingReviewText.isNotBlank() && editingReviewRating > 0) {
                                                    BookBuddyDatabase.updateReview(review.id, editingReviewRating, editingReviewText)
                                                    editingReviewId = null
                                                    editingReviewText = ""
                                                    editingReviewRating = 0
                                                }
                                            }) {
                                                Text("Save")
                                            }
                                        }
                                    }
                                }
                            } else {
                                // Display Review Card
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                                ) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.Top
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(
                                                    Icons.Default.Star,
                                                    contentDescription = "Rating",
                                                    tint = MaterialTheme.colorScheme.primary
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text("${review.rating}/5")
                                                if (review.isEdited) {
                                                    Spacer(modifier = Modifier.width(8.dp))
                                                    Text(
                                                        "(edited)",
                                                        style = MaterialTheme.typography.bodySmall,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                                    )
                                                }
                                            }
                                            
                                            // Edit/Delete buttons for own reviews
                                            if (isOwnReview) {
                                                Row {
                                                    IconButton(
                                                        onClick = {
                                                            editingReviewId = review.id
                                                            editingReviewText = review.content
                                                            editingReviewRating = review.rating
                                                        },
                                                        modifier = Modifier.size(32.dp)
                                                    ) {
                                                        Icon(
                                                            Icons.Default.Edit,
                                                            contentDescription = "Edit Review",
                                                            modifier = Modifier.size(18.dp)
                                                        )
                                                    }
                                                    IconButton(
                                                        onClick = { showDeleteReviewDialog = review.id },
                                                        modifier = Modifier.size(32.dp)
                                                    ) {
                                                        Icon(
                                                            Icons.Default.Delete,
                                                            contentDescription = "Delete Review",
                                                            modifier = Modifier.size(18.dp),
                                                            tint = MaterialTheme.colorScheme.error
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                        
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(review.content)
                                        Spacer(modifier = Modifier.height(4.dp))
                                        
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                "- ${review.username}",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                            
                                            // Voting Buttons
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                IconButton(
                                                    onClick = {
                                                        currentUser?.let { user ->
                                                            BookBuddyDatabase.voteReview(review.id, user.userId, true)
                                                        }
                                                    },
                                                    modifier = Modifier.size(32.dp)
                                                ) {
                                                    Icon(
                                                        Icons.Default.KeyboardArrowUp,
                                                        contentDescription = "Upvote",
                                                        modifier = Modifier.size(18.dp),
                                                        tint = if (userVote == 1) 
                                                            MaterialTheme.colorScheme.primary 
                                                        else 
                                                            MaterialTheme.colorScheme.onSurfaceVariant
                                                    )
                                                }
                                                Text(
                                                    "${votes.first}",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    modifier = Modifier.padding(horizontal = 4.dp)
                                                )
                                                IconButton(
                                                    onClick = {
                                                        currentUser?.let { user ->
                                                            BookBuddyDatabase.voteReview(review.id, user.userId, false)
                                                        }
                                                    },
                                                    modifier = Modifier.size(32.dp)
                                                ) {
                                                    Icon(
                                                        Icons.Default.KeyboardArrowDown,
                                                        contentDescription = "Downvote",
                                                        modifier = Modifier.size(18.dp),
                                                        tint = if (userVote == -1) 
                                                            MaterialTheme.colorScheme.error 
                                                        else 
                                                            MaterialTheme.colorScheme.onSurfaceVariant
                                                    )
                                                }
                                                Text(
                                                    "${votes.second}",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    modifier = Modifier.padding(horizontal = 4.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        Text(
                            text = "No reviews yet. Be the first to review this book!",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    
                    // Delete Review Confirmation Dialog
                    showDeleteReviewDialog?.let { reviewId ->
                        AlertDialog(
                            onDismissRequest = { showDeleteReviewDialog = null },
                            title = { Text("Delete Review") },
                            text = { Text("Are you sure you want to delete your review? This action cannot be undone.") },
                            confirmButton = {
                                TextButton(onClick = {
                                    BookBuddyDatabase.deleteReview(reviewId)
                                    showDeleteReviewDialog = null
                                }) {
                                    Text("Delete", color = MaterialTheme.colorScheme.error)
                                }
                            },
                            dismissButton = {
                                TextButton(onClick = { showDeleteReviewDialog = null }) {
                                    Text("Cancel")
                                }
                            }
                        )
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

