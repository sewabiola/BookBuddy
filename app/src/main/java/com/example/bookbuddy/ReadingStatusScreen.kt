package com.example.bookbuddy

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReadingStatusScreen(
    navController: NavHostController
) {
    val allBooks by BookBuddyDatabase.observeBooks().collectAsState()
    var selectedStatus by remember { mutableStateOf("All") }

    val filteredBooks = remember(allBooks, selectedStatus) {
        when (selectedStatus) {
            "All" -> allBooks
            else -> allBooks.filter { it.readingStatus == selectedStatus }
        }
    }

    val statusCounts = remember(allBooks) {
        mapOf(
            "All" to allBooks.size,
            "Not Started" to allBooks.count { it.readingStatus == "Not Started" },
            "Reading" to allBooks.count { it.readingStatus == "Reading" },
            "Read" to allBooks.count { it.readingStatus == "Read" }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Reading Status") },
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
        ) {
            // Statistics Cards
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Reading Progress",
                        style = MaterialTheme.typography.titleLarge
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        StatusStatCard(
                            label = "Not Started",
                            count = statusCounts["Not Started"] ?: 0,
                            icon = Icons.Default.Add
                        )
                        StatusStatCard(
                            label = "Reading",
                            count = statusCounts["Reading"] ?: 0,
                            icon = Icons.Default.Edit
                        )
                        StatusStatCard(
                            label = "Read",
                            count = statusCounts["Read"] ?: 0,
                            icon = Icons.Default.Check
                        )
                    }

                    // Progress Bar
                    if (allBooks.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(16.dp))
                        val readPercentage = (statusCounts["Read"] ?: 0).toFloat() / allBooks.size
                        Column {
                            LinearProgressIndicator(
                                progress = readPercentage,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "${(readPercentage * 100).toInt()}% Complete",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // Filter Tabs
            TabRow(
                selectedTabIndex = when (selectedStatus) {
                    "All" -> 0
                    "Not Started" -> 1
                    "Reading" -> 2
                    "Read" -> 3
                    else -> 0
                }
            ) {
                Tab(
                    selected = selectedStatus == "All",
                    onClick = { selectedStatus = "All" },
                    text = { Text("All (${statusCounts["All"]})") }
                )
                Tab(
                    selected = selectedStatus == "Not Started",
                    onClick = { selectedStatus = "Not Started" },
                    text = { Text("Not Started (${statusCounts["Not Started"]})") }
                )
                Tab(
                    selected = selectedStatus == "Reading",
                    onClick = { selectedStatus = "Reading" },
                    text = { Text("Reading (${statusCounts["Reading"]})") }
                )
                Tab(
                    selected = selectedStatus == "Read",
                    onClick = { selectedStatus = "Read" },
                    text = { Text("Read (${statusCounts["Read"]})") }
                )
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
                            Icons.Default.Star,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "No books in this category",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Add some books to get started",
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
                            onClick = {
                                navController.navigate("book_details/${book.id}")
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun StatusStatCard(
    label: String,
    count: Int,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSecondaryContainer
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = count.toString(),
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
        }
    }
}
