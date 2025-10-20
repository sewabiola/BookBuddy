package com.example.bookbuddy

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

// Task 54: 2-2-1-1 Implement book categorization system

// Categorization Manager
object BookCategorization {

    private val mainCategories = listOf(
        BookCategory(
            id = "fiction",
            name = "Fiction",
            description = "Imaginative narratives and stories",
            iconResource = "book_fiction",
            subcategories = listOf(
                BookCategory(id = "literary", name = "Literary Fiction", description = "Character-driven narratives"),
                BookCategory(id = "scifi", name = "Science Fiction", description = "Futuristic and scientific themes"),
                BookCategory(id = "fantasy", name = "Fantasy", description = "Magic and mythical worlds"),
                BookCategory(id = "mystery", name = "Mystery", description = "Crime and investigation"),
                BookCategory(id = "thriller", name = "Thriller", description = "Suspense and excitement"),
                BookCategory(id = "romance", name = "Romance", description = "Love stories"),
                BookCategory(id = "horror", name = "Horror", description = "Fear and suspense")
            )
        ),
        BookCategory(
            id = "nonfiction",
            name = "Non-Fiction",
            description = "Real-world information and facts",
            iconResource = "book_nonfiction",
            subcategories = listOf(
                BookCategory(id = "biography", name = "Biography", description = "Life stories"),
                BookCategory(id = "memoir", name = "Memoir", description = "Personal experiences"),
                BookCategory(id = "history", name = "History", description = "Historical events and periods"),
                BookCategory(id = "science", name = "Science", description = "Scientific topics"),
                BookCategory(id = "selfhelp", name = "Self-Help", description = "Personal development"),
                BookCategory(id = "business", name = "Business", description = "Business and economics"),
                BookCategory(id = "philosophy", name = "Philosophy", description = "Philosophical thought")
            )
        ),
        BookCategory(
            id = "academic",
            name = "Academic",
            description = "Educational and scholarly works",
            iconResource = "book_academic",
            subcategories = listOf(
                BookCategory(id = "textbook", name = "Textbook", description = "Educational materials"),
                BookCategory(id = "reference", name = "Reference", description = "Reference materials"),
                BookCategory(id = "research", name = "Research", description = "Research papers"),
                BookCategory(id = "education", name = "Education", description = "Learning resources")
            )
        ),
        BookCategory(
            id = "young_adult",
            name = "Young Adult",
            description = "Books for teenage readers",
            iconResource = "book_ya"
        ),
        BookCategory(
            id = "childrens",
            name = "Children's",
            description = "Books for young readers",
            iconResource = "book_children"
        ),
        BookCategory(
            id = "poetry",
            name = "Poetry",
            description = "Verse and poetic works",
            iconResource = "book_poetry"
        ),
        BookCategory(
            id = "graphic_novel",
            name = "Graphic Novel",
            description = "Visual storytelling",
            iconResource = "book_graphic"
        ),
        BookCategory(
            id = "classics",
            name = "Classics",
            description = "Timeless literary works",
            iconResource = "book_classics"
        )
    )

    fun getAllCategories(): List<BookCategory> = mainCategories

    fun getCategoryById(id: String): BookCategory? {
        return mainCategories.find { it.id == id }
            ?: mainCategories.flatMap { it.subcategories }.find { it.id == id }
    }

    fun getSubcategories(parentId: String): List<BookCategory> {
        return mainCategories.find { it.id == parentId }?.subcategories ?: emptyList()
    }

    fun categorizeBook(book: BookWithCategory): List<BookCategory> {
        return book.categories.mapNotNull { categoryName ->
            mainCategories.find { it.name.equals(categoryName, ignoreCase = true) }
                ?: mainCategories.flatMap { it.subcategories }
                    .find { it.name.equals(categoryName, ignoreCase = true) }
        }
    }

    fun searchCategories(query: String): List<BookCategory> {
        val lowerQuery = query.lowercase()
        return mainCategories.filter {
            it.name.lowercase().contains(lowerQuery) ||
            it.description.lowercase().contains(lowerQuery)
        } + mainCategories.flatMap { it.subcategories }.filter {
            it.name.lowercase().contains(lowerQuery) ||
            it.description.lowercase().contains(lowerQuery)
        }
    }

