package com.example.bookbuddy

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
//import androidx.compose.ui.unit.dp
//import coil.compose.AsyncImage

// Task 55: 2-2-1-2 Design book collection display

// Enhanced Collection Display with multiple view modes
enum class ViewMode {
    GRID,
    LIST,
    COMPACT
}

enum class SortOption {
    TITLE,
    AUTHOR,
    DATE_ADDED,
    RATING
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EnhancedCollectionDisplay(
    collections: List<BookCollection>,
    booksWithCategory: List<BookWithCategory> = emptyList(),
    onBookClick: (BookWithCategory) -> Unit = {},
    onCollectionClick: (BookCollection) -> Unit = {},
    onAddBookClick: () -> Unit = {},
    onProfileClick: () -> Unit = {}
) {
    var viewMode by remember { mutableStateOf(ViewMode.LIST) }
    var sortOption by remember { mutableStateOf(SortOption.TITLE) }
    var showSortMenu by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var isSearching by remember { mutableStateOf(false) }

    val filteredBooks = remember(searchQuery, booksWithCategory) {
        if (searchQuery.isBlank()) {
            booksWithCategory
        } else {
            booksWithCategory.filter {
                it.title.contains(searchQuery, ignoreCase = true) ||
                it.author.contains(searchQuery, ignoreCase = true) ||
                it.categories.any { cat -> cat.contains(searchQuery, ignoreCase = true) }
            }
        }
    }

    val sortedBooks = remember(sortOption, filteredBooks) {
        when (sortOption) {
            SortOption.TITLE -> filteredBooks.sortedBy { it.title }
            SortOption.AUTHOR -> filteredBooks.sortedBy { it.author }
            SortOption.DATE_ADDED -> filteredBooks.reversed()
            SortOption.RATING -> filteredBooks.sortedByDescending { it.rating }
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
                    placeholder = { Text("Search books, authors, genres...") },
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
                    SearchResultsList(
                        books = sortedBooks,
                        onBookClick = onBookClick
                    )
                }
            } else {
                TopAppBar(
                    title = { Text("BookBuddy") },
                    actions = {
                        IconButton(onClick = { isSearching = true }) {
                            Icon(Icons.Default.Search, contentDescription = "Search")
                        }
                        IconButton(onClick = { showSortMenu = true }) {
                            Icon(Icons.Default.Menu, contentDescription = "Sort")
                        }
                        IconButton(onClick = {
                            viewMode = when (viewMode) {
                                ViewMode.GRID -> ViewMode.LIST
                                ViewMode.LIST -> ViewMode.COMPACT
                                ViewMode.COMPACT -> ViewMode.GRID
                            }
                        }) {
                            Icon(
                                when (viewMode) {
                                    ViewMode.GRID -> Icons.Default.Menu
                                    ViewMode.LIST -> Icons.Default.Menu
                                    ViewMode.COMPACT -> Icons.Default.Menu
                                },
                                contentDescription = "Change View"
                            )
                        }
                        IconButton(onClick = onProfileClick) {
                            Icon(Icons.Default.Person, contentDescription = "Profile")
                        }
                    }
                )
            }
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onAddBookClick,
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("Add Book") }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Stats Card
            ReadingStatsCard(
                totalBooks = booksWithCategory.size,
                collections = collections.size,
                modifier = Modifier.padding(16.dp)
            )

            // Collections Section
            if (collections.isNotEmpty()) {
                Text(
                    text = "Your Collections",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))

                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(collections) { collection ->
                        CollectionCard(
                            collection = collection,
                            onClick = { onCollectionClick(collection) }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }

            // All Books Section
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "All Books",
                    style = MaterialTheme.typography.titleLarge
                )
                Text(
                    text = "${sortedBooks.size} books",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.height(8.dp))

            // Books Display based on view mode
            when (viewMode) {
                ViewMode.GRID -> BooksGridView(
                    books = sortedBooks,
                    onBookClick = onBookClick
                )
                ViewMode.LIST -> BooksListView(
                    books = sortedBooks,
                    onBookClick = onBookClick
                )
                ViewMode.COMPACT -> BooksCompactView(
                    books = sortedBooks,
                    onBookClick = onBookClick
                )
            }
        }

        // Sort Menu
        DropdownMenu(
            expanded = showSortMenu,
            onDismissRequest = { showSortMenu = false }
        ) {
            DropdownMenuItem(
                text = { Text("Title") },
                onClick = {
                    sortOption = SortOption.TITLE
                    showSortMenu = false
                },
                leadingIcon = {
                    if (sortOption == SortOption.TITLE) {
                        Icon(Icons.Default.Check, contentDescription = null)
                    }
                }
            )
            DropdownMenuItem(
                text = { Text("Author") },
                onClick = {
                    sortOption = SortOption.AUTHOR
                    showSortMenu = false
                },
                leadingIcon = {
                    if (sortOption == SortOption.AUTHOR) {
                        Icon(Icons.Default.Check, contentDescription = null)
                    }
                }
            )
            DropdownMenuItem(
                text = { Text("Date Added") },
                onClick = {
                    sortOption = SortOption.DATE_ADDED
                    showSortMenu = false
                },
                leadingIcon = {
                    if (sortOption == SortOption.DATE_ADDED) {
                        Icon(Icons.Default.Check, contentDescription = null)
                    }
                }
            )
            DropdownMenuItem(
                text = { Text("Rating") },
                onClick = {
                    sortOption = SortOption.RATING
                    showSortMenu = false
                },
                leadingIcon = {
                    if (sortOption == SortOption.RATING) {
                        Icon(Icons.Default.Check, contentDescription = null)
                    }
                }
            )
        }
    }
}

