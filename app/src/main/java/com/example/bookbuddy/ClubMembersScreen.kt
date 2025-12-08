package com.example.bookbuddy

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClubMembersScreen(
    clubId: String,
    navController: NavController
) {
    val allClubs by BookBuddyDatabase.observeClubs().collectAsState()
    val club = allClubs.find { it.id == clubId }
    val currentUser = BookBuddyDatabase.getCurrentUser()

    var selectedTab by remember { mutableStateOf(0) }
    var showRoleDialog by remember { mutableStateOf<ClubMember?>(null) }
    var showRemoveDialog by remember { mutableStateOf<ClubMember?>(null) }
    var showBanDialog by remember { mutableStateOf<ClubMember?>(null) }

    val members = remember(club, allClubs) {
        club?.let { BookBuddyDatabase.getClubMembers(it.id) } ?: emptyList()
    }
    val pendingRequests = remember(club, allClubs) {
        club?.let { BookBuddyDatabase.getPendingRequests(it.id) } ?: emptyList()
    }
    val userMembership = currentUser?.let { user ->
        club?.let { BookBuddyDatabase.getClubMember(it.id, user.userId) }
    }
    val isOwner = userMembership?.role == MemberRole.OWNER

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
                title = { Text("Manage Members") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
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
            // Tabs for Members and Pending Requests
            TabRow(selectedTabIndex = selectedTab) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Members (${members.size})") }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Requests")
                            if (pendingRequests.isNotEmpty()) {
                                Spacer(modifier = Modifier.width(4.dp))
                                Badge { Text("${pendingRequests.size}") }
                            }
                        }
                    }
                )
            }

            when (selectedTab) {
                0 -> {
                    // Members List
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(members.sortedBy { it.role.ordinal }) { member ->
                            val user = BookBuddyDatabase.getUserById(member.userId)
                            MemberManagementCard(
                                member = member,
                                username = user?.displayName?.ifBlank { user.username } ?: "Unknown User",
                                isOwner = isOwner,
                                isCurrentUser = member.userId == currentUser?.userId,
                                onChangeRole = { showRoleDialog = member },
                                onRemove = { showRemoveDialog = member },
                                onBan = { showBanDialog = member }
                            )
                        }
                    }
                }
                1 -> {
                    // Pending Requests
                    if (pendingRequests.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    Icons.Default.Add,
                                    contentDescription = null,
                                    modifier = Modifier.size(64.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = "No pending requests",
                                    style = MaterialTheme.typography.bodyLarge
                                )
                                Text(
                                    text = "New join requests will appear here",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(pendingRequests) { request ->
                                JoinRequestCard(
                                    request = request,
                                    onApprove = {
                                        BookBuddyDatabase.approveJoinRequest(clubId, request.userId)
                                    },
                                    onReject = {
                                        BookBuddyDatabase.rejectJoinRequest(clubId, request.userId)
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Role Change Dialog
    showRoleDialog?.let { member ->
        val user = BookBuddyDatabase.getUserById(member.userId)
        AlertDialog(
            onDismissRequest = { showRoleDialog = null },
            title = { Text("Change Role") },
            text = {
                Column {
                    Text("Select a role for ${user?.displayName ?: "this member"}:")
                    Spacer(modifier = Modifier.height(16.dp))

                    listOf(MemberRole.ADMIN, MemberRole.MODERATOR, MemberRole.MEMBER).forEach { role ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = member.role == role,
                                onClick = {
                                    BookBuddyDatabase.updateMemberRole(clubId, member.userId, role)
                                    showRoleDialog = null
                                }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = role.name.lowercase().replaceFirstChar { it.uppercase() },
                                    style = MaterialTheme.typography.bodyLarge
                                )
                                Text(
                                    text = when (role) {
                                        MemberRole.ADMIN -> "Can manage members and club settings"
                                        MemberRole.MODERATOR -> "Can add books and moderate content"
                                        else -> "Regular member"
                                    },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showRoleDialog = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Remove Member Dialog
    showRemoveDialog?.let { member ->
        val user = BookBuddyDatabase.getUserById(member.userId)
        AlertDialog(
            onDismissRequest = { showRemoveDialog = null },
            title = { Text("Remove Member") },
            text = {
                Text("Are you sure you want to remove ${user?.displayName ?: "this member"} from the club? They can rejoin later.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        BookBuddyDatabase.removeMember(clubId, member.userId)
                        showRemoveDialog = null
                    }
                ) {
                    Text("Remove", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showRemoveDialog = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Ban Member Dialog
    showBanDialog?.let { member ->
        val user = BookBuddyDatabase.getUserById(member.userId)
        AlertDialog(
            onDismissRequest = { showBanDialog = null },
            icon = { Icon(Icons.Default.Clear, contentDescription = null) },
            title = { Text("Ban Member") },
            text = {
                Text("Are you sure you want to ban ${user?.displayName ?: "this member"}? They will not be able to rejoin the club.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        BookBuddyDatabase.banMember(clubId, member.userId)
                        showBanDialog = null
                    }
                ) {
                    Text("Ban", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showBanDialog = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun MemberManagementCard(
    member: ClubMember,
    username: String,
    isOwner: Boolean,
    isCurrentUser: Boolean,
    onChangeRole: () -> Unit,
    onRemove: () -> Unit,
    onBan: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(
                        when (member.role) {
                            MemberRole.OWNER -> MaterialTheme.colorScheme.primaryContainer
                            MemberRole.ADMIN -> MaterialTheme.colorScheme.secondaryContainer
                            else -> MaterialTheme.colorScheme.surfaceVariant
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = username.take(1).uppercase(),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = when (member.role) {
                        MemberRole.OWNER -> MaterialTheme.colorScheme.onPrimaryContainer
                        MemberRole.ADMIN -> MaterialTheme.colorScheme.onSecondaryContainer
                        else -> MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = username,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium
                    )
                    if (isCurrentUser) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "(You)",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Role Badge
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = when (member.role) {
                            MemberRole.OWNER -> MaterialTheme.colorScheme.primaryContainer
                            MemberRole.ADMIN -> MaterialTheme.colorScheme.secondaryContainer
                            MemberRole.MODERATOR -> MaterialTheme.colorScheme.tertiaryContainer
                            else -> MaterialTheme.colorScheme.surfaceVariant
                        }
                    ) {
                        Text(
                            text = member.role.name.lowercase().replaceFirstChar { it.uppercase() },
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = when (member.role) {
                                MemberRole.OWNER -> MaterialTheme.colorScheme.onPrimaryContainer
                                MemberRole.ADMIN -> MaterialTheme.colorScheme.onSecondaryContainer
                                MemberRole.MODERATOR -> MaterialTheme.colorScheme.onTertiaryContainer
                                else -> MaterialTheme.colorScheme.onSurfaceVariant
                            }
                        )
                    }

                    Text(
                        text = "Joined ${SimpleDateFormat("MMM dd", Locale.getDefault()).format(Date(member.joinedAt))}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Actions (only show for owner, and not for themselves or other owners)
            if (isOwner && !isCurrentUser && member.role != MemberRole.OWNER) {
                Box {
                    IconButton(onClick = { showMenu = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "More options")
                    }
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Change Role") },
                            onClick = {
                                showMenu = false
                                onChangeRole()
                            },
                            leadingIcon = {
                                Icon(Icons.Default.Edit, contentDescription = null)
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Remove") },
                            onClick = {
                                showMenu = false
                                onRemove()
                            },
                            leadingIcon = {
                                Icon(Icons.Default.Delete, contentDescription = null)
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Ban", color = MaterialTheme.colorScheme.error) },
                            onClick = {
                                showMenu = false
                                onBan()
                            },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Clear,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun JoinRequestCard(
    request: JoinRequest,
    onApprove: () -> Unit,
    onReject: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = request.username.take(1).uppercase(),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = request.username,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "Requested ${SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date(request.requestedAt))}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (!request.message.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(12.dp))
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Text(
                        text = "\"${request.message}\"",
                        modifier = Modifier.padding(12.dp),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onReject,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Close, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Decline")
                }
                Button(
                    onClick = onApprove,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Check, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Approve")
                }
            }
        }
    }
}