    fun getBooksInCategory(categoryId: String, allBooks: List<BookWithCategory>): List<BookWithCategory> {
        val category = getCategoryById(categoryId) ?: return emptyList()
        return allBooks.filter { book ->
            book.categories.any { it.equals(category.name, ignoreCase = true) } ||
            category.subcategories.any { sub ->
                book.categories.any { it.equals(sub.name, ignoreCase = true) }
            }
        }
    }
}

// Category Browser Screen
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryBrowserScreen(
    onCategorySelected: (BookCategory) -> Unit,
    onNavigateBack: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var isSearching by remember { mutableStateOf(false) }

    val categories = remember { BookCategorization.getAllCategories() }
    val filteredCategories = remember(searchQuery) {
        if (searchQuery.isBlank()) {
            categories
        } else {
            BookCategorization.searchCategories(searchQuery)
        }
    }

    Scaffold(
        topBar = {
            if (isSearching) {
                SearchBar(
                    query = searchQuery,
                    onQueryChange = { searchQuery = it },
                    onSearch = { },
                    active = true,
                    onActiveChange = { isSearching = it },
                    placeholder = { Text("Search categories") },
                    leadingIcon = {
                        IconButton(onClick = {
                            isSearching = false
                            searchQuery = ""
                        }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                        }
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Close, contentDescription = "Clear")
                            }
                        }
                    }
                ) {
                    CategorySearchResults(
                        categories = filteredCategories,
                        onCategorySelected = onCategorySelected
                    )
                }
            } else {
                TopAppBar(
                    title = { Text("Browse Categories") },
                    navigationIcon = {
                        IconButton(onClick = onNavigateBack) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                        }
                    },
                    actions = {
                        IconButton(onClick = { isSearching = true }) {
                            Icon(Icons.Default.Search, contentDescription = "Search")
                        }
                    }
                )
            }
        }
    ) { padding ->
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(filteredCategories) { category ->
                CategoryCard(
                    category = category,
                    onClick = { onCategorySelected(category) }
                )
            }
        }
    }
}

@Composable
fun CategoryCard(
    category: BookCategory,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Category Icon (placeholder)
            Icon(
                imageVector = when (category.id) {
                    "fiction" -> Icons.Default.Star
                    "nonfiction" -> Icons.Default.Star
                    "academic" -> Icons.Default.Settings
                    "poetry" -> Icons.Default.Edit
                    else -> Icons.Default.Star
                },
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = category.name,
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center
            )

            if (category.booksCount > 0) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${category.booksCount} books",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (category.subcategories.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                Badge {
                    Text("${category.subcategories.size} subcategories")
                }
            }
        }
    }
}

@Composable
fun CategorySearchResults(
    categories: List<BookCategory>,
    onCategorySelected: (BookCategory) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
    ) {
        if (categories.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No categories found",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            categories.forEach { category ->
                ListItem(
                    headlineContent = { Text(category.name) },
                    supportingContent = { Text(category.description) },
                    leadingContent = {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null
                        )
                    },
                    modifier = Modifier.clickable { onCategorySelected(category) }
                )
                Divider()
            }
        }
    }
}

// Category Detail Screen
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryDetailScreen(
    category: BookCategory,
    books: List<BookWithCategory>,
    onNavigateBack: () -> Unit,
    onBookClick: (BookWithCategory) -> Unit
) {
    val booksInCategory = remember(category, books) {
        BookCategorization.getBooksInCategory(category.id, books)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(category.name) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
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
            // Category Info Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = category.description,
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "${booksInCategory.size} books in this category",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            // Subcategories
            if (category.subcategories.isNotEmpty()) {
                Text(
                    text = "Subcategories",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    category.subcategories.forEach { subcategory ->
                        FilterChip(
                            selected = false,
                            onClick = { /* Navigate to subcategory */ },
                            label = { Text(subcategory.name) }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Books in Category
            if (booksInCategory.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.Star,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "No books in this category yet",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(booksInCategory.size) { index ->
                        val book = booksInCategory[index]
                        BookGridItem(book = book, onClick = { onBookClick(book) })
                    }
                }
            }
        }
    }
}

@Composable
fun BookGridItem(
    book: BookWithCategory,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Icon(
                Icons.Default.Star,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = book.title,
                style = MaterialTheme.typography.titleSmall,
                maxLines = 2
            )
            Text(
                text = book.author,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1
            )
        }
    }
}
