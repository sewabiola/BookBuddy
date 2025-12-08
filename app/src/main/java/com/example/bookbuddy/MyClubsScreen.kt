package com.example.bookbuddy

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyClubsScreen(
    navController: NavController
) {
    val userClubs by BookBuddyDatabase.observeUserClubs().collectAsState()
    val currentUser = BookBuddyDatabase.getCurrentUser()

    var selectedFilter by remember { mutableStateOf(MyClubsFilter.ALL) }

    val filteredClubs = remember(userClubs, selectedFilter) {
        when (selectedFilter) {
            MyClubsFilter.ALL -> userClubs
            MyClubsFilter.OWNED -> userClubs.filter { it.ownerId == currentUser?.userId }
            MyClubsFilter.MEMBER -> userClubs.filter { it.ownerId != currentUser?.userId }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Clubs") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { navController.navigate("clubs_discovery") }) {
                        Icon(Icons.Default.Search, contentDescription = "Discover Clubs")
                    }
                    IconButton(onClick = { navController.navigate("create_club") }) {
                        Icon(Icons.Default.Add, contentDescription = "Create Club")
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { navController.navigate("create_club") },
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("Create Club") }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Filter Chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MyClubsFilter.entries.forEach { filter ->
                    FilterChip(
                        selected = selectedFilter == filter,
                        onClick = { selectedFilter = filter },
                        label = {
                            Text(
                                when (filter) {
                                    MyClubsFilter.ALL -> "All (${userClubs.size})"
                                    MyClubsFilter.OWNED -> "Owned (${userClubs.count { it.ownerId == currentUser?.userId }})"
                                    MyClubsFilter.MEMBER -> "Joined (${userClubs.count { it.ownerId != currentUser?.userId }})"
                                }
                            )
                        }
                    )
                }
            }

            if (userClubs.isEmpty()) {
                // Empty State
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Icon(
                            Icons.Default.Person,
                            contentDescription = null,
                            modifier = Modifier.size(80.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "No clubs yet",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Join a club or create your own to connect with other book lovers!",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            OutlinedButton(onClick = { navController.navigate("clubs_discovery") }) {
                                Icon(Icons.Default.Search, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Discover")
                            }
                            Button(onClick = { navController.navigate("create_club") }) {
                                Icon(Icons.Default.Add, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Create")
                            }
                        }
                    }
                }
            } else if (filteredClubs.isEmpty()) {
                // No results for filter
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.List,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "No clubs match this filter",
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredClubs) { club ->
                        val membership = currentUser?.let {
                            BookBuddyDatabase.getClubMember(club.id, it.userId)
                        }
                        MyClubCard(
                            club = club,
                            memberRole = membership?.role,
                            onClick = { navController.navigate("club_detail/${club.id}") }
                        )
                    }

                    // Bottom padding for FAB
                    item {
                        Spacer(modifier = Modifier.height(72.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun MyClubCard(
    club: Club,
    memberRole: MemberRole?,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Club Avatar
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .clip(CircleShape)
                    .background(
                        when (memberRole) {
                            MemberRole.OWNER -> MaterialTheme.colorScheme.primaryContainer
                            MemberRole.ADMIN -> MaterialTheme.colorScheme.secondaryContainer
                            else -> MaterialTheme.colorScheme.tertiaryContainer
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = club.name.take(2).uppercase(),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = when (memberRole) {
                        MemberRole.OWNER -> MaterialTheme.colorScheme.onPrimaryContainer
                        MemberRole.ADMIN -> MaterialTheme.colorScheme.onSecondaryContainer
                        else -> MaterialTheme.colorScheme.onTertiaryContainer
                    }
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = club.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    if (!club.isPublic) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            Icons.Default.Lock,
                            contentDescription = "Private",
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = club.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Role Badge
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = when (memberRole) {
                            MemberRole.OWNER -> MaterialTheme.colorScheme.primaryContainer
                            MemberRole.ADMIN -> MaterialTheme.colorScheme.secondaryContainer
                            MemberRole.MODERATOR -> MaterialTheme.colorScheme.tertiaryContainer
                            else -> MaterialTheme.colorScheme.surfaceVariant
                        }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                when (memberRole) {
                                    MemberRole.OWNER -> Icons.Default.Star
                                    MemberRole.ADMIN -> Icons.Default.Settings
                                    MemberRole.MODERATOR -> Icons.Default.Check
                                    else -> Icons.Default.Person
                                },
                                contentDescription = null,
                                modifier = Modifier.size(14.dp),
                                tint = when (memberRole) {
                                    MemberRole.OWNER -> MaterialTheme.colorScheme.onPrimaryContainer
                                    MemberRole.ADMIN -> MaterialTheme.colorScheme.onSecondaryContainer
                                    MemberRole.MODERATOR -> MaterialTheme.colorScheme.onTertiaryContainer
                                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                                }
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = memberRole?.name?.lowercase()?.replaceFirstChar { it.uppercase() } ?: "Member",
                                style = MaterialTheme.typography.labelSmall,
                                color = when (memberRole) {
                                    MemberRole.OWNER -> MaterialTheme.colorScheme.onPrimaryContainer
                                    MemberRole.ADMIN -> MaterialTheme.colorScheme.onSecondaryContainer
                                    MemberRole.MODERATOR -> MaterialTheme.colorScheme.onTertiaryContainer
                                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                                }
                            )
                        }
                    }

                    // Member Count
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Person,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${club.memberCount}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
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

enum class MyClubsFilter {
    ALL,
    OWNED,
    MEMBER
}