@Composable
fun ReadingStatsCard(
    totalBooks: Int,
    collections: Int,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            StatItem(
                icon = Icons.Default.Star,
                value = totalBooks.toString(),
                label = "Books"
            )
            VerticalDivider(modifier = Modifier.height(48.dp))
            StatItem(
                icon = Icons.Default.Star,
                value = collections.toString(),
                label = "Collections"
            )
            VerticalDivider(modifier = Modifier.height(48.dp))
            StatItem(
                icon = Icons.Default.Star,
                value = "4.2",
                label = "Avg Rating"
            )
        }
    }
}

@Composable
fun StatItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    value: String,
    label: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onPrimaryContainer
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
    }
}

@Composable
fun CollectionCard(
    collection: BookCollection,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .width(160.dp)
            .height(120.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Icon(
                Icons.Default.Star,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
            Column {
                Text(
                    text = collection.title,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${collection.books.size} books",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun BooksGridView(
    books: List<BookWithCategory>,
    onBookClick: (BookWithCategory) -> Unit
) {
    androidx.compose.foundation.lazy.grid.LazyVerticalGrid(
        columns = androidx.compose.foundation.lazy.grid.GridCells.Fixed(2),
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(books.size) { index ->
            val book = books[index]
            BookGridCard(book = book, onClick = { onBookClick(book) })
        }
    }
}

@Composable
fun BookGridCard(
    book: BookWithCategory,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column {
            // Book Cover
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant),
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
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = book.title,
                    style = MaterialTheme.typography.titleSmall,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = book.author,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (book.rating > 0) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Star,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = Color(0xFFFFB300)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
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

@Composable
fun BooksListView(
    books: List<BookWithCategory>,
    onBookClick: (BookWithCategory) -> Unit
) {
    androidx.compose.foundation.lazy.LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(books.size) { index ->
            val book = books[index]
            BookListItem(book = book, onClick = { onBookClick(book) })
        }
    }
}

@Composable
fun BookListItem(
    book: BookWithCategory,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            // Book Cover
            Box(
                modifier = Modifier
                    .size(80.dp, 100.dp)
                    .clip(MaterialTheme.shapes.small)
                    .background(MaterialTheme.colorScheme.surfaceVariant),
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
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = book.title,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = book.author,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                if (book.categories.isNotEmpty()) {
                    Text(
                        text = book.categories.joinToString(", "),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                if (book.rating > 0) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Star,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = Color(0xFFFFB300)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = String.format("%.1f", book.rating),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }

            Icon(
                Icons.Default.ArrowForward,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun BooksCompactView(
    books: List<BookWithCategory>,
    onBookClick: (BookWithCategory) -> Unit
) {
    androidx.compose.foundation.lazy.LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp)
    ) {
        items(books.size) { index ->
            val book = books[index]
            ListItem(
                headlineContent = { Text(book.title, maxLines = 1) },
                supportingContent = { Text(book.author, maxLines = 1) },
                leadingContent = {
                    Icon(Icons.Default.Star, contentDescription = null)
                },
                trailingContent = {
                    if (book.rating > 0) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Star,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = Color(0xFFFFB300)
                            )
                            Text(String.format("%.1f", book.rating))
                        }
                    }
                },
                modifier = Modifier.clickable { onBookClick(book) }
            )
            HorizontalDivider()
        }
    }
}

@Composable
fun SearchResultsList(
    books: List<BookWithCategory>,
    onBookClick: (BookWithCategory) -> Unit
) {
    Column(
        modifier = Modifier
                .fillMaxWidth()
            .verticalScroll(rememberScrollState())
    ) {
        if (books.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "No books found",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            books.forEach { book ->
                ListItem(
                    headlineContent = { Text(book.title) },
                    supportingContent = { Text(book.author) },
                    leadingContent = {
                        Icon(Icons.Default.Star, contentDescription = null)
                    },
                    modifier = Modifier.clickable { onBookClick(book) }
                )
                HorizontalDivider()
            }
        }
    }
}

@Composable
fun VerticalDivider(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .width(1.dp)
            .background(MaterialTheme.colorScheme.outlineVariant)
    )
}
