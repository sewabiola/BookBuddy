package com.example.bookbuddy

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.lazy.items
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.lazy.LazyColumn
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import java.util.*

// Task 51: 2-1-1-1 Create book addition interface
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookAdditionScreen(
    onBookAdded: (BookWithCategory) -> Unit,
    onNavigateBack: () -> Unit
) {
    var book by remember { mutableStateOf(BookWithCategory(id = UUID.randomUUID().toString(), title = "", author = "", categories = emptyList())) }
    var selectedCategories by remember { mutableStateOf<List<String>>(emptyList()) }
    var coverImageUri by remember { mutableStateOf<String?>(null) }
    var showCategoryDialog by remember { mutableStateOf(false) }
    var isSearching by remember { mutableStateOf(false) }
    var validationError by remember { mutableStateOf<String?>(null) }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let { coverImageUri = it.toString() }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add New Book") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { isSearching = !isSearching }) {
                        Icon(Icons.Default.Search, contentDescription = "Search Books")
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = {
                    when {
                        book.title.isBlank() -> validationError = "Please enter a book title"
                        book.author.isBlank() -> validationError = "Please enter an author name"
                        selectedCategories.isEmpty() -> validationError = "Please select at least one category"
                        else -> {
                            val newBook = book.copy(
                                categories = selectedCategories,
                                coverImageUrl = coverImageUri ?: ""
                            )
                            onBookAdded(newBook)
                            onNavigateBack()
                        }
                    }
                },
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("Add Book") }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Search Bar (Expandable)
            AnimatedVisibility(visible = isSearching) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        OutlinedTextField(
                            value = "",
                            onValueChange = { /* TODO: Implement book search API */ },
                            label = { Text("Search by ISBN or Title") },
                            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                            placeholder = { Text("Enter ISBN or book title") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Search online databases to auto-fill book information",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Book Cover Image
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                onClick = { imagePickerLauncher.launch("image/*") },
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    if (coverImageUri != null) {
                        AsyncImage(
                            model = coverImageUri,
                            contentDescription = "Book Cover",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                Icons.Default.Add,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Add Book Cover",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Validation Error
            validationError?.let { error ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Warning,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onErrorContainer
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = error,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Title Field
            OutlinedTextField(
                value = book.title,
                onValueChange = {
                    book = book.copy(title = it)
                    validationError = null
                },
                label = { Text("Book Title *") },
                leadingIcon = { Icon(Icons.Default.Star, contentDescription = null) },
                placeholder = { Text("Enter book title") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Author Field
            OutlinedTextField(
                value = book.author,
                onValueChange = {
                    book = book.copy(author = it)
                    validationError = null
                },
                label = { Text("Author *") },
                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                placeholder = { Text("Enter author name") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // ISBN Field
            OutlinedTextField(
                value = book.isbn,
                onValueChange = { book = book.copy(isbn = it) },
                label = { Text("ISBN (Optional)") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                placeholder = { Text("Enter ISBN") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Categories Selection
            OutlinedCard(
                onClick = { showCategoryDialog = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Categories *",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        if (selectedCategories.isEmpty()) {
                            Text(
                                text = "Select categories",
                                style = MaterialTheme.typography.bodyLarge
                            )
                        } else {
                            Text(
                                text = selectedCategories.joinToString(", "),
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Description Field
            OutlinedTextField(
                value = book.description,
                onValueChange = { book = book.copy(description = it) },
                label = { Text("Description (Optional)") },
                leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) },
                placeholder = { Text("Enter book description or synopsis") },
                minLines = 4,
                maxLines = 8,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Additional Fields Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Published Year
                OutlinedTextField(
                    value = if (book.publishedYear > 0) book.publishedYear.toString() else "",
                    onValueChange = {
                        book = book.copy(publishedYear = it.toIntOrNull() ?: 0)
                    },
                    label = { Text("Year") },
                    placeholder = { Text("2024") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f)
                )

                // Page Count
                OutlinedTextField(
                    value = if (book.pageCount > 0) book.pageCount.toString() else "",
                    onValueChange = {
                        book = book.copy(pageCount = it.toIntOrNull() ?: 0)
                    },
                    label = { Text("Pages") },
                    placeholder = { Text("300") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Language Field
            OutlinedTextField(
                value = book.language,
                onValueChange = { book = book.copy(language = it) },
                label = { Text("Language") },
                leadingIcon = { Icon(Icons.Default.Settings, contentDescription = null) },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(80.dp)) // Space for FAB
        }
    }

    // Category Selection Dialog
    if (showCategoryDialog) {
        CategorySelectionDialog(
            selectedCategories = selectedCategories,
            onCategoriesSelected = {
                selectedCategories = it
                validationError = null
            },
            onDismiss = { showCategoryDialog = false }
        )
    }
}

@Composable
fun CategorySelectionDialog(
    selectedCategories: List<String>,
    onCategoriesSelected: (List<String>) -> Unit,
    onDismiss: () -> Unit
) {
    var tempSelected by remember { mutableStateOf(selectedCategories) }

    val availableCategories = listOf(
        "Fiction", "Non-Fiction", "Mystery", "Thriller", "Romance",
        "Science Fiction", "Fantasy", "Biography", "History",
        "Self-Help", "Business", "Poetry", "Horror", "Adventure",
        "Young Adult", "Classics", "Graphic Novel", "Memoir",
        "Philosophy", "Science", "Travel", "Cooking", "Art",
        "Religion", "Education", "Technology"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select Categories") },
        text = {
            LazyColumn(
                modifier = Modifier.fillMaxWidth()
            ) {
                items(availableCategories.size) { index ->
                    val category = availableCategories[index]
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = tempSelected.contains(category),
                            onCheckedChange = { checked ->
                                tempSelected = if (checked) {
                                    tempSelected + category
                                } else {
                                    tempSelected - category
                                }
                            }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(category)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                onCategoriesSelected(tempSelected)
                onDismiss()
            }) {
                Text("Confirm")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
