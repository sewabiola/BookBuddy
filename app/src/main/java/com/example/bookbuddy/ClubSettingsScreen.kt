package com.example.bookbuddy

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ClubSettingsScreen(
    clubId: String,
    navController: NavController
) {
    val allClubs by BookBuddyDatabase.observeClubs().collectAsState()
    val club = allClubs.find { it.id == clubId }
    val currentUser = BookBuddyDatabase.getCurrentUser()

    var clubName by remember(club) { mutableStateOf(club?.name ?: "") }
    var clubDescription by remember(club) { mutableStateOf(club?.description ?: "") }
    var isPublic by remember(club) { mutableStateOf(club?.isPublic ?: true) }
    var clubRules by remember(club) { mutableStateOf(club?.rules ?: "") }
    var selectedGenres by remember(club) { mutableStateOf(club?.genreTags?.toSet() ?: emptySet()) }

    var showDeleteDialog by remember { mutableStateOf(false) }
    var showTransferDialog by remember { mutableStateOf(false) }
    var showGenreDialog by remember { mutableStateOf(false) }
    var showSaveSnackbar by remember { mutableStateOf(false) }
    var hasChanges by remember { mutableStateOf(false) }

    val members = remember(club) {
        club?.let { BookBuddyDatabase.getClubMembers(it.id) } ?: emptyList()
    }
    val userMembership = currentUser?.let { user ->
        club?.let { BookBuddyDatabase.getClubMember(it.id, user.userId) }
    }
    val isOwner = userMembership?.role == MemberRole.OWNER

    val availableGenres = listOf(
        "Fiction", "Non-Fiction", "Mystery", "Thriller", "Romance",
        "Science Fiction", "Fantasy", "Biography", "History", "Self-Help",
        "Classics", "Horror", "Poetry", "Young Adult", "Children's"
    )

    // Track changes
    LaunchedEffect(clubName, clubDescription, isPublic, clubRules, selectedGenres) {
        hasChanges = club?.let {
            clubName != it.name ||
                clubDescription != it.description ||
                isPublic != it.isPublic ||
                clubRules != (it.rules ?: "") ||
                selectedGenres != it.genreTags.toSet()
        } ?: false
    }

    if (club == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    Icons.Default.Warning,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.error
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text("Club not found")
                Spacer(modifier = Modifier.height(8.dp))
                Button(onClick = { navController.popBackStack() }) {
                    Text("Go Back")
                }
            }
        }
        return
    }

    fun saveChanges() {
        val updatedClub = club.copy(
            name = clubName.trim(),
            description = clubDescription.trim(),
            isPublic = isPublic,
            rules = clubRules.takeIf { it.isNotBlank() }?.trim(),
            genreTags = selectedGenres.toList()
        )
        BookBuddyDatabase.updateClub(updatedClub)
        showSaveSnackbar = true
        hasChanges = false
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Club Settings") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (hasChanges) {
                        TextButton(onClick = { saveChanges() }) {
                            Text("Save")
                        }
                    }
                }
            )
        },
        snackbarHost = {
            if (showSaveSnackbar) {
                Snackbar(
                    modifier = Modifier.padding(16.dp),
                    action = {
                        TextButton(onClick = { showSaveSnackbar = false }) {
                            Text("OK")
                        }
                    }
                ) {
                    Text("Changes saved successfully")
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            // Club Avatar Preview
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = clubName.take(2).uppercase().ifEmpty { "??" },
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            // Basic Info Section
            SettingsSection(title = "Basic Information") {
                OutlinedTextField(
                    value = clubName,
                    onValueChange = { if (it.length <= 50) clubName = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Club Name") },
                    singleLine = true,
                    supportingText = { Text("${clubName.length}/50") }
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = clubDescription,
                    onValueChange = { if (it.length <= 500) clubDescription = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 100.dp),
                    label = { Text("Description") },
                    supportingText = { Text("${clubDescription.length}/500") }
                )
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            // Genres Section
            SettingsSection(title = "Genres") {
                if (selectedGenres.isEmpty()) {
                    Text(
                        text = "No genres selected",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        selectedGenres.forEach { genre ->
                            InputChip(
                                selected = true,
                                onClick = { selectedGenres = selectedGenres - genre },
                                label = { Text(genre) },
                                trailingIcon = {
                                    Icon(
                                        Icons.Default.Close,
                                        contentDescription = "Remove",
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedButton(
                    onClick = { showGenreDialog = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Add Genres")
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            // Privacy Section
            SettingsSection(title = "Privacy") {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { isPublic = true }
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = isPublic,
                        onClick = { isPublic = true }
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Public Club",
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Text(
                            text = "Anyone can find and join",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { isPublic = false }
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = !isPublic,
                        onClick = { isPublic = false }
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Private Club",
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Text(
                            text = "Approval required to join",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            // Rules Section
            SettingsSection(title = "Club Rules") {
                OutlinedTextField(
                    value = clubRules,
                    onValueChange = { if (it.length <= 1000) clubRules = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 80.dp),
                    placeholder = { Text("Set rules for members (optional)") },
                    supportingText = { Text("${clubRules.length}/1000") }
                )
            }

            // Owner-only Actions
            if (isOwner) {
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                SettingsSection(title = "Ownership") {
                    OutlinedButton(
                        onClick = { showTransferDialog = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Transfer Ownership")
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                SettingsSection(title = "Danger Zone") {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        )
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Delete Club",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                            Text(
                                text = "This action cannot be undone. All club data will be permanently deleted.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Button(
                                onClick = { showDeleteDialog = true },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.error
                                )
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Delete Club")
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }

    // Genre Selection Dialog
    if (showGenreDialog) {
        AlertDialog(
            onDismissRequest = { showGenreDialog = false },
            title = { Text("Select Genres") },
            text = {
                LazyColumn {
                    items(availableGenres) { genre ->
                        val isSelected = selectedGenres.contains(genre)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    selectedGenres = if (isSelected) {
                                        selectedGenres - genre
                                    } else {
                                        selectedGenres + genre
                                    }
                                }
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = isSelected,
                                onCheckedChange = {
                                    selectedGenres = if (isSelected) {
                                        selectedGenres - genre
                                    } else {
                                        selectedGenres + genre
                                    }
                                }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(genre)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showGenreDialog = false }) {
                    Text("Done")
                }
            }
        )
    }

    // Transfer Ownership Dialog
    if (showTransferDialog) {
        val eligibleMembers = members.filter {
            it.role != MemberRole.OWNER && it.userId != currentUser?.userId
        }

        AlertDialog(
            onDismissRequest = { showTransferDialog = false },
            title = { Text("Transfer Ownership") },
            text = {
                if (eligibleMembers.isEmpty()) {
                    Text("There are no other members to transfer ownership to.")
                } else {
                    Column {
                        Text(
                            text = "Select a member to become the new owner:",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        LazyColumn(modifier = Modifier.heightIn(max = 300.dp)) {
                            items(eligibleMembers) { member ->
                                val user = BookBuddyDatabase.getUserById(member.userId)
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            BookBuddyDatabase.transferOwnership(clubId, member.userId)
                                            showTransferDialog = false
                                            navController.popBackStack()
                                        }
                                        .padding(vertical = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(CircleShape)
                                            .background(MaterialTheme.colorScheme.surfaceVariant),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = (user?.displayName ?: "?").take(1).uppercase(),
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            text = user?.displayName?.ifBlank { user.username } ?: "Unknown",
                                            style = MaterialTheme.typography.bodyLarge
                                        )
                                        Text(
                                            text = member.role.name.lowercase().replaceFirstChar { it.uppercase() },
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showTransferDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Delete Club Dialog
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            icon = { Icon(Icons.Default.Warning, contentDescription = null) },
            title = { Text("Delete Club") },
            text = {
                Text("Are you sure you want to delete \"${club.name}\"? This action cannot be undone and all club data will be permanently lost.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        BookBuddyDatabase.deleteClub(clubId)
                        showDeleteDialog = false
                        navController.navigate("clubs_discovery") {
                            popUpTo("clubs_discovery") { inclusive = true }
                        }
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
fun SettingsSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(12.dp))
        content()
    }
}
