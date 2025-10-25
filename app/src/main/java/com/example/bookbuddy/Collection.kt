package com.example.bookbuddy

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController

@Composable
fun CollectionsScreenWithSeeMore(
    navController: NavHostController,
    collections: List<BookCollection>,
    booksWithCategory: List<BookWithCategory>,
    onBookClick: (BookWithCategory) -> Unit,
    onCollectionClick: (BookCollection) -> Unit,
    onAddBookClick: () -> Unit,
    onProfileClick: () -> Unit,
    onBookDelete: (BookWithCategory) -> Unit,
    onSeeMoreCollections: () -> Unit
) {
    Scaffold(
        bottomBar = {
            Button(
                onClick = onSeeMoreCollections,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text("See More Collections")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            EnhancedCollectionDisplay(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                collections = collections,
                booksWithCategory = booksWithCategory,
                onBookClick = onBookClick,
                onCollectionClick = onCollectionClick,
                onAddBookClick = onAddBookClick,
                onProfileClick = onProfileClick,
                onBookDelete = onBookDelete
            )
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AllCollectionsScreen(
    onBack: () -> Unit
) {
    var collections by remember { mutableStateOf(BookBuddyDatabase.getUserCollections()) }
    var showAddDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("All Collections") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.Person, contentDescription = "Back")
                    }
                },
                actions = {
                    Button(onClick = { showAddDialog = true }) {
                        Text("Add Collection")
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .padding(paddingValues)
                .padding(16.dp)
        ) {
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

    if (showAddDialog) {
        AddCollectionDialog(
            onDismiss = { showAddDialog = false },
            onCreate = { name, selectedBooks ->
                val newCollection = BookCollection(title = name, books = selectedBooks)
                BookBuddyDatabase.createCollection(newCollection)
                collections = BookBuddyDatabase.getUserCollections()
                showAddDialog = false
            }
        )
    }
}
@Composable
fun AddCollectionDialog(
    onDismiss: () -> Unit,
    onCreate: (String, List<Book>) -> Unit
) {
    var collectionName by remember { mutableStateOf("") }
    val allBooks = BookBuddyDatabase.getUserBooks()
    val selectedBooks = remember { mutableStateListOf<Book>() }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add New Collection") },
        text = {
            Column {
                OutlinedTextField(
                    value = collectionName,
                    onValueChange = { collectionName = it },
                    label = { Text("Collection Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))
                Text("Select Books", style = MaterialTheme.typography.titleMedium)

                LazyColumn(
                    modifier = Modifier
                        .heightIn(max = 300.dp)
                        .padding(vertical = 8.dp)
                ) {
                    items(allBooks, key = { it.id }) { book ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        ) {
                            val isSelected = selectedBooks.contains(Book(book.title, book.author))
                            Checkbox(
                                checked = isSelected,
                                onCheckedChange = { checked ->
                                    if (checked) selectedBooks.add(Book(book.title, book.author))
                                    else selectedBooks.remove(Book(book.title, book.author))
                                }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("${book.title} by ${book.author}", style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (collectionName.isNotBlank()) {
                        onCreate(collectionName, selectedBooks.toList())
                    }
                }
            ) {
                Text("Create")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
