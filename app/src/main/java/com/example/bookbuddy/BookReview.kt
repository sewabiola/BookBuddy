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
import androidx.compose.ui.unit.dp
import java.util.*
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import java.util.UUID


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookReviewScreen(
    book: BookWithCategory,
    onReviewAdded: (BookReview) -> Unit,
    onNavigateBack: () -> Unit
) {
    var rating by remember { mutableStateOf(0f) }
    var reviewText by remember { mutableStateOf("") }
    var validationError by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Review: ${book.title}") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = {
                    when {
                        rating <= 0f -> validationError = "Please provide a rating"
                        reviewText.isBlank() -> validationError = "Please enter your review"
                        else -> {
                            val newReview = BookReview(
                                id = UUID.randomUUID().toString(),
                                bookId = book.id,
                                reviewerName = BookBuddyDatabase.getCurrentUser()?.displayName ?: "Anonymous",
                                rating = rating,
                                comment = reviewText,
                                date = Date().toString()
                            )
                            BookBuddyDatabase.addReview(newReview)
                            onReviewAdded(newReview)
                            onNavigateBack()
                        }
                    }
                },
                icon = { Icon(Icons.Default.Send, contentDescription = null) },
                text = { Text("Submit Review") }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            validationError?.let { error ->
                Text(
                    text = error,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            Text(
                text = "Rate this book:",
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(8.dp))

            RatingBar(
                rating = rating,
                onRatingChanged = { rating = it }
            )

            Spacer(modifier = Modifier.height(24.dp))

            OutlinedTextField(
                value = reviewText,
                onValueChange = {
                    reviewText = it
                    validationError = null
                },
                label = { Text("Write your review") },
                placeholder = { Text("What did you think about this book?") },
                leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) },
                modifier = Modifier.fillMaxWidth(),
                minLines = 5,
                maxLines = 8
            )
        }
    }
}

@Composable
fun RatingBar(
    rating: Float,
    onRatingChanged: (Float) -> Unit
) {
    Row(
        horizontalArrangement = Arrangement.Center,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp)
    ) {
        val filledColor = MaterialTheme.colorScheme.primary
        val emptyColor = MaterialTheme.colorScheme.onSurfaceVariant

        for (i in 1..5) {
            IconButton(onClick = { onRatingChanged(i.toFloat()) }) {
                val color = if (i <= rating) filledColor else emptyColor
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = "Rating $i",
                    tint = color
                )
            }
        }
    }
}

data class BookReview(
    val id: String,
    val bookId: String,
    val reviewerName: String,
    val username: String = "",
    val rating: Float,
    val comment: String,
    val date: String
)


