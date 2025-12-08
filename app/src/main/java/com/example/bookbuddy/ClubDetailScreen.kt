package com.example.bookbuddy

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClubDetailScreen(
    clubId: String,
    navController: NavController
) {
    val allClubs by BookBuddyDatabase.observeClubs().collectAsState()
    val club = allClubs.find { it.id == clubId }
    val currentUser = BookBuddyDatabase.getCurrentUser()

    var selectedTab by remember { mutableStateOf(0) }
    var showLeaveDialog by remember { mutableStateOf(false) }
    var showJoinRequestDialog by remember { mutableStateOf(false) }
    var joinRequestMessage by remember { mutableStateOf("") }
    var showInviteCodeDialog by remember { mutableStateOf(false) }
    var showInviteCodeEntryDialog by remember { mutableStateOf(false) }
    var inviteCodeInput by remember { mutableStateOf("") }

    val members = remember(club) {
        club?.let { BookBuddyDatabase.getClubMembers(it.id) } ?: emptyList()
    }
    val clubBooks = remember(club) {
        club?.let { BookBuddyDatabase.getClubBooks(it.id) } ?: emptyList()
    }
    val currentClubBook = remember(club) {
        club?.let { BookBuddyDatabase.getCurrentClubBook(it.id) }
    }
    val clubDiscussions = remember(club) {
        club?.let { BookBuddyDatabase.getClubDiscussions(it.id) } ?: emptyList()
    }
    val clubActivities = remember(club) {
        club?.let { BookBuddyDatabase.getClubActivities(it.id, limit = 10) } ?: emptyList()
    }
    val isMember = currentUser?.let { user ->
        club?.let { BookBuddyDatabase.isUserMember(it.id, user.userId) }
    } ?: false
    val userMembership = currentUser?.let { user ->
        club?.let { BookBuddyDatabase.getClubMember(it.id, user.userId) }
    }
    val hasPendingRequest = currentUser?.let { user ->
        club?.let { BookBuddyDatabase.hasPendingRequest(it.id, user.userId) }
    } ?: false
    val isOwnerOrAdmin = userMembership?.role == MemberRole.OWNER || userMembership?.role == MemberRole.ADMIN

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

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(club.name, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (isOwnerOrAdmin) {
                        IconButton(onClick = { navController.navigate("club_settings/${club.id}") }) {
                            Icon(Icons.Default.Settings, contentDescription = "Settings")
                        }
                    }
                    if (isMember && userMembership?.role != MemberRole.OWNER) {
                        IconButton(onClick = { showLeaveDialog = true }) {
                            Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = "Leave Club")
                        }
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Club Header
            item {
                ClubHeader(
                    club = club,
                    isMember = isMember,
                    hasPendingRequest = hasPendingRequest,
                    memberRole = userMembership?.role,
                    onJoinClick = {
                        if (club.isPublic) {
                            BookBuddyDatabase.joinClub(club.id)
                        } else {
                            showJoinRequestDialog = true
                        }
                    },
                    onInviteCodeClick = { showInviteCodeDialog = true },
                    onInviteCodeEntryClick = { showInviteCodeEntryDialog = true }
                )
            }

            // Current Book Section
            if (currentClubBook != null && isMember) {
                item {
                    CurrentBookSection(
                        book = currentClubBook,
                        onClick = { navController.navigate("book_details/${currentClubBook.id}") }
                    )
                }
            }

            // Tabs
            item {
                TabRow(
                    selectedTabIndex = selectedTab,
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = { Text("About") }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = { Text("Members (${club.memberCount})") }
                    )
                    if (isMember) {
                        Tab(
                            selected = selectedTab == 2,
                            onClick = { selectedTab = 2 },
                            text = { Text("Books") }
                        )
                        Tab(
                            selected = selectedTab == 3,
                            onClick = { selectedTab = 3 },
                            text = { Text("Discussions") }
                        )
                    }
                }
            }

            // Tab Content
            when (selectedTab) {
                0 -> {
                    // About Tab
                    item {
                        AboutTabContent(club = club)
                    }
                }
                1 -> {
                    // Members Tab
                    item {
                        MembersTabContent(
                            members = members,
                            club = club,
                            isOwnerOrAdmin = isOwnerOrAdmin,
                            navController = navController
                        )
                    }
                }
                2 -> {
                    // Books Tab (only for members)
                    if (isMember) {
                        item {
                            BooksTabContent(
                                books = clubBooks,
                                isOwnerOrAdmin = isOwnerOrAdmin,
                                clubId = club.id,
                                navController = navController
                            )
                        }
                    }
                }
                3 -> {
                    // Discussions Tab (only for members)
                    if (isMember) {
                        item {
                            DiscussionsTabContent(
                                discussions = clubDiscussions,
                                clubId = club.id,
                                isOwnerOrAdmin = isOwnerOrAdmin,
                                navController = navController
                            )
                        }
                    }
                }
            }
        }
    }

    // Leave Club Dialog
    if (showLeaveDialog) {
        AlertDialog(
            onDismissRequest = { showLeaveDialog = false },
            title = { Text("Leave Club") },
            text = { Text("Are you sure you want to leave ${club.name}?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        BookBuddyDatabase.leaveClub(club.id)
                        showLeaveDialog = false
                    }
                ) {
                    Text("Leave", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLeaveDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Join Request Dialog (for private clubs)
    if (showJoinRequestDialog) {
        AlertDialog(
            onDismissRequest = { showJoinRequestDialog = false },
            title = { Text("Request to Join") },
            text = {
                Column {
                    Text("Send a message with your request (optional):")
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = joinRequestMessage,
                        onValueChange = { joinRequestMessage = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Why do you want to join?") },
                        maxLines = 3
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        BookBuddyDatabase.createJoinRequest(
                            club.id,
                            joinRequestMessage.takeIf { it.isNotBlank() }
                        )
                        showJoinRequestDialog = false
                        joinRequestMessage = ""
                    }
                ) {
                    Text("Send Request")
                }
            },
            dismissButton = {
                TextButton(onClick = { showJoinRequestDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Invite Code Generation Dialog
    if (showInviteCodeDialog) {
        val inviteCode = BookBuddyDatabase.getInviteCode(club.id) ?: ""
        AlertDialog(
            onDismissRequest = { showInviteCodeDialog = false },
            title = { Text("Invite Code") },
            text = {
                Column {
                    if (inviteCode.isEmpty()) {
                        Text("No invite code generated yet. Generate one to share with others.")
                    } else {
                        Text("Share this code with others to let them join the club:")
                        Spacer(modifier = Modifier.height(16.dp))
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = inviteCode,
                                modifier = Modifier.padding(16.dp),
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Row {
                    if (inviteCode.isEmpty()) {
                        Button(onClick = {
                            BookBuddyDatabase.generateInviteCode(club.id)
                            showInviteCodeDialog = false
                        }) {
                            Text("Generate Code")
                        }
                    }
                    TextButton(onClick = { showInviteCodeDialog = false }) {
                        Text("Close")
                    }
                }
            }
        )
    }

    // Invite Code Entry Dialog
    if (showInviteCodeEntryDialog) {
        AlertDialog(
            onDismissRequest = { showInviteCodeEntryDialog = false },
            title = { Text("Join with Invite Code") },
            text = {
                Column {
                    Text("Enter the invite code to join this club:")
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = inviteCodeInput,
                        onValueChange = { inviteCodeInput = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Enter 8-digit code") },
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (BookBuddyDatabase.joinClubViaInviteCode(inviteCodeInput)) {
                            showInviteCodeEntryDialog = false
                            inviteCodeInput = ""
                        }
                    },
                    enabled = inviteCodeInput.length == 8
                ) {
                    Text("Join")
                }
            },
            dismissButton = {
                TextButton(onClick = { showInviteCodeEntryDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun ClubHeader(
    club: Club,
    isMember: Boolean,
    hasPendingRequest: Boolean,
    memberRole: MemberRole?,
    onJoinClick: () -> Unit,
    onInviteCodeClick: () -> Unit = {},
    onInviteCodeEntryClick: () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Club Avatar
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = club.name.take(2).uppercase(),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Club Name with Privacy Badge
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = club.name,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            if (!club.isPublic) {
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    Icons.Default.Lock,
                    contentDescription = "Private Club",
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Member Count
        Row(
            modifier = Modifier.padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.Person,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "${club.memberCount} members",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Genre Tags
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(club.genreTags) { tag ->
                SuggestionChip(
                    onClick = { },
                    label = { Text(tag) }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Join/Status Button
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            when {
                isMember -> {
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = MaterialTheme.colorScheme.tertiaryContainer,
                        modifier = Modifier.weight(1f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                Icons.Default.Check,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp),
                                tint = MaterialTheme.colorScheme.onTertiaryContainer
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = when (memberRole) {
                                    MemberRole.OWNER -> "Owner"
                                    MemberRole.ADMIN -> "Admin"
                                    MemberRole.MODERATOR -> "Moderator"
                                    else -> "Member"
                                },
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onTertiaryContainer
                            )
                        }
                    }
                    if (memberRole == MemberRole.OWNER || memberRole == MemberRole.ADMIN) {
                        OutlinedButton(onClick = onInviteCodeClick) {
                            Icon(Icons.Default.Share, contentDescription = null)
                        }
                    }
                }
                hasPendingRequest -> {
                    OutlinedButton(
                        onClick = { },
                        enabled = false,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Request Pending")
                    }
                }
                else -> {
                    Button(
                        onClick = onJoinClick,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(if (club.isPublic) "Join Club" else "Request to Join")
                    }
                    if (!club.isPublic) {
                        OutlinedButton(onClick = onInviteCodeEntryClick) {
                            Icon(Icons.Default.Lock, contentDescription = null)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CurrentBookSection(
    book: BookWithCategory,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.Star,
                contentDescription = null,
                modifier = Modifier.size(40.dp),
                tint = MaterialTheme.colorScheme.onSecondaryContainer
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Currently Reading",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
                Text(
                    text = book.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
                Text(
                    text = "by ${book.author}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f)
                )
            }
            Icon(
                Icons.Default.ArrowForward,
                contentDescription = "View Book",
                tint = MaterialTheme.colorScheme.onSecondaryContainer
            )
        }
    }
}

@Composable
fun AboutTabContent(club: Club) {
    val activities = remember(club) {
        BookBuddyDatabase.getClubActivities(club.id, limit = 5)
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        // Activity Feed
        if (activities.isNotEmpty()) {
            Text(
                text = "Recent Activity",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(8.dp))
            activities.forEach { activity ->
                ActivityItem(activity = activity)
                Spacer(modifier = Modifier.height(8.dp))
            }
            HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))
        }

        Text(
            text = "Description",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = club.description,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        if (!club.rules.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "Club Rules",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = club.rules,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Club Info
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            InfoItem(
                icon = Icons.Default.DateRange,
                label = "Created",
                value = SimpleDateFormat("MMM yyyy", Locale.getDefault())
                    .format(Date(club.createdAt))
            )
            InfoItem(
                icon = Icons.Default.Person,
                label = "Members",
                value = club.memberCount.toString()
            )
            InfoItem(
                icon = if (club.isPublic) Icons.Default.Share else Icons.Default.Lock,
                label = "Type",
                value = if (club.isPublic) "Public" else "Private"
            )
        }
    }
}

@Composable
fun InfoItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(
            icon,
            contentDescription = null,
            modifier = Modifier.size(24.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun MembersTabContent(
    members: List<ClubMember>,
    club: Club,
    isOwnerOrAdmin: Boolean,
    navController: NavController
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        if (isOwnerOrAdmin) {
            val pendingRequests = BookBuddyDatabase.getPendingRequests(club.id)
            if (pendingRequests.isNotEmpty()) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { navController.navigate("club_members/${club.id}") },
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "${pendingRequests.size} Pending Requests",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                text = "Tap to review",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                            )
                        }
                        Icon(
                            Icons.Default.ArrowForward,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            OutlinedButton(
                onClick = { navController.navigate("club_members/${club.id}") },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Settings, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Manage Members")
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        Text(
            text = "All Members",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(8.dp))

        members.forEach { member ->
            val user = BookBuddyDatabase.getUserById(member.userId)
            MemberListItem(
                member = member,
                username = user?.displayName?.ifBlank { user.username } ?: "Unknown User"
            )
        }
    }
}

@Composable
fun MemberListItem(
    member: ClubMember,
    username: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
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
                text = username.take(1).uppercase(),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = username,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = "Joined ${SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date(member.joinedAt))}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        if (member.role != MemberRole.MEMBER) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = when (member.role) {
                    MemberRole.OWNER -> MaterialTheme.colorScheme.primaryContainer
                    MemberRole.ADMIN -> MaterialTheme.colorScheme.secondaryContainer
                    else -> MaterialTheme.colorScheme.tertiaryContainer
                }
            ) {
                Text(
                    text = member.role.name.lowercase().replaceFirstChar { it.uppercase() },
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.labelSmall,
                    color = when (member.role) {
                        MemberRole.OWNER -> MaterialTheme.colorScheme.onPrimaryContainer
                        MemberRole.ADMIN -> MaterialTheme.colorScheme.onSecondaryContainer
                        else -> MaterialTheme.colorScheme.onTertiaryContainer
                    }
                )
            }
        }
    }
}

@Composable
fun BooksTabContent(
    books: List<BookWithCategory>,
    isOwnerOrAdmin: Boolean,
    clubId: String,
    navController: NavController
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        if (isOwnerOrAdmin) {
            var showBookPicker by remember { mutableStateOf(false) }
            OutlinedButton(
                onClick = { showBookPicker = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Add Book to Club")
            }
            Spacer(modifier = Modifier.height(16.dp))

            if (showBookPicker) {
                BookPickerDialog(
                    clubId = clubId,
                    existingBookIds = books.map { it.id },
                    onDismiss = { showBookPicker = false },
                    onBookSelected = { bookId ->
                        BookBuddyDatabase.addBookToClub(clubId, bookId)
                        showBookPicker = false
                    }
                )
            }
        }

        if (books.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.Star,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "No books yet",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Text(
                        text = "Books added to the club will appear here",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            books.forEach { book ->
                ClubBookItem(
                    book = book,
                    onClick = { navController.navigate("book_details/${book.id}") }
                )
            }
        }
    }
}

@Composable
fun DiscussionsTabContent(
    discussions: List<ClubDiscussion>,
    clubId: String,
    isOwnerOrAdmin: Boolean,
    navController: NavController
) {
    var showNewDiscussionDialog by remember { mutableStateOf(false) }
    val currentUser = BookBuddyDatabase.getCurrentUser()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Discussions",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Button(onClick = { showNewDiscussionDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(4.dp))
                Text("New")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (discussions.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.Info,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "No discussions yet",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Text(
                        text = "Start a discussion to engage with club members!",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            discussions.forEach { discussion ->
                DiscussionCard(
                    discussion = discussion,
                    onDelete = {
                        BookBuddyDatabase.deleteClubDiscussion(clubId, discussion.id)
                    },
                    canDelete = isOwnerOrAdmin || discussion.authorId == currentUser?.userId
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }

    if (showNewDiscussionDialog) {
        var title by remember { mutableStateOf("") }
        var body by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showNewDiscussionDialog = false },
            title = { Text("New Discussion") },
            text = {
                Column {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Title") },
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = body,
                        onValueChange = { body = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 100.dp),
                        label = { Text("Message") }
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (title.isNotBlank() && body.isNotBlank()) {
                            BookBuddyDatabase.createClubDiscussion(clubId, title, body)
                            showNewDiscussionDialog = false
                        }
                    },
                    enabled = title.isNotBlank() && body.isNotBlank()
                ) {
                    Text("Post")
                }
            },
            dismissButton = {
                TextButton(onClick = { showNewDiscussionDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun DiscussionCard(
    discussion: ClubDiscussion,
    onDelete: () -> Unit,
    canDelete: Boolean
) {
    var showDeleteDialog by remember { mutableStateOf(false) }

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = discussion.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "by ${discussion.authorName} • ${SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date(discussion.createdAt))}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (canDelete) {
                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = discussion.body,
                style = MaterialTheme.typography.bodyMedium
            )
            if (discussion.comments.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "${discussion.comments.size} comment${if (discussion.comments.size != 1) "s" else ""}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Discussion") },
            text = { Text("Are you sure you want to delete this discussion?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDelete()
                        showDeleteDialog = false
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
fun ClubBookItem(
    book: BookWithCategory,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Star,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = book.title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = book.author,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
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
fun ActivityItem(activity: ClubActivity) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
            Icon(
                when (activity.type) {
                    ActivityType.MEMBER_JOINED -> Icons.Default.Person
                    ActivityType.MEMBER_LEFT -> Icons.Default.Person
                    ActivityType.BOOK_ADDED -> Icons.Default.Add
                    ActivityType.BOOK_REMOVED -> Icons.Default.Delete
                    ActivityType.CURRENT_BOOK_CHANGED -> Icons.Default.Star
                    ActivityType.DISCUSSION_CREATED -> Icons.Default.Info
                    ActivityType.DISCUSSION_COMMENTED -> Icons.Default.Info
                },
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = when (activity.type) {
                    ActivityType.MEMBER_JOINED -> "${activity.username ?: "Someone"} joined the club"
                    ActivityType.MEMBER_LEFT -> "${activity.username ?: "Someone"} left the club"
                    ActivityType.BOOK_ADDED -> "${activity.username ?: "Someone"} added \"${activity.bookTitle ?: "a book"}\""
                    ActivityType.BOOK_REMOVED -> "${activity.username ?: "Someone"} removed \"${activity.bookTitle ?: "a book"}\""
                    ActivityType.CURRENT_BOOK_CHANGED -> "${activity.username ?: "Someone"} set the current book to \"${activity.bookTitle ?: "a book"}\""
                    ActivityType.DISCUSSION_CREATED -> "${activity.username ?: "Someone"} started a discussion: \"${activity.discussionTitle ?: "Untitled"}\""
                    ActivityType.DISCUSSION_COMMENTED -> "${activity.username ?: "Someone"} commented on \"${activity.discussionTitle ?: "a discussion"}\""
                },
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault()).format(Date(activity.timestamp)),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun BookPickerDialog(
    clubId: String,
    existingBookIds: List<String>,
    onDismiss: () -> Unit,
    onBookSelected: (String) -> Unit
) {
    val allBooks by BookBuddyDatabase.observeBooks().collectAsState()
    var searchQuery by remember { mutableStateOf("") }

    val availableBooks = remember(allBooks, existingBookIds, searchQuery) {
        allBooks.filter { book ->
            !existingBookIds.contains(book.id) &&
            (searchQuery.isBlank() ||
             book.title.contains(searchQuery, ignoreCase = true) ||
             book.author.contains(searchQuery, ignoreCase = true))
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select a Book") },
        text = {
            Column(modifier = Modifier.heightIn(max = 400.dp)) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Search books...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(8.dp))
                LazyColumn {
                    if (availableBooks.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = if (searchQuery.isBlank()) "No available books" else "No books found",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    } else {
                        items(availableBooks) { book ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .clickable { onBookSelected(book.id) }
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
                                            style = MaterialTheme.typography.bodyLarge,
                                            fontWeight = FontWeight.Medium
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
                Text("Cancel")
            }
        }
    )
}
