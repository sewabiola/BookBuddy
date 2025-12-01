package com.example.bookbuddy

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import kotlinx.coroutines.launch

// Screen for viewing all collections
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AllCollectionsScreen(
    navController: NavHostController,
    onBack: () -> Unit
) {
    val collections by BookBuddyDatabase.observeCollections().collectAsState()
    val books by BookBuddyDatabase.observeBooks().collectAsState()
    var showCreateDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf<BookCollection?>(null) }
    var showEditDialog by remember { mutableStateOf<BookCollection?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Collections") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showCreateDialog = true },
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("New Collection") }
            )
        }
    ) { padding ->
        if (collections.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        Icons.Default.List,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "No collections yet",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Create your first collection to get started",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(collections) { collection ->
                    CollectionItemCard(
                        collection = collection,
                        onEdit = { showEditDialog = collection },
                        onDelete = { showDeleteDialog = collection },
                        onClick = {
                            navController.navigate("collection_details/${collection.title}")
                        }
                    )
                }
            }
        }
    }

    // Create Collection Dialog
    if (showCreateDialog) {
        CreateCollectionDialog(
            onDismiss = { showCreateDialog = false },
            onConfirm = { name ->
                val newCollection = BookCollection(
                    title = name,
                    books = emptyList()
                )
                if (BookBuddyDatabase.createCollection(newCollection)) {
                    scope.launch {
                        snackbarHostState.showSnackbar("Collection created successfully!")
                    }
                    showCreateDialog = false
                } else {
                    scope.launch {
                        snackbarHostState.showSnackbar("Failed to create collection")
                    }
                }
            }
        )
    }

    // Edit Collection Dialog
    showEditDialog?.let { collection ->
        EditCollectionDialog(
            collection = collection,
            onDismiss = { showEditDialog = null },
            onConfirm = { newName ->
                val updatedCollection = collection.copy(title = newName)
                if (BookBuddyDatabase.updateCollection(collection.title, updatedCollection)) {
                    scope.launch {
                        snackbarHostState.showSnackbar("Collection updated successfully!")
                    }
                    showEditDialog = null
                } else {
                    scope.launch {
                        snackbarHostState.showSnackbar("Failed to update collection")
                    }
                }
            }
        )
    }

    // Delete Collection Dialog
    showDeleteDialog?.let { collection ->
        AlertDialog(
            onDismissRequest = { showDeleteDialog = null },
            title = { Text("Delete Collection") },
            text = { Text("Are you sure you want to delete \"${collection.title}\"? This action cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (BookBuddyDatabase.deleteCollection(collection)) {
                            scope.launch {
                                snackbarHostState.showSnackbar("Collection deleted")
                            }
                        }
                        showDeleteDialog = null
                    }
                ) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun CollectionItemCard(
    collection: BookCollection,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = collection.title,
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${collection.books.size} books",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Row {
                IconButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit")
                }
                IconButton(onClick = onDelete) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

@Composable
fun CreateCollectionDialog(
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Create New Collection") },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        error = null
                    },
                    label = { Text("Collection Name") },
                    placeholder = { Text("e.g., Favorites, To Read, etc.") },
                    singleLine = true,
                    isError = error != null,
                    modifier = Modifier.fillMaxWidth()
                )
                if (error != null) {
                    Text(
                        text = error!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    when {
                        name.isBlank() -> error = "Please enter a collection name"
                        else -> onConfirm(name)
                    }
                }
            ) {
                Text("Create")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun EditCollectionDialog(
    collection: BookCollection,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var name by remember { mutableStateOf(collection.title) }
    var error by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Collection") },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        error = null
                    },
                    label = { Text("Collection Name") },
                    singleLine = true,
                    isError = error != null,
                    modifier = Modifier.fillMaxWidth()
                )
                if (error != null) {
                    Text(
                        text = error!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    when {
                        name.isBlank() -> error = "Please enter a collection name"
                        else -> onConfirm(name)
                    }
                }
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

// Screen for viewing a specific collection's books and managing them
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CollectionDetailsScreen(
    navController: NavHostController,
    collectionTitle: String
) {
    val collections by BookBuddyDatabase.observeCollections().collectAsState()
    val allBooks by BookBuddyDatabase.observeBooks().collectAsState()
    val collection = collections.find { it.title == collectionTitle }

    var showAddBookDialog by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    if (collection == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text("Collection not found")
        }
        return
    }

    // Get full book details for books in collection
    val collectionBooks = remember(collection.books, allBooks) {
        allBooks.filter { book ->
            collection.books.any { it.id == book.id }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(collection.title) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showAddBookDialog = true },
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("Add Books") }
            )
        }
    ) { padding ->
        if (collectionBooks.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
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
                        text = "No books in this collection",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Tap the button below to add books",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(collectionBooks) { book ->
                    BookInCollectionCard(
                        book = book,
                        onRemove = {
                            if (BookBuddyDatabase.removeBookFromCollection(collectionTitle, book.id)) {
                                scope.launch {
                                    snackbarHostState.showSnackbar("Book removed from collection")
                                }
                            }
                        },
                        onClick = {
                            navController.navigate("book_details/${book.id}")
                        }
                    )
                }
            }
        }
    }

    // Add Book Dialog
    if (showAddBookDialog) {
        AddBookToCollectionDialog(
            collectionTitle = collectionTitle,
            availableBooks = allBooks.filter { book ->
                !collection.books.any { it.id == book.id }
            },
            onDismiss = { showAddBookDialog = false },
            onBookAdded = { book ->
                if (BookBuddyDatabase.addBookToCollection(collectionTitle, book)) {
                    scope.launch {
                        snackbarHostState.showSnackbar("Book added to collection")
                    }
                }
            }
        )
    }
}

@Composable
fun BookInCollectionCard(
    book: BookWithCategory,
    onRemove: () -> Unit,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Book cover or placeholder
            Card(
                modifier = Modifier.size(60.dp),
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
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = book.title,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 2
                )
                Text(
                    text = book.author,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            IconButton(onClick = onRemove) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Remove from collection",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
fun AddBookToCollectionDialog(
    collectionTitle: String,
    availableBooks: List<BookWithCategory>,
    onDismiss: () -> Unit,
    onBookAdded: (BookWithCategory) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }

    val filteredBooks = remember(searchQuery, availableBooks) {
        if (searchQuery.isBlank()) {
            availableBooks
        } else {
            availableBooks.filter {
                it.title.contains(searchQuery, ignoreCase = true) ||
                it.author.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Books to $collectionTitle") },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    label = { Text("Search books") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                if (filteredBooks.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (availableBooks.isEmpty()) {
                                "All books are already in this collection"
                            } else {
                                "No books found"
                            },
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 300.dp)
                    ) {
                        items(filteredBooks) { book ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .clickable {
                                        onBookAdded(book)
                                        onDismiss()
                                    }
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = book.title,
                                            style = MaterialTheme.typography.bodyLarge
                                        )
                                        Text(
                                            text = book.author,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Icon(
                                        Icons.Default.Add,
                                        contentDescription = "Add",
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Done")
            }
        }
    )
}
