package com.example.bookbuddy

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookDetailScreen(
    book: BookWithCategory,
    reviews: List<Review>,
    currentUser: UserProfile?,
    onNavigateBack: () -> Unit,
    onDeleteBook: () -> Unit,
    onSubmitReview: (reviewId: String?, rating: Int, content: String) -> Unit,
    onDeleteReview: (Review) -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    var reviewText by remember { mutableStateOf("") }
    var selectedRating by remember { mutableFloatStateOf(4f) }
    var editingReviewId by remember { mutableStateOf<String?>(null) }
    val isLoggedIn = currentUser != null
    val averageRating = remember(reviews, book.rating) {
        if (reviews.isNotEmpty()) {
            reviews.map { it.rating }.average().toFloat()
        } else {
            book.rating
        }
    }

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
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            // Book Cover
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
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
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

            // Book Information
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                // Title
                Text(
                    text = book.title,
                    style = MaterialTheme.typography.headlineMedium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Author
                Text(
                    text = "by ${book.author}",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Rating
                if (averageRating > 0) {
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
                            text = String.format("%.1f", averageRating),
                            style = MaterialTheme.typography.titleLarge
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // Categories
                if (book.categories.isNotEmpty()) {
                    Text(
                        text = "Categories",
                        style = MaterialTheme.typography.titleMedium
                    )
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
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "Book Details",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        if (book.publishedYear > 0) {
                            DetailRow("Published", book.publishedYear.toString())
                        }
                        if (book.pageCount > 0) {
                            DetailRow("Pages", book.pageCount.toString())
                        }
                        if (book.language.isNotEmpty()) {
                            DetailRow("Language", book.language)
                        }
                        if (book.isbn.isNotEmpty()) {
                            DetailRow("ISBN", book.isbn)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Description
                if (book.description.isNotEmpty()) {
                    Card(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                text = "Description",
                                style = MaterialTheme.typography.titleMedium
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = book.description,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                ReviewComposer(
                    isLoggedIn = isLoggedIn,
                    reviewText = reviewText,
                    rating = selectedRating,
                    isEditing = editingReviewId != null,
                    onReviewTextChange = { reviewText = it },
                    onRatingChange = { selectedRating = it },
                    onSubmit = {
                        if (reviewText.isNotBlank()) {
                            onSubmitReview(editingReviewId, selectedRating.toInt(), reviewText)
                            reviewText = ""
                            selectedRating = 4f
                            editingReviewId = null
                        }
                    },
                    onCancelEdit = {
                        editingReviewId = null
                        reviewText = ""
                        selectedRating = 4f
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))

                ReviewList(
                    reviews = reviews,
                    editableUserId = currentUser?.userId,
                    onEdit = { review ->
                        editingReviewId = review.id
                        reviewText = review.content
                        selectedRating = review.rating.toFloat()
                    },
                    onDelete = onDeleteReview
                )
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
                TextButton(
                    onClick = {
                        onDeleteBook()
                        showDeleteDialog = false
                    }
                ) {
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

@Composable
fun ReviewComposer(
    isLoggedIn: Boolean,
    reviewText: String,
    rating: Float,
    isEditing: Boolean,
    onReviewTextChange: (String) -> Unit,
    onRatingChange: (Float) -> Unit,
    onSubmit: () -> Unit,
    onCancelEdit: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = if (isEditing) "Update your review" else "Leave a review",
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
            if (!isLoggedIn) {
                Text(
                    text = "Log in to write a review.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                Text(text = "Rating: ${rating.toInt()} stars")
                Slider(
                    value = rating,
                    onValueChange = onRatingChange,
                    valueRange = 1f..5f,
                    steps = 3
                )
                OutlinedTextField(
                    value = reviewText,
                    onValueChange = onReviewTextChange,
                    label = { Text("Your thoughts") },
                    placeholder = { Text("Share what stood out to you...") },
                    minLines = 3,
                    maxLines = 6,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = onSubmit,
                        enabled = reviewText.isNotBlank()
                    ) {
                        Text(if (isEditing) "Save changes" else "Post review")
                    }
                    if (isEditing) {
                        OutlinedButton(onClick = onCancelEdit) {
                            Text("Cancel")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ReviewList(
    reviews: List<Review>,
    editableUserId: String?,
    onEdit: (Review) -> Unit,
    onDelete: (Review) -> Unit
) {
    if (reviews.isEmpty()) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    Icons.Default.Star,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text("No reviews yet. Be the first to share your thoughts!")
            }
        }
        return
    }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        reviews.forEach { review ->
            ReviewCard(
                review = review,
                isOwner = editableUserId == review.userId,
                onEdit = { onEdit(review) },
                onDelete = { onDelete(review) }
            )
        }
    }
}

@Composable
fun ReviewCard(
    review: Review,
    isOwner: Boolean,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val dateLabel = remember(review.updatedAt) { formatReviewDate(review.updatedAt) }
    Card {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = review.username,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = dateLabel,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        Icons.Default.Star,
                        contentDescription = null,
                        tint = Color(0xFFFFB300)
                    )
                    Text("${review.rating}")
                }
                if (isOwner) {
                    Row {
                        IconButton(onClick = onEdit) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit review")
                        }
                        IconButton(onClick = onDelete) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = "Delete review",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = review.content,
                style = MaterialTheme.typography.bodyMedium
            )
            if (review.isEdited) {
                Text(
                    text = "Edited",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

private fun formatReviewDate(timestamp: Long): String {
    val formatter = java.text.SimpleDateFormat("MMM dd, yyyy", java.util.Locale.getDefault())
    return formatter.format(java.util.Date(timestamp))
}
