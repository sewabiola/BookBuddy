package com.example.bookbuddy

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.Alignment
import androidx.navigation.NavController
import androidx.navigation.NavHostController

// --- Main Collections Screen ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CollectionsScreenWithSeeMore(
    navController: NavHostController,
    collections: List<BookCollection>,
    booksWithCategory: List<BookWithCategory>,
    onBookClick: (BookWithCategory) -> Unit,
    onCollectionClick: (BookCollection) -> Unit,
    onBookDelete: (BookWithCategory) -> Unit,
    onSeeMoreCollections: () -> Unit
) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("BookBuddy") },
                actions = {
                    TextButton(onClick = { navController.navigate("stats") }) {
                        Text(
                            text = "View Stats",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            )
        },
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
                onAddBookClick = { navController.navigate("add_book") },
                onProfileClick = { navController.navigate("profile") },
                onBookDelete = onBookDelete
            )
        }
    }
}

// --- All Collections Screen with Add/Edit/Delete ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AllCollectionsScreen(
    navController: NavController,
    onBack: () -> Unit
) {
    var collections by remember { mutableStateOf(BookBuddyDatabase.getUserCollections()) }
    var showAddDialog by remember { mutableStateOf(false) }
    var editingCollection by remember { mutableStateOf<BookCollection?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("All Collections") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
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
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            items(collections, key = { it.title }) { col ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                ) {
                    Box(modifier = Modifier.fillMaxWidth()) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp)
                        ) {
                            Text(col.title, style = MaterialTheme.typography.titleMedium)
                            col.books.forEach { book ->
                                Text(
                                    "• ${book.title} by ${book.author}",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }

                        Row(
                            modifier = Modifier.align(Alignment.TopEnd)
                        ) {
                            IconButton(
                                onClick = { editingCollection = col },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(Icons.Default.Edit, contentDescription = "Edit Collection")
                            }

                            IconButton(
                                onClick = {
                                    BookBuddyDatabase.deleteCollection(col)
                                    collections = BookBuddyDatabase.getUserCollections()
                                },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete Collection")
                            }
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
                val newCollection = BookCollection(
                    title = name,
                    books = selectedBooks.map { Book(it.title, it.author) }
                )
                BookBuddyDatabase.createCollection(newCollection)
                collections = BookBuddyDatabase.getUserCollections()
                showAddDialog = false
            }
        )
    }

    editingCollection?.let { col ->
        EditCollectionDialog(
            collection = col,
            onDismiss = { editingCollection = null },
            onUpdate = { updatedCol ->
                BookBuddyDatabase.updateCollection(col.title, updatedCol)
                collections = BookBuddyDatabase.getUserCollections()
                editingCollection = null
            }
        )
    }
}

// --- Add Collection Dialog ---
@Composable
fun AddCollectionDialog(
    onDismiss: () -> Unit,
    onCreate: (String, List<BookWithCategory>) -> Unit
) {
    var collectionName by remember { mutableStateOf("") }
    val allBooks = BookBuddyDatabase.getUserBooks()
    val selectedBooks = remember { mutableStateListOf<BookWithCategory>() }

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
                            val isSelected = selectedBooks.contains(book)
                            Checkbox(
                                checked = isSelected,
                                onCheckedChange = { checked ->
                                    if (checked) selectedBooks.add(book)
                                    else selectedBooks.remove(book)
                                }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("${book.title} by ${book.author}")
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                if (collectionName.isNotBlank()) {
                    onCreate(collectionName, selectedBooks.toList())
                }
            }) { Text("Create") }
        },
        dismissButton = { Button(onClick = onDismiss) { Text("Cancel") } }
    )
}

// --- Edit Collection Dialog ---
@Composable
fun EditCollectionDialog(
    collection: BookCollection,
    onDismiss: () -> Unit,
    onUpdate: (BookCollection) -> Unit
) {
    var collectionName by remember { mutableStateOf(collection.title) }
    val allBooks = BookBuddyDatabase.getUserBooks()
    val selectedBooks = remember { mutableStateListOf<BookWithCategory>() }

    LaunchedEffect(collection) {
        selectedBooks.clear()
        selectedBooks.addAll(
            collection.books.mapNotNull { book ->
                allBooks.find { it.title == book.title && it.author == book.author }
            }
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Collection") },
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
                            val isSelected = selectedBooks.contains(book)
                            Checkbox(
                                checked = isSelected,
                                onCheckedChange = { checked ->
                                    if (checked) selectedBooks.add(book)
                                    else selectedBooks.remove(book)
                                }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("${book.title} by ${book.author}")
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                if (collectionName.isNotBlank()) {
                    val updatedCollection = BookCollection(
                        title = collectionName,
                        books = selectedBooks.map { Book(it.title, it.author) }
                    )
                    onUpdate(updatedCollection)
                }
            }) { Text("Update") }
        },
        dismissButton = { Button(onClick = onDismiss) { Text("Cancel") } }
    )
}