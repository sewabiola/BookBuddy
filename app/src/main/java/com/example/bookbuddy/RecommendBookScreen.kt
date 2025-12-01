package com.example.bookbuddy

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavController
import coil.compose.AsyncImage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecommendationScreen(navController: NavController, onBookClick: (BookWithCategory) -> Unit = {}) {
    val currentUser = BookBuddyDatabase.getCurrentUser()
    val allBooks by BookBuddyDatabase.observeBooks().collectAsState()
    val collaborativeRecommendations by BookBuddyDatabase.observeRecommendations().collectAsState()
    
    // Get recommendations from both engines
    val contentBasedRecommendations = remember(currentUser, allBooks) {
        if (currentUser != null && allBooks.isNotEmpty()) {
            RecommendationEngine.recommendFor(currentUser, allBooks)
        } else {
            emptyList()
        }
    }
    
    // Combine both recommendation types, prioritizing collaborative
    val allRecommendations = remember(collaborativeRecommendations, contentBasedRecommendations) {
        val combined = (collaborativeRecommendations + contentBasedRecommendations)
            .distinctBy { it.id }
            .take(20)
        if (combined.isEmpty() && allBooks.isNotEmpty()) {
            // Fallback: show top-rated books
            allBooks.sortedByDescending { it.rating }.take(10)
        } else {
            combined
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Recommendations") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            Text(
                text = "Your Personalized Book Suggestions",
                style = MaterialTheme.typography.titleLarge
            )
            
            Spacer(Modifier.height(8.dp))
            
            if (collaborativeRecommendations.isNotEmpty()) {
                Text(
                    text = "Based on similar readers",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            } else if (contentBasedRecommendations.isNotEmpty()) {
                Text(
                    text = "Based on your reading history",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            } else {
                Text(
                    text = "Top-rated books",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(Modifier.height(16.dp))

            if (allRecommendations.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Default.Star,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.height(16.dp))
                        Text(
                            text = "No recommendations available yet",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = "Start reading and rating books to get personalized recommendations!",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(allRecommendations, key = { it.id }) { book ->
                        RecommendationBookCard(
                            book = book,
                            onClick = {
                                onBookClick(book)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun RecommendationBookCard(
    book: BookWithCategory,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Book Cover
            Box(
                modifier = Modifier
                    .size(80.dp, 100.dp)
                    .clip(RoundedCornerShape(8.dp)),
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
                    Icon(
                        Icons.Default.Star,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            Spacer(Modifier.width(16.dp))
            
            // Book Info
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = book.title,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "by ${book.author}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(8.dp))
                
                if (book.categories.isNotEmpty()) {
                    Text(
                        text = book.categories.take(2).joinToString(", "),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                
                Spacer(Modifier.height(4.dp))
                
                if (book.rating > 0) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Star,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = String.format("%.1f", book.rating),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }
    }
}
